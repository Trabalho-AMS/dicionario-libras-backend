package br.udesc.dicionariolibras.submissoes.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracao_mao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfiguracaoMao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    /** Caminho da imagem ilustrativa. */
    private String imagem;
}
