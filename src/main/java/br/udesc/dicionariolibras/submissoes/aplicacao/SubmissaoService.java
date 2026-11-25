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
import br.udesc.dicionariolibras.submissoes.dominio.Assunto;
import br.udesc.dicionariolibras.submissoes.dominio.ConfiguracaoMao;
import br.udesc.dicionariolibras.submissoes.dominio.Consentimento;
import br.udesc.dicionariolibras.submissoes.dominio.Regiao;
import br.udesc.dicionariolibras.submissoes.dominio.Sinal;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;
import br.udesc.dicionariolibras.submissoes.dominio.Verbete;
import br.udesc.dicionariolibras.submissoes.dominio.Video;
import br.udesc.dicionariolibras.submissoes.infra.ArmazenamentoDeArquivos;
import br.udesc.dicionariolibras.submissoes.infra.AssuntoRepository;
import br.udesc.dicionariolibras.submissoes.infra.ConfiguracaoMaoRepository;
import br.udesc.dicionariolibras.submissoes.infra.RegiaoRepository;
import br.udesc.dicionariolibras.submissoes.infra.SubmissaoRepository;
import br.udesc.dicionariolibras.submissoes.infra.VerbeteRepository;
import lombok.RequiredArgsConstructor;

/** CRUD das submissões de sinais com vídeo (UC01, passos 1 a 4). */
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissaoService {

    private static final String VERSAO_TERMO = "1.0";
    private static final Duration VALIDADE_LINK = Duration.ofHours(1);

    private final SubmissaoRepository submissoes;
    private final VerbeteRepository verbetes;
    private final RegiaoRepository regioes;
    private final AssuntoRepository assuntos;
    private final ConfiguracaoMaoRepository configuracoes;
    private final ArmazenamentoDeArquivos armazenamento;

    /**
     * Cria a submissão: valida e guarda o vídeo, registra o consentimento e propõe o sinal. Se a palavra já
     * existe, o sinal entra como variação do verbete existente.
     */
    public SubmissaoResposta criar(NovaSubmissao nova) {
        if (!nova.aceiteTermo()) {
            throw new RegraDeNegocioException("É preciso aceitar o termo de uso de imagem para enviar o vídeo");
        }
        if (nova.video() == null) {
            throw new RegraDeNegocioException("Envie o vídeo do sinal");
        }
        Video video = guardarVideo(nova.video(), nova.duracaoSeg());

        Verbete verbete = verbetes.findFirstByPalavraIgnoreCase(nova.palavra().trim())
                .orElseGet(() -> preencher(new Verbete(nova.palavra().trim(), nova.acepcao(), assunto(nova.assunto())), nova));
        Sinal sinal = new Sinal(regiao(nova.regiao()), configuracao(nova.configuracaoMao()), video);
        verbete.adicionarSinal(sinal);
        if (verbete.getId() == null) {
            verbetes.save(verbete); // verbete novo: salva junto o sinal e o vídeo
        }
        verbetes.flush(); // verbete existente: o novo sinal é salvo pela lista de sinais

        String termo = nova.sinalizanteMenor() && nova.termoResponsavel() != null
                ? guardar("termos/", nova.termoResponsavel())
                : null;
        Submissao submissao = new Submissao(new Consentimento(VERSAO_TERMO, nova.sinalizanteMenor(), termo), sinal);
        submissao.enviar();
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

    /** Altera o verbete e o sinal; se vier um novo vídeo, ele substitui o atual, que é apagado do bucket. */
    public SubmissaoResposta alterar(UUID id, NovaSubmissao nova) {
        Submissao submissao = buscar(id);
        Sinal sinal = submissao.getSinal();
        Verbete verbete = preencher(sinal.getVerbete(), nova);
        verbete.setPalavra(nova.palavra().trim());
        verbete.setAcepcao(nova.acepcao());
        verbete.setAssunto(assunto(nova.assunto()));
        sinal.setRegiao(regiao(nova.regiao()));
        sinal.setConfiguracaoMao(configuracao(nova.configuracaoMao()));
        if (nova.video() != null) {
            String videoAntigo = sinal.getVideo().getUrl();
            sinal.setVideo(guardarVideo(nova.video(), nova.duracaoSeg()));
            depoisDeConfirmar(() -> armazenamento.remover(videoAntigo));
        }
        return resposta(submissoes.save(submissao));
    }

    /** Exclui a submissão, o sinal, o vídeo e o consentimento (LGPD); o verbete sai se ficar sem sinais. */
    public void excluir(UUID id) {
        Submissao submissao = buscar(id);
        Sinal sinal = submissao.getSinal();
        Verbete verbete = sinal.getVerbete();
        String video = sinal.getVideo().getUrl();
        String termo = submissao.getConsentimento().getTermoResponsavel();

        submissoes.delete(submissao);
        submissoes.flush();
        verbete.getSinais().remove(sinal);
        if (verbete.getSinais().isEmpty()) {
            verbetes.delete(verbete);
        }
        depoisDeConfirmar(() -> {
            armazenamento.remover(video);
            if (termo != null) {
                armazenamento.remover(termo);
            }
        });
    }

    private Submissao buscar(UUID id) {
        return submissoes.findById(id).orElseThrow(() -> new NaoEncontradoException("Submissão não encontrada"));
    }

    /** Campos opcionais do verbete. */
    private static Verbete preencher(Verbete verbete, NovaSubmissao nova) {
        verbete.setExemplo(nova.exemplo());
        verbete.setExemploLibras(nova.exemploLibras());
        verbete.setClasseGramatical(nova.classeGramatical());
        verbete.setOrigem(nova.origem());
        return verbete;
    }

    private Assunto assunto(String nome) {
        return assuntos.findByNome(nome).orElseThrow(() -> new RegraDeNegocioException("Assunto inválido: " + nome));
    }

    private Regiao regiao(String uf) {
        return uf == null ? null : regioes.findByUf(uf).orElseThrow(() -> new RegraDeNegocioException("Região inválida: " + uf));
    }

    private ConfiguracaoMao configuracao(String codigo) {
        return configuracoes.findByCodigo(codigo)
                .orElseThrow(() -> new RegraDeNegocioException("Configuração de mão inválida: " + codigo));
    }

    /** Valida o vídeo (RF04) antes de mandar para o bucket. A duração é lida pelo navegador. */
    private Video guardarVideo(ArquivoEnviado arquivo, Integer duracaoSeg) {
        String formato = formatoDe(arquivo);
        String chave = "videos/" + UUID.randomUUID() + "." + formato;
        Video video = new Video(chave, formato, duracaoSeg == null ? 0 : duracaoSeg, arquivo.tamanhoMB());
        video.motivoInvalido().ifPresent(motivo -> {
            throw new RegraDeNegocioException(motivo);
        });
        salvarNoBucket(chave, arquivo);
        return video;
    }

    private String guardar(String pasta, ArquivoEnviado arquivo) {
        String chave = pasta + UUID.randomUUID() + extensao(arquivo.nome());
        salvarNoBucket(chave, arquivo);
        return chave;
    }

    private void salvarNoBucket(String chave, ArquivoEnviado arquivo) {
        armazenamento.salvar(chave, arquivo.conteudo(), arquivo.contentType());
        // Se algo falhar depois do upload, o arquivo não fica perdido no bucket
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    armazenamento.remover(chave);
                }
            }
        });
    }

    private SubmissaoResposta resposta(Submissao s) {
        String url = armazenamento.urlTemporaria(s.getSinal().getVideo().getUrl(), VALIDADE_LINK).toString();
        return SubmissaoResposta.de(s, url);
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

    private static void depoisDeConfirmar(Runnable acao) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                acao.run();
            }
        });
    }
}
