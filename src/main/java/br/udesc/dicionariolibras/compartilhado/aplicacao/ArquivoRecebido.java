package br.udesc.dicionariolibras.compartilhado.aplicacao;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.springframework.web.multipart.MultipartFile;

/** Arquivo enviado pelo usuário, sem depender da camada HTTP nos casos de uso. */
public record ArquivoRecebido(String nome, String contentType, long tamanhoBytes, Conteudo conteudo) {

    @FunctionalInterface
    public interface Conteudo {
        InputStream abrir() throws IOException;
    }

    public InputStream abrir() {
        try {
            return conteudo.abrir();
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o arquivo " + nome, e);
        }
    }

    /** Converte o arquivo do multipart; devolve null quando nada foi enviado. */
    public static ArquivoRecebido de(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }
        return new ArquivoRecebido(arquivo.getOriginalFilename(), arquivo.getContentType(), arquivo.getSize(),
                arquivo::getInputStream);
    }

    public double tamanhoMB() {
        return tamanhoBytes / (1024.0 * 1024.0);
    }
}
