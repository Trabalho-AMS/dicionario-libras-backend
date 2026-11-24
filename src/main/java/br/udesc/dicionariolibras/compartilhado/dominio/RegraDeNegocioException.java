package br.udesc.dicionariolibras.compartilhado.dominio;

/** Lançada quando uma operação viola uma regra de negócio do domínio. */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
