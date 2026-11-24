package br.udesc.dicionariolibras.midia.aplicacao;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

/** Porta para o armazenamento de objetos (bucket S3). */
public interface ArmazenamentoDeArquivos {

    void salvar(String chave, InputStream conteudo, long tamanhoBytes, String contentType);

    /** URL assinada e temporária para o navegador baixar/assistir o arquivo. */
    URL urlTemporaria(String chave, Duration validade);

    void remover(String chave);
}
