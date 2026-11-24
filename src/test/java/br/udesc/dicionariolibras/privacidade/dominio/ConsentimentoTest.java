package br.udesc.dicionariolibras.privacidade.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConsentimentoTest {

    @Test
    void adultoComTermoAceitoEhValido() {
        assertThat(new Consentimento("1.0", false, null).ehValido()).isTrue();
    }

    @Test
    void menorSemTermoDoResponsavelEhInvalido() {
        assertThat(new Consentimento("1.0", true, null).ehValido()).isFalse();
    }

    @Test
    void menorComTermoDoResponsavelEhValido() {
        assertThat(new Consentimento("1.0", true, "termos/responsavel.pdf").ehValido()).isTrue();
    }

    @Test
    void revogadoDeixaDeSerValido() {
        Consentimento consentimento = new Consentimento("1.0", false, null);

        consentimento.revogar();

        assertThat(consentimento.ehValido()).isFalse();
        assertThat(consentimento.getRevogadoEm()).isNotNull();
    }
}
