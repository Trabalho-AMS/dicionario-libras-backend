package br.udesc.dicionariolibras.dicionario.aplicacao;

/** Campos do verbete informados pelo colaborador. */
public record DadosVerbete(String palavra, String acepcao, String exemplo, String classeGramatical, Long assuntoId) {
}
