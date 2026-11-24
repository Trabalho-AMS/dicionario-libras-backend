package br.udesc.dicionariolibras.dicionario.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;

/** Palavra do dicionário. Tem um ou mais sinais (composição). */
@Entity
@Table(name = "verbete")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verbete {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assunto_id", nullable = false)
    private Assunto assunto;

    @OneToMany(mappedBy = "verbete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sinal> sinais = new ArrayList<>();

    @Builder
    public Verbete(String palavra, String acepcao, String exemplo, String exemploLibras,
            String classeGramatical, String origem, Assunto assunto) {
        this.palavra = palavra;
        this.acepcao = acepcao;
        this.exemplo = exemplo;
        this.exemploLibras = exemploLibras;
        this.classeGramatical = classeGramatical;
        this.origem = origem;
        this.assunto = assunto;
    }

    public List<Sinal> getSinais() {
        return Collections.unmodifiableList(sinais);
    }

    /** Adiciona um sinal ao verbete. Só pode existir um sinal padrão (nacional) publicado. */
    public void adicionarSinal(Sinal sinal) {
        if (sinal.isPadrao() && sinal.isPublicado() && possuiSinalPadraoPublicado()) {
            throw new RegraDeNegocioException("O verbete \"" + palavra + "\" já possui um sinal padrão publicado");
        }
        sinal.vincularAo(this);
        sinais.add(sinal);
    }

    /**
     * Altera os campos do verbete. Um verbete que já tem sinal publicado só muda pela curadoria,
     * então a alteração vinda de uma submissão é recusada.
     */
    public void alterar(String palavra, String acepcao, String exemplo, String classeGramatical, Assunto assunto) {
        if (possuiSinalPublicado()) {
            throw new RegraDeNegocioException("O verbete \"" + this.palavra + "\" já está publicado e não pode ser alterado por uma submissão");
        }
        this.palavra = palavra;
        this.acepcao = acepcao;
        this.exemplo = exemplo;
        this.classeGramatical = classeGramatical;
        this.assunto = assunto;
    }

    /** Remove um sinal ainda não publicado (ex.: o colaborador desistiu da submissão). */
    public void removerSinal(Sinal sinal) {
        if (sinal.isPublicado()) {
            throw new RegraDeNegocioException("Um sinal publicado não pode ser removido por uma submissão");
        }
        sinais.remove(sinal);
    }

    public boolean possuiSinalPublicado() {
        return sinais.stream().anyMatch(Sinal::isPublicado);
    }

    boolean possuiSinalPadraoPublicado() {
        return sinais.stream().anyMatch(s -> s.isPadrao() && s.isPublicado());
    }
}
