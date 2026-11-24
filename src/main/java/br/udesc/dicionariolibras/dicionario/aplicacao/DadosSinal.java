package br.udesc.dicionariolibras.dicionario.aplicacao;

import java.util.UUID;

/** Dados do sinal proposto. Sem região, é o sinal padrão nacional. */
public record DadosSinal(Long regiaoId, Long configuracaoMaoId, UUID videoId) {
}
