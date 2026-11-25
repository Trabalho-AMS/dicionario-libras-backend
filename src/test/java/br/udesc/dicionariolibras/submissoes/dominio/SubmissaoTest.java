package br.udesc.dicionariolibras.submissoes.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.dicionario.dominio.Sinal;
import br.udesc.dicionariolibras.midia.dominio.Video;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;

class SubmissaoTest {

    private final Sinal sinal = new Sinal(null, null, new Video("videos/pai.mp4", "mp4", 8, 4.2));

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
}
