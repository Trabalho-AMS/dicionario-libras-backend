package br.udesc.dicionariolibras.submissoes.aplicacao;

import java.time.Instant;
import java.util.UUID;

import br.udesc.dicionariolibras.submissoes.dominio.StatusSubmissao;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;

/** Submissão como o frontend recebe, com um link temporário para o vídeo. */
public record SubmissaoResposta(UUID id, String palavra, String acepcao, String exemplo, String classeGramatical,
        String assunto, String regiao, String configuracaoMao, Video video, boolean sinalizanteMenor,
        String versaoTermo, Instant consentimentoEm, StatusSubmissao status, Instant dataEnvio,
        Instant atualizadaEm) {

    public record Video(String url, String formato, int duracaoSeg, double tamanhoMB) {
    }

    static SubmissaoResposta de(Submissao s, String urlVideo) {
        return new SubmissaoResposta(s.getId(), s.getPalavra(), s.getAcepcao(), s.getExemplo(),
                s.getClasseGramatical(), s.getAssunto(), s.getRegiao(), s.getConfiguracaoMao(),
                new Video(urlVideo, s.getVideoFormato(), s.getVideoDuracaoSeg(), s.getVideoTamanhoMB()),
                s.isSinalizanteMenor(), s.getVersaoTermo(), s.getConsentimentoEm(), s.getStatus(), s.getDataEnvio(),
                s.getAtualizadaEm());
    }
}
