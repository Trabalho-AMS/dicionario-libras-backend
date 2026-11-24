package br.udesc.dicionariolibras.submissoes.http;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.dicionariolibras.submissoes.aplicacao.SubmissaoResposta;
import br.udesc.dicionariolibras.submissoes.aplicacao.SubmissaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** CRUD das submissões de sinais com vídeo. */
@RestController
@RequestMapping("/api/submissoes")
@RequiredArgsConstructor
@Tag(name = "Submissões")
class SubmissaoController {

    private final SubmissaoService submissoes;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia um novo sinal com vídeo e consentimento; a submissão fica Em análise")
    SubmissaoResposta criar(@Valid @ModelAttribute FormularioSubmissao formulario) {
        return submissoes.criar(formulario.paraDados());
    }

    @GetMapping
    @Operation(summary = "Lista as submissões")
    List<SubmissaoResposta> listar() {
        return submissoes.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de uma submissão, com link temporário do vídeo")
    SubmissaoResposta detalhar(@PathVariable UUID id) {
        return submissoes.detalhar(id);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Altera a submissão; o vídeo é opcional (sem ele, o atual é mantido)")
    SubmissaoResposta alterar(@PathVariable UUID id, @Valid @ModelAttribute FormularioSubmissao formulario) {
        return submissoes.alterar(id, formulario.paraDados());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui a submissão com o vídeo e o consentimento")
    void excluir(@PathVariable UUID id) {
        submissoes.excluir(id);
    }
}
