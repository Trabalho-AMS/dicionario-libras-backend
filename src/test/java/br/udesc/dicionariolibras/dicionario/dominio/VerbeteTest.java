package br.udesc.dicionariolibras.dicionario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;

class VerbeteTest {

    private final ConfiguracaoMao cm = new ConfiguracaoMao("CM01", null);

    private Verbete verbete() {
        return Verbete.builder().palavra("Pai").acepcao("Genitor").assunto(new Assunto("Família")).build();
    }

    @Test
    void sinalSemRegiaoEhPadraoNacional() {
        Sinal nacional = new Sinal(null, cm, UUID.randomUUID());
        Sinal gaucho = new Sinal(new Regiao("RS", "Rio Grande do Sul"), cm, UUID.randomUUID());

        assertThat(nacional.isPadrao()).isTrue();
        assertThat(gaucho.isPadrao()).isFalse();
    }

    @Test
    void adicionarSinalVinculaAoVerbete() {
        Verbete verbete = verbete();
        Sinal sinal = new Sinal(null, cm, UUID.randomUUID());

        verbete.adicionarSinal(sinal);

        assertThat(verbete.getSinais()).containsExactly(sinal);
        assertThat(sinal.getVerbete()).isSameAs(verbete);
        assertThat(sinal.isPublicado()).isFalse();
    }

    @Test
    void verbeteTemNoMaximoUmSinalPadraoPublicado() {
        Verbete verbete = verbete();
        Sinal primeiro = new Sinal(null, cm, UUID.randomUUID());
        Sinal segundo = new Sinal(null, cm, UUID.randomUUID());
        verbete.adicionarSinal(primeiro);
        verbete.adicionarSinal(segundo);
        primeiro.publicar();

        assertThatThrownBy(segundo::publicar).isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void variacoesRegionaisPodemSerPublicadasJuntoAoPadrao() {
        Verbete verbete = verbete();
        Sinal nacional = new Sinal(null, cm, UUID.randomUUID());
        Sinal carioca = new Sinal(new Regiao("RJ", "Rio de Janeiro"), cm, UUID.randomUUID());
        verbete.adicionarSinal(nacional);
        verbete.adicionarSinal(carioca);

        nacional.publicar();
        carioca.publicar();

        assertThat(verbete.getSinais()).allMatch(Sinal::isPublicado);
    }
}
