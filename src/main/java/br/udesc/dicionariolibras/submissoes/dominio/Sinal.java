package br.udesc.dicionariolibras.submissoes.dominio;

import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Sinal de um verbete. Sem região, é o padrão nacional; com região, é uma variação. */
@Entity
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

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "verbete_id")
    private Verbete verbete;

    @ManyToOne
    @JoinColumn(name = "regiao_id")
    private Regiao regiao;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "configuracao_mao_id")
    private ConfiguracaoMao configuracaoMao;

    @Setter
    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "video_id")
    private Video video;

    /** Sinal novo ainda não é publicado: depende da curadoria. */
    public Sinal(Regiao regiao, ConfiguracaoMao configuracaoMao, Video video) {
        setRegiao(regiao);
        this.configuracaoMao = configuracaoMao;
        this.video = video;
        this.publicado = false;
    }

    public void setRegiao(Regiao regiao) {
        this.regiao = regiao;
        this.padrao = regiao == null;
    }
}
