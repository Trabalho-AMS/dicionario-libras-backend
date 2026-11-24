package br.udesc.dicionariolibras.compartilhado.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dicionarioLibrasOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Dicionário de Libras — API")
                .description("API da Expansão do Dicionário de Libras (AMS0002 — UDESC)")
                .version("0.0.1"));
    }
}
