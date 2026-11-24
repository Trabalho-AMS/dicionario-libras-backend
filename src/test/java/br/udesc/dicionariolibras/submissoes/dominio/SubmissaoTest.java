package br.udesc.dicionariolibras.submissoes.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.identidade.dominio.Perfil;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;

class SubmissaoTest {

    private Usuario autor;
    private Usuario curador;
    private Consentimento consentimento;
    private Submissao submissao;

    @BeforeEach
    void setUp() {
        autor = comId(new Usuario("Ana", "ana@exemplo.com", Perfil.COLABORADOR));
        curador = comId(new Usuario("Caio", "caio@exemplo.com", Perfil.CURADOR));
        consentimento = comId(new Consentimento("1.0", false, null));
        submissao = comId(new Submissao(autor, consentimento, UUID.randomUUID()));
    }

    @Test
    void enviarColocaEmAnalise() {
        submissao.enviar(consentimento);

        assertThat(submissao.getStatus()).isEqualTo(StatusSubmissao.EM_ANALISE);
        assertThat(submissao.getDataEnvio()).isNotNull();
    }

    @Test
    void naoEnviaComConsentimentoRevogado() {
        consentimento.revogar();

        assertThatThrownBy(() -> submissao.enviar(consentimento))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Consentimento inválido");
    }

    @Test
    void aprovarRegistraCuradorEEvento() {
        submissao.enviar(consentimento);

        submissao.aprovar(curador);

        assertThat(submissao.getStatus()).isEqualTo(StatusSubmissao.APROVADA);
        assertThat(submissao.getCuradorId()).isEqualTo(curador.getId());
        assertThat(eventos()).singleElement().isInstanceOf(SubmissaoAprovada.class);
    }

    @Test
    void curadorNaoAprovaAPropriaSubmissao() {
        Usuario curadorAutor = comId(new Usuario("Bia", "bia@exemplo.com", Perfil.CURADOR));
        Submissao propria = comId(new Submissao(curadorAutor, consentimento, UUID.randomUUID()));
        propria.enviar(consentimento);

        assertThatThrownBy(() -> propria.aprovar(curadorAutor))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("própria submissão");
    }

    @Test
    void colaboradorNaoPodeAvaliar() {
        Usuario outro = comId(new Usuario("Davi", "davi@exemplo.com", Perfil.COLABORADOR));
        submissao.enviar(consentimento);

        assertThatThrownBy(() -> submissao.aprovar(outro)).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void rejeitarExigeJustificativa() {
        submissao.enviar(consentimento);

        assertThatThrownBy(() -> submissao.rejeitar(curador, " ")).isInstanceOf(RegraDeNegocioException.class);

        submissao.rejeitar(curador, "Vídeo fora de enquadramento");
        assertThat(submissao.getStatus()).isEqualTo(StatusSubmissao.REJEITADA);
        assertThat(submissao.getJustificativa()).isEqualTo("Vídeo fora de enquadramento");
    }

    @Test
    void ajusteSolicitadoPodeSerReenviado() {
        submissao.enviar(consentimento);
        submissao.solicitarAjuste(curador, "Grave com fundo neutro");

        submissao.enviar(consentimento);

        assertThat(submissao.getStatus()).isEqualTo(StatusSubmissao.EM_ANALISE);
        assertThat(submissao.getJustificativa()).isNull();
    }

    @Test
    void naoAvaliaSubmissaoJaAprovada() {
        submissao.enviar(consentimento);
        submissao.aprovar(curador);

        assertThatThrownBy(() -> submissao.rejeitar(curador, "motivo"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não está em análise");
    }

    @SuppressWarnings("unchecked")
    private java.util.Collection<Object> eventos() {
        return (java.util.Collection<Object>) ReflectionTestUtils.invokeMethod(submissao, "domainEvents");
    }

    private static <T> T comId(T entidade) {
        ReflectionTestUtils.setField(entidade, "id", UUID.randomUUID());
        return entidade;
    }

    @Test
    void alterarAposAjusteVoltaParaAFila() {
        submissao.enviar(consentimento);
        submissao.solicitarAjuste(curador, "Enquadre melhor as mãos");

        submissao.registrarAlteracao(autor, consentimento);

        assertThat(submissao.getStatus()).isEqualTo(StatusSubmissao.EM_ANALISE);
        assertThat(submissao.getJustificativa()).isNull();
    }

    @Test
    void apenasOAutorAltera() {
        submissao.enviar(consentimento);

        assertThatThrownBy(() -> submissao.registrarAlteracao(curador, consentimento))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Apenas o autor");
    }

    @Test
    void naoAlteraSubmissaoRejeitada() {
        submissao.enviar(consentimento);
        submissao.rejeitar(curador, "Sinal incorreto");

        assertThatThrownBy(() -> submissao.registrarAlteracao(autor, consentimento))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void naoExcluiSubmissaoAprovada() {
        submissao.enviar(consentimento);
        submissao.aprovar(curador);

        assertThatThrownBy(() -> submissao.verificarExclusaoPor(autor))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já foi aprovada");
    }

    @Test
    void autorExcluiSubmissaoEmAnalise() {
        submissao.enviar(consentimento);

        submissao.verificarExclusaoPor(autor);
    }
}
