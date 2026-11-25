package br.udesc.dicionariolibras.midia.infra;

import java.net.URL;
import java.time.Duration;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ArmazenamentoDeArquivos implements ArmazenamentoDeArquivos {

    private final S3Client s3;
    private final S3Presigner presigner;
    private final ArmazenamentoProperties props;

    @EventListener(ApplicationReadyEvent.class)
    public void criarBucketSeNecessario() {
        if (!props.criarBucket()) {
            return;
        }
        try {
            s3.headBucket(b -> b.bucket(props.bucket()));
        } catch (NoSuchBucketException e) {
            s3.createBucket(b -> b.bucket(props.bucket()));
            log.info("Bucket '{}' criado", props.bucket());
        }
    }

    @Override
    public void salvar(String chave, byte[] conteudo, String contentType) {
        s3.putObject(b -> b.bucket(props.bucket()).key(chave).contentType(contentType), RequestBody.fromBytes(conteudo));
    }

    @Override
    public URL urlTemporaria(String chave, Duration validade) {
        return presigner.presignGetObject(p -> p
                .signatureDuration(validade)
                .getObjectRequest(g -> g.bucket(props.bucket()).key(chave)))
                .url();
    }

    @Override
    public void remover(String chave) {
        s3.deleteObject(b -> b.bucket(props.bucket()).key(chave));
    }
}
