package br.udesc.dicionariolibras.midia.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VideoTest {

    @Test
    void videoDentroDosLimitesEhValido() {
        assertThat(new Video("v.webm", "webm", 30, 50).validar()).isTrue();
    }

    @Test
    void videoInvalidoExplicaComoCorrigir() {
        assertThat(new Video("v.avi", "avi", 10, 5).motivoInvalido()).hasValue("Formato \"avi\" não aceito; envie MP4 ou WebM");
        assertThat(new Video("v.mp4", "mp4", 45, 5).motivoInvalido()).hasValue("O vídeo tem 45 s; o limite é 30 s");
        assertThat(new Video("v.mp4", "mp4", 10, 51.5).motivoInvalido()).get().asString().contains("o limite é 50 MB");
        assertThat(new Video("v.mp4", "mp4", 0, 5).validar()).isFalse();
    }
}
