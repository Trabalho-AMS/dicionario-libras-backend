package br.udesc.dicionariolibras.privacidade.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConsentimentoTest {

    @Test
    void termoAceitoEhValido() {
        assertThat(new Consentimento("1.0", false, null).ehValido()).isTrue();
    }

    @Test
    void menorDeIdadeExigeTermoDoResponsavel() {
        assertThat(new Consentimento("1.0", true, null).ehValido()).isFalse();
        assertThat(new Consentimento("1.0", true, "termos/t.pdf").ehValido()).isTrue();
    }

    @Test
    void semVersaoDoTermoNaoEhValido() {
        assertThat(new Consentimento(" ", false, null).ehValido()).isFalse();
    }
}
