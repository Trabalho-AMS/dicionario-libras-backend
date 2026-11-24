package br.udesc.dicionariolibras.submissoes.dominio;

import java.util.UUID;

/** Evento publicado quando o curador pede ajuste ou rejeita uma submissão. */
public record SubmissaoDevolvida(UUID submissaoId, UUID sinalId, UUID autorId, StatusSubmissao status,
        String motivo) {
}
