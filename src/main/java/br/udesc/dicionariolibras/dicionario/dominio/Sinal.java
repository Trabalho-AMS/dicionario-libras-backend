package br.udesc.dicionariolibras.dicionario.dominio;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;

/**
 * Sinal de um verbete. Sem região, é o sinal padrão nacional; com região, é uma variação regional.
 * O vídeo pertence ao módulo de mídia e é referenciado pelo id.
 */
@Entity
@Table(name = "sinal")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sinal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private boolean padrao;

    @Column(nullable = false)
    private boolean publicado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "verbete_id", nullable = false)
    private Verbete verbete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regiao_id")
    private Regiao regiao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuracao_mao_id", nullable = false)
    private ConfiguracaoMao configuracaoMao;

    @Column(name = "video_id", nullable = false)
    private UUID videoId;

    /** Cria um sinal ainda não publicado (aguardando curadoria). */
    public Sinal(Regiao regiao, ConfiguracaoMao configuracaoMao, UUID videoId) {
        this.regiao = regiao;
        this.padrao = regiao == null;
        this.configuracaoMao = configuracaoMao;
        this.videoId = videoId;
        this.publicado = false;
    }

    public void publicar() {
        if (publicado) {
            return;
        }
        if (padrao && verbete.possuiSinalPadraoPublicado()) {
            throw new RegraDeNegocioException("O verbete já possui um sinal padrão publicado");
        }
        publicado = true;
    }

    public void despublicar() {
        publicado = false;
    }

    void vincularAo(Verbete verbete) {
        this.verbete = verbete;
    }
}
