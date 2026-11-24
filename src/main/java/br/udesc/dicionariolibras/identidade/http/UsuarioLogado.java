package br.udesc.dicionariolibras.identidade.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injeta o usuário da requisição, identificado pelo cabeçalho {@code X-Usuario-Id}.
 * O protótipo ainda não tem login: o frontend escolhe um dos usuários de demonstração.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsuarioLogado {

    String CABECALHO = "X-Usuario-Id";
}
