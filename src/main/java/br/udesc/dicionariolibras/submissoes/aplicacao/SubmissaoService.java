package br.udesc.dicionariolibras.submissoes.aplicacao;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao.VideoDoSinal;
import br.udesc.dicionariolibras.submissoes.infra.SubmissaoRepository;
import lombok.RequiredArgsConstructor;

/** CRUD das submissões de sinais com vídeo. */
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissaoService {

    private static final Duration VALIDADE_LINK = Duration.ofHours(1);

    private final SubmissaoRepository submissoes;
    private final ArmazenamentoDeArquivos armazenamento;

    public SubmissaoResposta criar(NovaSubmissao nova) {
        if (nova.video() == null) {
            throw new RegraDeNegocioException("Envie o vídeo do sinal");
        }
        VideoDoSinal video = guardarVideo(nova.video(), nova.duracaoSeg());
        String termo = nova.sinalizanteMenor() && nova.termoResponsavel() != null
                ? guardar("termos/", nova.termoResponsavel())
                : null;
        Submissao submissao = new Submissao(nova.dados(), video, nova.aceiteTermo(), nova.sinalizanteMenor(), termo);
        return resposta(submissoes.save(submissao));
    }

    @Transactional(readOnly = true)
    public List<SubmissaoResposta> listar() {
        return submissoes.findAllByOrderByDataEnvioDesc().stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public SubmissaoResposta detalhar(UUID id) {
        return resposta(buscar(id));
    }

    /** Altera os campos; se vier um novo vídeo, ele substitui o atual, que é apagado do bucket. */
    public SubmissaoResposta alterar(UUID id, NovaSubmissao nova) {
        Submissao submissao = buscar(id);
        submissao.alterarDados(nova.dados());
        if (nova.video() != null) {
            String videoAntigo = submissao.getVideoChave();
            submissao.trocarVideo(guardarVideo(nova.video(), nova.duracaoSeg()));
            depoisDeConfirmar(() -> armazenamento.remover(videoAntigo));
        }
        return resposta(submissao);
    }

    /** Exclui a submissão e apaga o vídeo e o termo do bucket (LGPD). */
    public void excluir(UUID id) {
        Submissao submissao = buscar(id);
        submissoes.delete(submissao);
        depoisDeConfirmar(() -> {
            armazenamento.remover(submissao.getVideoChave());
            if (submissao.getTermoResponsavel() != null) {
                armazenamento.remover(submissao.getTermoResponsavel());
            }
        });
    }

    private Submissao buscar(UUID id) {
        return submissoes.findById(id).orElseThrow(() -> new NaoEncontradoException("Submissão não encontrada"));
    }

    /** Valida o vídeo (RF04) antes de mandar para o bucket. A duração é lida pelo navegador. */
    private VideoDoSinal guardarVideo(ArquivoEnviado arquivo, Integer duracaoSeg) {
        String formato = formatoDe(arquivo);
        int duracao = duracaoSeg == null ? 0 : duracaoSeg;
        Submissao.motivoVideoInvalido(formato, duracao, arquivo.tamanhoMB()).ifPresent(motivo -> {
            throw new RegraDeNegocioException(motivo);
        });
        return new VideoDoSinal(guardar("videos/", arquivo), formato, duracao, arquivo.tamanhoMB());
    }

    private String guardar(String pasta, ArquivoEnviado arquivo) {
        String chave = pasta + UUID.randomUUID() + extensao(arquivo.nome());
        armazenamento.salvar(chave, arquivo.conteudo(), arquivo.contentType());
        // Se algo falhar depois do upload, o arquivo não fica perdido no bucket
        aoDesfazer(() -> armazenamento.remover(chave));
        return chave;
    }

    private SubmissaoResposta resposta(Submissao s) {
        return SubmissaoResposta.de(s, armazenamento.urlTemporaria(s.getVideoChave(), VALIDADE_LINK).toString());
    }

    private static String formatoDe(ArquivoEnviado arquivo) {
        String tipo = arquivo.contentType() == null ? "" : arquivo.contentType().toLowerCase(Locale.ROOT);
        return tipo.startsWith("video/") ? tipo.substring("video/".length()) : extensao(arquivo.nome()).replace(".", "");
    }

    private static String extensao(String nome) {
        if (nome == null || nome.lastIndexOf('.') < 0) {
            return "";
        }
        return nome.substring(nome.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }

    private static void aoDesfazer(Runnable acao) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    acao.run();
                }
            }
        });
    }

    private static void depoisDeConfirmar(Runnable acao) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                acao.run();
            }
        });
    }
}
