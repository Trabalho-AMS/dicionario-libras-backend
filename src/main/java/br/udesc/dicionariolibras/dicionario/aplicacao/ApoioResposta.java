package br.udesc.dicionariolibras.dicionario.aplicacao;

import java.util.List;

/** Tabelas de apoio usadas nos formulários e na busca. */
public record ApoioResposta(List<ItemApoio> regioes, List<ItemApoio> assuntos, List<ItemApoio> configuracoesMao) {

    /** Item genérico: {@code codigo} é a UF da região ou o código da configuração de mão. */
    public record ItemApoio(Long id, String codigo, String nome) {
    }
}
