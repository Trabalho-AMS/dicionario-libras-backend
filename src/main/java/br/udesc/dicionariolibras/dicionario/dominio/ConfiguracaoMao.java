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

/** Configuração de mão usada na busca por parâmetros. */
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
    @Column(length = 255)
    private String imagem;

    public ConfiguracaoMao(String codigo, String imagem) {
        this.codigo = codigo;
        this.imagem = imagem;
    }
}
