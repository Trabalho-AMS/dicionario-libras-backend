package br.udesc.dicionariolibras.dicionario.aplicacao;

import java.util.UUID;

import br.udesc.dicionariolibras.dicionario.aplicacao.ApoioResposta.ItemApoio;

/** Sinal com o verbete ao qual pertence, para quem está fora do módulo dicionário. */
public record SinalResumo(UUID id, boolean padrao, boolean publicado, ItemApoio regiao, ItemApoio configuracaoMao,
        UUID videoId, VerbeteResumo verbete) {

    public record VerbeteResumo(UUID id, String palavra, String acepcao, String exemplo, String classeGramatical,
            ItemApoio assunto) {
    }
}
