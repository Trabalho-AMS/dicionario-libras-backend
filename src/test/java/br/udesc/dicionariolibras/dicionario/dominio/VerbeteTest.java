package br.udesc.dicionariolibras.dicionario.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.udesc.dicionariolibras.midia.dominio.Video;

class VerbeteTest {

    private final Sinal sinal = new Sinal(null, null, new Video("videos/pai.mp4", "mp4", 8, 4.2));

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
