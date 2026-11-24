package br.udesc.dicionariolibras.submissoes.aplicacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.dicionariolibras.compartilhado.dominio.AcessoNegadoException;
import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.dicionario.aplicacao.DadosSinal;
import br.udesc.dicionariolibras.dicionario.aplicacao.DicionarioService;
import br.udesc.dicionariolibras.dicionario.aplicacao.SinalResumo;
import br.udesc.dicionariolibras.identidade.aplicacao.UsuarioResumo;
import br.udesc.dicionariolibras.identidade.aplicacao.UsuarioService;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import br.udesc.dicionariolibras.midia.aplicacao.MidiaService;
import br.udesc.dicionariolibras.privacidade.aplicacao.ConsentimentoService;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;
import br.udesc.dicionariolibras.submissoes.dominio.StatusSubmissao;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;
import br.udesc.dicionariolibras.submissoes.infra.SubmissaoRepository;
import lombok.RequiredArgsConstructor;

/**
 * Casos de uso do UC01 – Submeter e aprovar novo sinal com vídeo. Usa os outros módulos apenas pelas
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

    /** Passos 1 a 4 do UC01: valida o vídeo, registra o consentimento e coloca a submissão "Em análise". */
    public SubmissaoResposta criar(Usuario autor, DadosSubmissao dados) {
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
    public List<SubmissaoResposta> minhas(Usuario autor) {
        return submissoes.findByAutorIdOrderByDataEnvioDesc(autor.getId()).stream().map(this::resposta).toList();
    }

    /** Fila da curadoria. Sem status, lista todas as submissões. */
    @Transactional(readOnly = true)
    public List<SubmissaoResposta> fila(Usuario curador, StatusSubmissao status) {
        exigirCurador(curador);
        List<Submissao> lista = status == null
                ? submissoes.findAllByOrderByDataEnvioDesc()
                : submissoes.findByStatusOrderByDataEnvio(status);
        return lista.stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public SubmissaoResposta detalhar(UUID id, Usuario usuario) {
        Submissao submissao = buscar(id);
        if (!submissao.podeSerVistaPor(usuario)) {
            throw new AcessoNegadoException("Você não tem acesso a esta submissão");
        }
        return resposta(submissao);
    }

    /** O autor corrige a submissão; se o curador tinha pedido ajuste, ela volta para a fila. */
    public SubmissaoResposta alterar(UUID id, Usuario autor, DadosSubmissao dados) {
        Submissao submissao = buscar(id);
        Consentimento consentimento = consentimentos.buscar(submissao.getConsentimentoId());
        submissao.registrarAlteracao(autor, consentimento);

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

    /** O autor desiste da submissão: sinal, vídeo e consentimento são apagados (LGPD). */
    public void excluir(UUID id, Usuario autor) {
        Submissao submissao = buscar(id);
        submissao.verificarExclusaoPor(autor);
        SinalResumo sinal = dicionario.resumo(submissao.getSinalId());

        submissoes.delete(submissao);
        dicionario.removerSinalProposto(sinal.id());
        midia.remover(sinal.videoId());
        consentimentos.excluir(submissao.getConsentimentoId());
    }

    /** Passo 6 do UC01: aprova e publica o sinal (pelo evento SubmissaoAprovada). */
    public SubmissaoResposta aprovar(UUID id, Usuario curador) {
        Submissao submissao = buscar(id);
        submissao.aprovar(curador);
        return resposta(submissoes.save(submissao));
    }

    public SubmissaoResposta solicitarAjuste(UUID id, Usuario curador, String motivo) {
        Submissao submissao = buscar(id);
        submissao.solicitarAjuste(curador, motivo);
        return resposta(submissoes.save(submissao));
    }

    public SubmissaoResposta rejeitar(UUID id, Usuario curador, String motivo) {
        Submissao submissao = buscar(id);
        submissao.rejeitar(curador, motivo);
        return resposta(submissoes.save(submissao));
    }

    private Submissao buscar(UUID id) {
        return submissoes.findById(id).orElseThrow(() -> new NaoEncontradoException("Submissão não encontrada"));
    }

    private static void exigirCurador(Usuario usuario) {
        if (!usuario.ehCurador()) {
            throw new AcessoNegadoException("Apenas curadores acessam a fila de submissões");
        }
    }

    private static int duracao(DadosSubmissao dados) {
        if (dados.duracaoSeg() == null) {
            throw new RegraDeNegocioException("Informe a duração do vídeo");
        }
        return dados.duracaoSeg();
    }

    private SubmissaoResposta resposta(Submissao s) {
        SinalResumo sinal = dicionario.resumo(s.getSinalId());
        UsuarioResumo curador = usuarios.resumo(s.getCuradorId());
        return new SubmissaoResposta(s.getId(), s.getStatus(), s.getDataEnvio(), s.getJustificativa(),
                usuarios.resumo(s.getAutorId()), curador, sinal, midia.resumo(sinal.videoId()),
                consentimentos.resumo(s.getConsentimentoId()));
    }
}
