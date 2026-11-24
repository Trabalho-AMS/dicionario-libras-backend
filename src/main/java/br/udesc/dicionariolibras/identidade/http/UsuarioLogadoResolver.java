package br.udesc.dicionariolibras.identidade.http;

import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.bind.MissingRequestHeaderException;

import br.udesc.dicionariolibras.identidade.aplicacao.UsuarioService;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class UsuarioLogadoResolver implements HandlerMethodArgumentResolver {

    private final UsuarioService usuarios;

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return parametro.hasParameterAnnotation(UsuarioLogado.class) && Usuario.class.equals(parametro.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parametro, ModelAndViewContainer mav, NativeWebRequest requisicao,
            WebDataBinderFactory binderFactory) throws Exception {
        String valor = requisicao.getHeader(UsuarioLogado.CABECALHO);
        if (valor == null || valor.isBlank()) {
            throw new MissingRequestHeaderException(UsuarioLogado.CABECALHO, parametro);
        }
        try {
            return usuarios.buscar(UUID.fromString(valor));
        } catch (IllegalArgumentException e) {
            throw new MissingRequestHeaderException(UsuarioLogado.CABECALHO, parametro);
        }
    }
}
