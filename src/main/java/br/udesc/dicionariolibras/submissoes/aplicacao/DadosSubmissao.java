package br.udesc.dicionariolibras.submissoes.aplicacao;

import br.udesc.dicionariolibras.compartilhado.aplicacao.ArquivoRecebido;
import br.udesc.dicionariolibras.dicionario.aplicacao.DadosVerbete;

/**
 * Tudo o que o colaborador envia numa submissão. Na alteração, {@code video} pode vir nulo para manter o atual.
 */
public record DadosSubmissao(DadosVerbete verbete, Long regiaoId, Long configuracaoMaoId, ArquivoRecebido video,
        Integer duracaoSeg, boolean aceiteTermo, boolean sinalizanteMenor, ArquivoRecebido termoResponsavel) {
}
