package br.udesc.dicionariolibras.submissoes.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao.DadosDoSinal;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao.VideoDoSinal;

class SubmissaoTest {

    private final DadosDoSinal dados = new DadosDoSinal("Pai", "Genitor", null, "Substantivo", "Família", "RS", "CM01");
    private final VideoDoSinal video = new VideoDoSinal("videos/pai.mp4", "mp4", 8, 4.2);

    @Test
    void criaEmAnaliseComConsentimentoRegistrado() {
        Submissao s = new Submissao(dados, video, true, false, null);

        assertThat(s.getStatus()).isEqualTo(StatusSubmissao.EM_ANALISE);
        assertThat(s.getVersaoTermo()).isEqualTo(Submissao.VERSAO_TERMO);
        assertThat(s.getConsentimentoEm()).isNotNull();
        assertThat(s.getRegiao()).isEqualTo("RS");
    }

    @Test
    void exigeAceiteDoTermo() {
        assertThatThrownBy(() -> new Submissao(dados, video, false, false, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("aceitar o termo");
    }

    @Test
    void menorDeIdadeExigeTermoDoResponsavel() {
        assertThatThrownBy(() -> new Submissao(dados, video, true, true, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("responsável legal");

        assertThat(new Submissao(dados, video, true, true, "termos/t.pdf").isSinalizanteMenor()).isTrue();
    }

    @Test
    void videoValidoNaoTemMotivo() {
        assertThat(Submissao.motivoVideoInvalido("webm", 30, 50)).isEmpty();
    }

    @Test
    void recusaFormatoDuracaoETamanhoComMensagemDeComoCorrigir() {
        assertThat(Submissao.motivoVideoInvalido("avi", 10, 5)).hasValue("Formato \"avi\" não aceito; envie MP4 ou WebM");
        assertThat(Submissao.motivoVideoInvalido("mp4", 45, 5)).hasValue("O vídeo tem 45 s; o limite é 30 s");
        assertThat(Submissao.motivoVideoInvalido("mp4", 10, 51.5)).get().asString().contains("o limite é 50 MB");
        assertThat(Submissao.motivoVideoInvalido("mp4", 0, 5)).isPresent();
    }

    @Test
    void naoCriaComVideoInvalido() {
        VideoDoSinal longo = new VideoDoSinal("videos/x.mp4", "mp4", 45, 4);

        assertThatThrownBy(() -> new Submissao(dados, longo, true, false, null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O vídeo tem 45 s; o limite é 30 s");
    }

    @Test
    void alterarTrocaOsCampos() {
        Submissao s = new Submissao(dados, video, true, false, null);

        s.alterarDados(new DadosDoSinal("Pai", "Genitor; pai", "Meu pai", null, "Família", null, "CM02"));

        assertThat(s.getAcepcao()).isEqualTo("Genitor; pai");
        assertThat(s.getRegiao()).isNull();
        assertThat(s.getConfiguracaoMao()).isEqualTo("CM02");
    }
}
