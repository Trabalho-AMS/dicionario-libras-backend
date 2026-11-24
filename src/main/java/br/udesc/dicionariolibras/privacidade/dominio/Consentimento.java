package br.udesc.dicionariolibras.privacidade.dominio;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Aceite do termo de uso de imagem pelo sinalizante (LGPD). Sinalizante menor de 18 anos exige o termo
 * assinado pelo responsável legal (art. 14).
 */
@Entity
@Table(name = "consentimento")
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
    @Column(name = "termo_responsavel", length = 255)
    private String termoResponsavel;

    @Column(name = "revogado_em")
    private Instant revogadoEm;

    public Consentimento(String versaoTermo, boolean sinalizanteMenor, String termoResponsavel) {
        this.versaoTermo = versaoTermo;
        this.sinalizanteMenor = sinalizanteMenor;
        this.termoResponsavel = termoResponsavel;
        this.dataHora = Instant.now();
    }

    public boolean ehValido() {
        boolean termoAceito = versaoTermo != null && !versaoTermo.isBlank();
        boolean responsavelOk = !sinalizanteMenor || (termoResponsavel != null && !termoResponsavel.isBlank());
        return termoAceito && responsavelOk && !isRevogado();
    }

    public void revogar() {
        if (!isRevogado()) {
            revogadoEm = Instant.now();
        }
    }

    public boolean isRevogado() {
        return revogadoEm != null;
    }
}
