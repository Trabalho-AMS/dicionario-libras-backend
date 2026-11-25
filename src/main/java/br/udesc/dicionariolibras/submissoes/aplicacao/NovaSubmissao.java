package br.udesc.dicionariolibras.submissoes.aplicacao;

/** Dados do formulário. Na alteração, {@code video} pode vir nulo para manter o vídeo atual. */
public record NovaSubmissao(String palavra, String acepcao, String exemplo, String exemploLibras,
        String classeGramatical, String origem, String assunto, String regiao, String configuracaoMao,
        ArquivoEnviado video, Integer duracaoSeg, boolean aceiteTermo, boolean sinalizanteMenor,
        ArquivoEnviado termoResponsavel) {
}
