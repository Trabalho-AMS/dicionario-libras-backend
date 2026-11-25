package br.udesc.dicionariolibras.submissoes.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;

class SubmissaoTest {

    private final Video video = new Video("videos/pai.mp4", "mp4", 8, 4.2);
    private final Sinal sinal = new Sinal(null, null, video);

    @Test
    void enviarColocaEmAnalise() {
        Submissao s = new Submissao(new Consentimento("1.0", false, null), sinal);

        s.enviar();

        assertThat(s.getStatus()).isEqualTo(StatusSubmissao.EM_ANALISE);
        assertThat(s.getDataEnvio()).isNotNull();
    }

    @Test
    void naoEnviaSemTermoDoResponsavelParaMenor() {
        Submissao s = new Submissao(new Consentimento("1.0", true, null), sinal);

        assertThatThrownBy(s::enviar)
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("responsável legal");
    }

    @Test
    void consentimentoDeMenorComTermoEhValido() {
        assertThat(new Consentimento("1.0", true, "termos/t.pdf").ehValido()).isTrue();
        assertThat(new Consentimento("1.0", false, null).ehValido()).isTrue();
        assertThat(new Consentimento(" ", false, null).ehValido()).isFalse();
    }

    @Test
    void videoValido() {
        assertThat(new Video("v.webm", "webm", 30, 50).validar()).isTrue();
    }

    @Test
    void videoInvalidoExplicaComoCorrigir() {
        assertThat(new Video("v.avi", "avi", 10, 5).motivoInvalido()).hasValue("Formato \"avi\" não aceito; envie MP4 ou WebM");
        assertThat(new Video("v.mp4", "mp4", 45, 5).motivoInvalido()).hasValue("O vídeo tem 45 s; o limite é 30 s");
        assertThat(new Video("v.mp4", "mp4", 10, 51.5).motivoInvalido()).get().asString().contains("o limite é 50 MB");
        assertThat(new Video("v.mp4", "mp4", 0, 5).validar()).isFalse();
    }

    @Test
    void sinalSemRegiaoEhOPadraoNacional() {
        assertThat(sinal.isPadrao()).isTrue();
        assertThat(sinal.isPublicado()).isFalse();
    }

    @Test
    void adicionarSinalLigaOSinalAoVerbete() {
        Verbete verbete = new Verbete("Pai", "Genitor", null);

        verbete.adicionarSinal(sinal);

        assertThat(verbete.getSinais()).containsExactly(sinal);
        assertThat(sinal.getVerbete()).isSameAs(verbete);
    }
}
