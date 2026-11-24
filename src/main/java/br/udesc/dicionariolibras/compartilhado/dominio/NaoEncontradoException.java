package br.udesc.dicionariolibras.compartilhado.dominio;

/** Lançada quando o recurso pedido não existe. */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
