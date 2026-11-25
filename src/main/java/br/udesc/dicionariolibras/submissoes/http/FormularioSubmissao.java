package br.udesc.dicionariolibras.submissoes.http;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.springframework.web.multipart.MultipartFile;

import br.udesc.dicionariolibras.submissoes.aplicacao.ArquivoEnviado;
import br.udesc.dicionariolibras.submissoes.aplicacao.NovaSubmissao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Campos do formulário multipart de envio (e de alteração) de um sinal. */
record FormularioSubmissao(
        @NotBlank(message = "Informe a palavra") @Size(max = 120, message = "A palavra tem no máximo 120 caracteres") String palavra,
        @NotBlank(message = "Descreva a acepção da palavra") @Size(max = 1000, message = "A acepção tem no máximo 1000 caracteres") String acepcao,
        @Size(max = 500, message = "O exemplo tem no máximo 500 caracteres") String exemplo,
        @Size(max = 500, message = "O exemplo em Libras tem no máximo 500 caracteres") String exemploLibras,
        @Size(max = 40) String classeGramatical,
        @Size(max = 120, message = "A origem tem no máximo 120 caracteres") String origem,
        @NotBlank(message = "Escolha o assunto") @Size(max = 60) String assunto,
        @Size(min = 2, max = 2, message = "Região inválida") String regiao,
        @NotBlank(message = "Escolha a configuração de mão") @Size(max = 10) String configuracaoMao,
        Integer duracaoSeg,
        Boolean aceiteTermo,
        Boolean sinalizanteMenor,
        MultipartFile video,
        MultipartFile termoResponsavel) {

    NovaSubmissao paraDados() {
        return new NovaSubmissao(palavra, acepcao, vazioParaNulo(exemplo), vazioParaNulo(exemploLibras),
                vazioParaNulo(classeGramatical), vazioParaNulo(origem), assunto, vazioParaNulo(regiao), configuracaoMao, arquivo(video), duracaoSeg, Boolean.TRUE.equals(aceiteTermo),
                Boolean.TRUE.equals(sinalizanteMenor), arquivo(termoResponsavel));
    }

    private static ArquivoEnviado arquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }
        try {
            return new ArquivoEnviado(arquivo.getOriginalFilename(), arquivo.getContentType(), arquivo.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler o arquivo enviado", e);
        }
    }

    private static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
