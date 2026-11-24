package br.udesc.dicionariolibras.submissoes.dominio;

import java.util.UUID;

/**
 * Evento publicado quando o curador aprova uma submissão. Os módulos dicionário (publicar o sinal),
 * mídia (transcodificar o vídeo) e notificações (avisar o colaborador) reagem a ele.
 */
public record SubmissaoAprovada(UUID submissaoId, UUID sinalId, UUID autorId, UUID curadorId) {
}
