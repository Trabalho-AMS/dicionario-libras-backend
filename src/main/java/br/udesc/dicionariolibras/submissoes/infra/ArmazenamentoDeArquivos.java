package br.udesc.dicionariolibras.submissoes.infra;

import java.net.URL;
import java.time.Duration;

/**
 * Armazenamento dos arquivos (vídeo e termo do responsável). A aplicação depende só desta interface;
 * a implementação com o bucket S3 fica em {@code infra}.
 */
public interface ArmazenamentoDeArquivos {

    void salvar(String chave, byte[] conteudo, String contentType);

    /** URL assinada e temporária para o navegador assistir/baixar o arquivo. */
    URL urlTemporaria(String chave, Duration validade);

    void remover(String chave);
}
