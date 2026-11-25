package br.udesc.dicionariolibras.submissoes.aplicacao;

/** Arquivo recebido no formulário (vídeo ou termo do responsável). */
public record ArquivoEnviado(String nome, String contentType, byte[] conteudo) {

    public double tamanhoMB() {
        return conteudo.length / (1024.0 * 1024.0);
    }
}
