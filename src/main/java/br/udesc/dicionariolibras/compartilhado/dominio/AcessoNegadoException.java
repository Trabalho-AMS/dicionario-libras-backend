package br.udesc.dicionariolibras.compartilhado.dominio;

/** Lançada quando o usuário não tem o perfil necessário para a operação. */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
