package br.udesc.dicionariolibras.submissoes.aplicacao;

import java.net.URL;
import java.time.Duration;

/** Porta para o armazenamento de arquivos (bucket S3). */
public interface ArmazenamentoDeArquivos {

    void salvar(String chave, byte[] conteudo, String contentType);

    /** URL assinada e temporária para o navegador assistir/baixar o arquivo. */
    URL urlTemporaria(String chave, Duration validade);

    void remover(String chave);
}
