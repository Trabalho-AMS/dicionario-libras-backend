package br.udesc.dicionariolibras.submissoes.http;

import org.springframework.web.multipart.MultipartFile;

import br.udesc.dicionariolibras.compartilhado.aplicacao.ArquivoRecebido;
import br.udesc.dicionariolibras.dicionario.aplicacao.DadosVerbete;
import br.udesc.dicionariolibras.submissoes.aplicacao.DadosSubmissao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Campos do formulário multipart de envio (e de alteração) de um sinal. */
record FormularioSubmissao(
        @NotBlank(message = "Informe a palavra") @Size(max = 120, message = "A palavra tem no máximo 120 caracteres") String palavra,
        @NotBlank(message = "Descreva a acepção da palavra") @Size(max = 1000, message = "A acepção tem no máximo 1000 caracteres") String acepcao,
        @Size(max = 500, message = "O exemplo tem no máximo 500 caracteres") String exemplo,
        @Size(max = 40) String classeGramatical,
        @NotNull(message = "Escolha o assunto") Long assuntoId,
        Long regiaoId,
        @NotNull(message = "Escolha a configuração de mão") Long configuracaoMaoId,
        @Positive(message = "Informe a duração do vídeo") @Max(value = 600, message = "Duração inválida") Integer duracaoSeg,
        Boolean aceiteTermo,
        Boolean sinalizanteMenor,
        MultipartFile video,
        MultipartFile termoResponsavel) {

    DadosSubmissao paraDados() {
        return new DadosSubmissao(
                new DadosVerbete(palavra, acepcao, vazioParaNulo(exemplo), vazioParaNulo(classeGramatical), assuntoId),
                regiaoId, configuracaoMaoId, ArquivoRecebido.de(video), duracaoSeg, Boolean.TRUE.equals(aceiteTermo),
                Boolean.TRUE.equals(sinalizanteMenor),
                ArquivoRecebido.de(termoResponsavel));
    }

    private static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
