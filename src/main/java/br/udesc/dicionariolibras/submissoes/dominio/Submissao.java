package br.udesc.dicionariolibras.submissoes.dominio;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.AbstractAggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;

/**
 * Contribuição de um colaborador: liga o autor, o curador, o consentimento e o sinal proposto.
 * Outros módulos são referenciados pelo id, para manter os módulos independentes.
 * Os eventos registrados são publicados quando a submissão é salva pelo repositório.
 */
@Entity
@Table(name = "submissao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submissao extends AbstractAggregateRoot<Submissao> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "autor_id", nullable = false)
    private UUID autorId;

    @Column(name = "curador_id")
    private UUID curadorId;

    @Column(name = "consentimento_id", nullable = false)
    private UUID consentimentoId;

    @Column(name = "sinal_id", nullable = false, unique = true)
    private UUID sinalId;

    @Column(name = "data_envio")
    private Instant dataEnvio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSubmissao status;

    @Column(length = 1000)
    private String justificativa;

    public Submissao(Usuario autor, Consentimento consentimento, UUID sinalId) {
        this.autorId = autor.getId();
        this.consentimentoId = consentimento.getId();
        this.sinalId = sinalId;
    }

    /** Envia (ou reenvia após ajuste) a submissão para a fila de curadoria. */
    public void enviar(Consentimento consentimento) {
        if (status != null && status != StatusSubmissao.AJUSTE_SOLICITADO) {
            throw new RegraDeNegocioException("Só é possível reenviar uma submissão com ajuste solicitado");
        }
        if (!consentimento.getId().equals(consentimentoId) || !consentimento.ehValido()) {
            throw new RegraDeNegocioException(
                    "Consentimento inválido: aceite o termo (e envie o termo do responsável, se o sinalizante for menor de 18 anos)");
        }
        status = StatusSubmissao.EM_ANALISE;
        dataEnvio = Instant.now();
        justificativa = null;
        curadorId = null;
    }

    public void aprovar(Usuario curador) {
        registrarDecisao(curador);
        status = StatusSubmissao.APROVADA;
        registerEvent(new SubmissaoAprovada(id, sinalId, autorId, curadorId));
    }

    public void solicitarAjuste(Usuario curador, String motivo) {
        devolver(curador, motivo, StatusSubmissao.AJUSTE_SOLICITADO);
    }

    public void rejeitar(Usuario curador, String motivo) {
        devolver(curador, motivo, StatusSubmissao.REJEITADA);
    }

    /**
     * Registra a alteração feita pelo autor. Só é possível antes da aprovação ou rejeição; se o curador
     * tinha pedido ajuste, a submissão volta para a fila.
     */
    public void registrarAlteracao(Usuario autor, Consentimento consentimento) {
        verificarAutor(autor, "alterar");
        if (status != StatusSubmissao.EM_ANALISE && status != StatusSubmissao.AJUSTE_SOLICITADO) {
            throw new RegraDeNegocioException("Só é possível alterar uma submissão em análise ou com ajuste solicitado");
        }
        if (status == StatusSubmissao.AJUSTE_SOLICITADO) {
            enviar(consentimento);
        }
    }

    /** O autor pode desistir da submissão enquanto ela não foi aprovada (o sinal aprovado já está publicado). */
    public void verificarExclusaoPor(Usuario autor) {
        verificarAutor(autor, "excluir");
        if (status == StatusSubmissao.APROVADA) {
            throw new RegraDeNegocioException("A submissão já foi aprovada e o sinal está publicado; ela não pode ser excluída");
        }
    }

    public boolean podeSerVistaPor(Usuario usuario) {
        return usuario.getId().equals(autorId) || usuario.ehCurador();
    }

    private void verificarAutor(Usuario usuario, String acao) {
        if (!usuario.getId().equals(autorId)) {
            throw new RegraDeNegocioException("Apenas o autor pode " + acao + " a submissão");
        }
    }

    private void devolver(Usuario curador, String motivo, StatusSubmissao novoStatus) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Informe a justificativa para o colaborador");
        }
        registrarDecisao(curador);
        status = novoStatus;
        justificativa = motivo;
        registerEvent(new SubmissaoDevolvida(id, sinalId, autorId, novoStatus, motivo));
    }

    private void registrarDecisao(Usuario curador) {
        if (status != StatusSubmissao.EM_ANALISE) {
            throw new RegraDeNegocioException("A submissão não está em análise");
        }
        if (!curador.ehCurador()) {
            throw new RegraDeNegocioException("Apenas curadores podem avaliar submissões");
        }
        if (curador.getId().equals(autorId)) {
            throw new RegraDeNegocioException("O curador não pode avaliar a própria submissão");
        }
        curadorId = curador.getId();
    }
}
