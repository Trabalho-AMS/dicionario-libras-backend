package br.udesc.dicionariolibras.midia.aplicacao;

import java.util.UUID;

/** Dados do vídeo para exibição. A URL é temporária (assinada) e aponta para o bucket. */
public record VideoResumo(UUID id, String url, String formato, int duracaoSeg, double tamanhoMB) {
}
