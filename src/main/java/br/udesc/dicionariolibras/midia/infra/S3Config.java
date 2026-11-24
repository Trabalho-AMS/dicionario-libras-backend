package br.udesc.dicionariolibras.midia.infra;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(ArmazenamentoProperties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(ArmazenamentoProperties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of(props.regiao()))
                .credentialsProvider(credenciais(props))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(ArmazenamentoProperties props) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.endpointParaNavegador()))
                .region(Region.of(props.regiao()))
                .credentialsProvider(credenciais(props))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private static StaticCredentialsProvider credenciais(ArmazenamentoProperties props) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(props.accessKey(), props.secretKey()));
    }
}
