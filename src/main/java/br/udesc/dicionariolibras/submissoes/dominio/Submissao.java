package br.udesc.dicionariolibras.submissoes.dominio;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.dicionario.dominio.Sinal;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;

/** Contribuição de um colaborador: liga o consentimento ao sinal proposto. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submissao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_envio", nullable = false)
    private Instant dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSubmissao status;

    /** Preenchida pelo curador ao pedir ajuste ou rejeitar. */
    @Column(length = 1000)
    private String justificativa;

    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "consentimento_id")
    private Consentimento consentimento;

    @OneToOne(optional = false)
    @JoinColumn(name = "sinal_id")
    private Sinal sinal;

    public Submissao(Consentimento consentimento, Sinal sinal) {
        this.consentimento = consentimento;
        this.sinal = sinal;
    }

    /** Envia a submissão para a fila de curadoria; exige consentimento válido. */
    public void enviar() {
        if (!consentimento.ehValido()) {
            throw new RegraDeNegocioException(
                    "Sinalizante menor de 18 anos: anexe o termo assinado pelo responsável legal (LGPD, art. 14)");
        }
        status = StatusSubmissao.EM_ANALISE;
        dataEnvio = Instant.now();
    }
}
