package br.udesc.dicionariolibras.midia.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.armazenamento")
public record ArmazenamentoProperties(
        String endpoint,
        /** Endereço do bucket visto pelo navegador, usado nas URLs temporárias. Se vazio, usa {@code endpoint}. */
        String endpointPublico,
        String regiao,
        String accessKey,
        String secretKey,
        String bucket,
        boolean criarBucket) {

    public String endpointParaNavegador() {
        return endpointPublico == null || endpointPublico.isBlank() ? endpoint : endpointPublico;
    }
}
