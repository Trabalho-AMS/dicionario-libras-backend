package br.udesc.dicionariolibras.midia.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VideoTest {

    @Test
    void aceitaMp4DentroDosLimites() {
        assertThat(new Video("videos/a.mp4", "MP4", 12, 8.5).validar()).isTrue();
    }

    @Test
    void recusaVideoLongoExplicandoOLimite() {
        Video video = new Video("videos/a.webm", "webm", 45, 10);

        assertThat(video.validar()).isFalse();
        assertThat(video.motivoInvalido()).contains("O vídeo tem 45 s; o limite é 30 s");
    }

    @Test
    void recusaFormatoNaoSuportado() {
        assertThat(new Video("videos/a.avi", "avi", 10, 5).validar()).isFalse();
    }

    @Test
    void recusaArquivoGrande() {
        assertThat(new Video("videos/a.mp4", "mp4", 10, 51).validar()).isFalse();
    }

    @Test
    void transcodificarConverteParaMp4() {
        Video video = new Video("videos/a.webm", "webm", 10, 5);

        video.transcodificar();

        assertThat(video.getFormato()).isEqualTo("mp4");
        assertThat(video.isTranscodificado()).isTrue();
    }
}
