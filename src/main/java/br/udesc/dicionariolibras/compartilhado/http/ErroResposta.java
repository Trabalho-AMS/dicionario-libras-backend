package br.udesc.dicionariolibras.compartilhado.http;

/** Corpo das respostas de erro. A mensagem é pensada para ser mostrada ao usuário e dizer como corrigir. */
public record ErroResposta(int status, String mensagem) {
}
