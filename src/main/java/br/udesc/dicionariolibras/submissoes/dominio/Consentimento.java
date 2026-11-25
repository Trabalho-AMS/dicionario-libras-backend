package br.udesc.dicionariolibras.submissoes.dominio;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Aceite do termo de uso de imagem pelo sinalizante (LGPD). */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consentimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "versao_termo", nullable = false, length = 20)
    private String versaoTermo;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @Column(name = "sinalizante_menor", nullable = false)
    private boolean sinalizanteMenor;

    /** Chave, no bucket, do termo assinado pelo responsável legal. */
    @Column(name = "termo_responsavel")
    private String termoResponsavel;

    public Consentimento(String versaoTermo, boolean sinalizanteMenor, String termoResponsavel) {
        this.versaoTermo = versaoTermo;
        this.dataHora = Instant.now();
        this.sinalizanteMenor = sinalizanteMenor;
        this.termoResponsavel = termoResponsavel;
    }

    /** Termo aceito e, se o sinalizante for menor de 18 anos, termo do responsável (LGPD, art. 14). */
    public boolean ehValido() {
        boolean termoAceito = versaoTermo != null && !versaoTermo.isBlank();
        boolean responsavelOk = !sinalizanteMenor || (termoResponsavel != null && !termoResponsavel.isBlank());
        return termoAceito && responsavelOk;
    }
}
