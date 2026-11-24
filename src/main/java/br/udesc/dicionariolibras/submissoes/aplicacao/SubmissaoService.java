package br.udesc.dicionariolibras.submissoes.aplicacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.dicionario.aplicacao.DadosSinal;
import br.udesc.dicionariolibras.dicionario.aplicacao.DicionarioService;
import br.udesc.dicionariolibras.dicionario.aplicacao.SinalResumo;
import br.udesc.dicionariolibras.identidade.aplicacao.UsuarioService;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import br.udesc.dicionariolibras.midia.aplicacao.MidiaService;
import br.udesc.dicionariolibras.privacidade.aplicacao.ConsentimentoService;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;
import br.udesc.dicionariolibras.submissoes.infra.SubmissaoRepository;
import lombok.RequiredArgsConstructor;

/**
 * CRUD das submissões de sinais com vídeo (UC01, passos 1 a 4). Usa os outros módulos apenas pelas
 * interfaces públicas deles (serviços de aplicação), como pede a arquitetura.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubmissaoService {

    private final SubmissaoRepository submissoes;
    private final DicionarioService dicionario;
    private final MidiaService midia;
    private final ConsentimentoService consentimentos;
    private final UsuarioService usuarios;

    /** Valida o vídeo, registra o consentimento e coloca a submissão "Em análise". */
    public SubmissaoResposta criar(DadosSubmissao dados) {
        Usuario autor = usuarios.usuarioAtual();
        Consentimento consentimento = consentimentos.registrar(dados.aceiteTermo(), dados.sinalizanteMenor(),
                dados.termoResponsavel());
        UUID videoId = midia.enviar(dados.video(), duracao(dados));
        UUID sinalId = dicionario.proporSinal(dados.verbete(),
                new DadosSinal(dados.regiaoId(), dados.configuracaoMaoId(), videoId));

        Submissao submissao = new Submissao(autor, consentimento, sinalId);
        submissao.enviar(consentimento);
        return resposta(submissoes.save(submissao));
    }

    @Transactional(readOnly = true)
    public List<SubmissaoResposta> listar() {
        return submissoes.findByAutorIdOrderByDataEnvioDesc(usuarios.usuarioAtual().getId()).stream()
                .map(this::resposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubmissaoResposta detalhar(UUID id) {
        return resposta(buscar(id));
    }

    /** Altera os campos e, se vier um novo vídeo, troca o vídeo (o antigo é apagado do bucket). */
    public SubmissaoResposta alterar(UUID id, DadosSubmissao dados) {
        Submissao submissao = buscar(id);
        Consentimento consentimento = consentimentos.buscar(submissao.getConsentimentoId());
        submissao.registrarAlteracao(usuarios.usuarioAtual(), consentimento);

        SinalResumo sinalAtual = dicionario.resumo(submissao.getSinalId());
        UUID videoId = sinalAtual.videoId();
        if (dados.video() != null) {
            videoId = midia.enviar(dados.video(), duracao(dados));
            midia.remover(sinalAtual.videoId());
        }
        dicionario.alterarSinalProposto(submissao.getSinalId(), dados.verbete(),
                new DadosSinal(dados.regiaoId(), dados.configuracaoMaoId(), videoId));
        return resposta(submissoes.save(submissao));
    }

    /** Exclui a submissão com o sinal, o vídeo e o consentimento (LGPD). */
    public void excluir(UUID id) {
        Submissao submissao = buscar(id);
        submissao.verificarExclusaoPor(usuarios.usuarioAtual());
        SinalResumo sinal = dicionario.resumo(submissao.getSinalId());

        submissoes.delete(submissao);
        dicionario.removerSinalProposto(sinal.id());
        midia.remover(sinal.videoId());
        consentimentos.excluir(submissao.getConsentimentoId());
    }

    private Submissao buscar(UUID id) {
        return submissoes.findById(id).orElseThrow(() -> new NaoEncontradoException("Submissão não encontrada"));
    }

    private static int duracao(DadosSubmissao dados) {
        if (dados.duracaoSeg() == null) {
            throw new RegraDeNegocioException("Informe a duração do vídeo");
        }
        return dados.duracaoSeg();
    }

    private SubmissaoResposta resposta(Submissao s) {
        SinalResumo sinal = dicionario.resumo(s.getSinalId());
        return new SubmissaoResposta(s.getId(), s.getStatus(), s.getDataEnvio(), usuarios.resumo(s.getAutorId()),
                sinal, midia.resumo(sinal.videoId()), consentimentos.resumo(s.getConsentimentoId()));
    }
}
