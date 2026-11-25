package br.udesc.dicionariolibras.submissoes.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Região de uma variação de sinal. Um sinal sem região é o padrão nacional. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Regiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2)
    private String uf;

    @Column(nullable = false, length = 60)
    private String nome;
}
