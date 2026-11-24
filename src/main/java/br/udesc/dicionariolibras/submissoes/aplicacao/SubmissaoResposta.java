package br.udesc.dicionariolibras.submissoes.aplicacao;

import java.time.Instant;
import java.util.UUID;

import br.udesc.dicionariolibras.dicionario.aplicacao.SinalResumo;
import br.udesc.dicionariolibras.identidade.aplicacao.UsuarioResumo;
import br.udesc.dicionariolibras.midia.aplicacao.VideoResumo;
import br.udesc.dicionariolibras.privacidade.aplicacao.ConsentimentoResumo;
import br.udesc.dicionariolibras.submissoes.dominio.StatusSubmissao;

/** Submissão com os dados dos outros módulos, montada para a tela. */
public record SubmissaoResposta(UUID id, StatusSubmissao status, Instant dataEnvio, String justificativa,
        UsuarioResumo autor, UsuarioResumo curador, SinalResumo sinal, VideoResumo video,
        ConsentimentoResumo consentimento) {
}
