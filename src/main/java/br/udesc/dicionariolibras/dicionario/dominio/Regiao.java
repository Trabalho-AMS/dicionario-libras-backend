package br.udesc.dicionariolibras.dicionario.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Região de uma variação de sinal. Um sinal sem região é o padrão nacional. */
@Entity
@Table(name = "regiao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Regiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false, length = 60)
    private String nome;

    public Regiao(String uf, String nome) {
        this.uf = uf;
        this.nome = nome;
    }
}
