package br.udesc.dicionariolibras.compartilhado.http;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;

/** Converte as exceções em respostas HTTP com uma mensagem legível. */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    ResponseEntity<ErroResposta> regraDeNegocio(RegraDeNegocioException e) {
        return erro(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<ErroResposta> naoEncontrado(NaoEncontradoException e) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    ResponseEntity<ErroResposta> validacao(BindException e) {
        String mensagem = e.getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<ErroResposta> arquivoFaltando(MissingServletRequestPartException e) {
        return erro(HttpStatus.BAD_REQUEST, "Envie o arquivo \"" + e.getRequestPartName() + "\"");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErroResposta> arquivoGrande(MaxUploadSizeExceededException e) {
        return erro(HttpStatus.CONTENT_TOO_LARGE, "O arquivo passa do limite de 50 MB");
    }

    private static ResponseEntity<ErroResposta> erro(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResposta(status.value(), mensagem));
    }
}
