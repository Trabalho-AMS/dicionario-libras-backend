package br.udesc.dicionariolibras.submissoes.aplicacao;

import br.udesc.dicionariolibras.submissoes.dominio.Submissao.DadosDoSinal;

/** Dados do formulário. Na alteração, {@code video} pode vir nulo para manter o vídeo atual. */
public record NovaSubmissao(DadosDoSinal dados, ArquivoEnviado video, Integer duracaoSeg, boolean aceiteTermo,
        boolean sinalizanteMenor, ArquivoEnviado termoResponsavel) {
}
