package br.udesc.dicionariolibras.submissoes.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Palavra do dicionário. Tem um ou mais sinais (composição). */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verbete {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String palavra;

    @Column(nullable = false, length = 1000)
    private String acepcao;

    @Column(length = 500)
    private String exemplo;

    @Column(name = "exemplo_libras", length = 500)
    private String exemploLibras;

    @Column(name = "classe_gramatical", length = 40)
    private String classeGramatical;

    @Column(length = 120)
    private String origem;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assunto_id")
    private Assunto assunto;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "verbete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sinal> sinais = new ArrayList<>();

    public Verbete(String palavra, String acepcao, Assunto assunto) {
        this.palavra = palavra;
        this.acepcao = acepcao;
        this.assunto = assunto;
    }

    public void adicionarSinal(Sinal sinal) {
        sinal.setVerbete(this);
        sinais.add(sinal);
    }
}
