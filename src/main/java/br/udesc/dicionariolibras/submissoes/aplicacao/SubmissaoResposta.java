package br.udesc.dicionariolibras.submissoes.aplicacao;

import java.time.Instant;
import java.util.UUID;

import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;
import br.udesc.dicionariolibras.dicionario.dominio.Sinal;
import br.udesc.dicionariolibras.submissoes.dominio.StatusSubmissao;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;
import br.udesc.dicionariolibras.dicionario.dominio.Verbete;
import br.udesc.dicionariolibras.midia.dominio.Video;

/** Submissão como o frontend recebe: dados da submissão, do verbete, do sinal, do vídeo e do consentimento. */
public record SubmissaoResposta(UUID id, Instant dataEnvio, StatusSubmissao status, String palavra, String acepcao,
        String exemplo, String exemploLibras, String classeGramatical, String origem, String assunto, String regiao,
        String configuracaoMao, VideoResposta video, ConsentimentoResposta consentimento) {

    public record VideoResposta(String url, String formato, int duracaoSeg, double tamanhoMB) {
    }

    public record ConsentimentoResposta(String versaoTermo, Instant dataHora, boolean sinalizanteMenor) {
    }

    static SubmissaoResposta de(Submissao s, String urlVideo) {
        Sinal sinal = s.getSinal();
        Verbete v = sinal.getVerbete();
        Video video = sinal.getVideo();
        Consentimento c = s.getConsentimento();
        return new SubmissaoResposta(s.getId(), s.getDataEnvio(), s.getStatus(), v.getPalavra(), v.getAcepcao(),
                v.getExemplo(), v.getExemploLibras(), v.getClasseGramatical(), v.getOrigem(), v.getAssunto().getNome(),
                sinal.getRegiao() == null ? null : sinal.getRegiao().getUf(), sinal.getConfiguracaoMao().getCodigo(),
                new VideoResposta(urlVideo, video.getFormato(), video.getDuracaoSeg(), video.getTamanhoMB()),
                new ConsentimentoResposta(c.getVersaoTermo(), c.getDataHora(), c.isSinalizanteMenor()));
    }
}
