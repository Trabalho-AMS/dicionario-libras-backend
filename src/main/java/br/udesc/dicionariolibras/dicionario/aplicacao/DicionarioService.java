package br.udesc.dicionariolibras.dicionario.aplicacao;

import java.util.Comparator;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.dicionario.aplicacao.ApoioResposta.ItemApoio;
import br.udesc.dicionariolibras.dicionario.aplicacao.SinalResumo.VerbeteResumo;
import br.udesc.dicionariolibras.dicionario.dominio.Assunto;
import br.udesc.dicionariolibras.dicionario.dominio.ConfiguracaoMao;
import br.udesc.dicionariolibras.dicionario.dominio.Regiao;
import br.udesc.dicionariolibras.dicionario.dominio.Sinal;
import br.udesc.dicionariolibras.dicionario.dominio.Verbete;
import br.udesc.dicionariolibras.dicionario.infra.AssuntoRepository;
import br.udesc.dicionariolibras.dicionario.infra.ConfiguracaoMaoRepository;
import br.udesc.dicionariolibras.dicionario.infra.RegiaoRepository;
import br.udesc.dicionariolibras.dicionario.infra.SinalRepository;
import br.udesc.dicionariolibras.dicionario.infra.VerbeteRepository;
import lombok.RequiredArgsConstructor;

/** Interface pública do módulo dicionário: sinais propostos e tabelas de apoio. */
@Service
@RequiredArgsConstructor
@Transactional
public class DicionarioService {

    private final VerbeteRepository verbetes;
    private final SinalRepository sinais;
    private final AssuntoRepository assuntos;
    private final RegiaoRepository regioes;
    private final ConfiguracaoMaoRepository configuracoes;

    /**
     * Cria um sinal ainda não publicado. Se a palavra já existe, o sinal entra como variação do verbete
     * existente (fluxo alternativo do UC01); senão, cria o verbete.
     */
    public UUID proporSinal(DadosVerbete dadosVerbete, DadosSinal dadosSinal) {
        Verbete verbete = verbetes.findFirstByPalavraIgnoreCase(dadosVerbete.palavra().trim())
                .orElseGet(() -> Verbete.builder()
                        .palavra(dadosVerbete.palavra().trim())
                        .acepcao(dadosVerbete.acepcao())
                        .exemplo(dadosVerbete.exemplo())
                        .classeGramatical(dadosVerbete.classeGramatical())
                        .assunto(assunto(dadosVerbete.assuntoId()))
                        .build());
        Sinal sinal = new Sinal(regiao(dadosSinal.regiaoId()), configuracao(dadosSinal.configuracaoMaoId()),
                dadosSinal.videoId());
        verbete.adicionarSinal(sinal);
        if (verbete.getId() == null) {
            verbetes.saveAndFlush(verbete); // verbete novo: o sinal é salvo junto (cascade)
        } else {
            sinais.saveAndFlush(sinal); // verbete existente: salva só o novo sinal, sem merge do verbete
        }
        return sinal.getId();
    }

    /** Altera um sinal proposto e, se o verbete ainda não estiver publicado, os campos do verbete. */
    public void alterarSinalProposto(UUID sinalId, DadosVerbete dadosVerbete, DadosSinal dadosSinal) {
        Sinal sinal = sinal(sinalId);
        Verbete verbete = sinal.getVerbete();
        if (!verbete.possuiSinalPublicado()) {
            verbete.alterar(dadosVerbete.palavra().trim(), dadosVerbete.acepcao(), dadosVerbete.exemplo(),
                    dadosVerbete.classeGramatical(), assunto(dadosVerbete.assuntoId()));
        }
        sinal.alterar(regiao(dadosSinal.regiaoId()), configuracao(dadosSinal.configuracaoMaoId()), dadosSinal.videoId());
    }

    /** Remove um sinal não publicado; o verbete também sai se ficar sem nenhum sinal. */
    public void removerSinalProposto(UUID sinalId) {
        Sinal sinal = sinal(sinalId);
        Verbete verbete = sinal.getVerbete();
        verbete.removerSinal(sinal);
        if (verbete.getSinais().isEmpty()) {
            verbetes.delete(verbete);
        }
    }

    @Transactional(readOnly = true)
    public SinalResumo resumo(UUID sinalId) {
        return resumoDe(sinal(sinalId));
    }

    @Transactional(readOnly = true)
    public ApoioResposta apoio() {
        return new ApoioResposta(
                regioes.findAll().stream().sorted(Comparator.comparing(Regiao::getNome)).map(DicionarioService::item).toList(),
                assuntos.findAll().stream().map(DicionarioService::item).toList(),
                configuracoes.findAll().stream().sorted(Comparator.comparing(ConfiguracaoMao::getCodigo))
                        .map(DicionarioService::item).toList());
    }

    private SinalResumo resumoDe(Sinal s) {
        return new SinalResumo(s.getId(), s.isPadrao(), s.isPublicado(),
                s.getRegiao() == null ? null : item(s.getRegiao()), item(s.getConfiguracaoMao()), s.getVideoId(),
                verbeteResumo(s.getVerbete()));
    }

    private static VerbeteResumo verbeteResumo(Verbete v) {
        return new VerbeteResumo(v.getId(), v.getPalavra(), v.getAcepcao(), v.getExemplo(), v.getClasseGramatical(),
                item(v.getAssunto()));
    }

    private Sinal sinal(UUID id) {
        return sinais.findById(id).orElseThrow(() -> new NaoEncontradoException("Sinal não encontrado"));
    }

    private Assunto assunto(Long id) {
        if (id == null) {
            throw new RegraDeNegocioException("Escolha o assunto do verbete");
        }
        return assuntos.findById(id).orElseThrow(() -> new RegraDeNegocioException("Assunto inválido"));
    }

    private Regiao regiao(Long id) {
        return id == null ? null : regioes.findById(id).orElseThrow(() -> new RegraDeNegocioException("Região inválida"));
    }

    private ConfiguracaoMao configuracao(Long id) {
        if (id == null) {
            throw new RegraDeNegocioException("Escolha a configuração de mão do sinal");
        }
        return configuracoes.findById(id).orElseThrow(() -> new RegraDeNegocioException("Configuração de mão inválida"));
    }

    private static ItemApoio item(Regiao r) {
        return new ItemApoio(r.getId(), r.getUf(), r.getNome());
    }

    private static ItemApoio item(Assunto a) {
        return new ItemApoio(a.getId(), null, a.getNome());
    }

    private static ItemApoio item(ConfiguracaoMao c) {
        return new ItemApoio(c.getId(), c.getCodigo(), c.getCodigo());
    }
}
