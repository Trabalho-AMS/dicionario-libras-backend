package br.udesc.dicionariolibras.midia.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.armazenamento")
public record ArmazenamentoProperties(
        String endpoint,
        String regiao,
        String accessKey,
        String secretKey,
        String bucket,
        boolean criarBucket) {
}
