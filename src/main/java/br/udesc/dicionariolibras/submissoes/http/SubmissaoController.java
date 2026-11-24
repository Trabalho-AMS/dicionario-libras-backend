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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import br.udesc.dicionariolibras.identidade.http.UsuarioLogado;
import br.udesc.dicionariolibras.submissoes.aplicacao.SubmissaoResposta;
import br.udesc.dicionariolibras.submissoes.aplicacao.SubmissaoService;
import br.udesc.dicionariolibras.submissoes.dominio.StatusSubmissao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * CRUD das submissões e decisões da curadoria (UC01). O usuário vem do cabeçalho {@code X-Usuario-Id}.
 */
@RestController
@RequestMapping("/api/submissoes")
@RequiredArgsConstructor
@Tag(name = "Submissões")
class SubmissaoController {

    private final SubmissaoService submissoes;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia um novo sinal com vídeo e consentimento; a submissão fica Em análise")
    SubmissaoResposta criar(@Parameter(hidden = true) @UsuarioLogado Usuario usuario,
            @Valid @ModelAttribute FormularioSubmissao formulario) {
        return submissoes.criar(usuario, formulario.paraDados());
    }

    @GetMapping("/minhas")
    @Operation(summary = "Submissões do usuário logado")
    List<SubmissaoResposta> minhas(@Parameter(hidden = true) @UsuarioLogado Usuario usuario) {
        return submissoes.minhas(usuario);
    }

    @GetMapping
    @Operation(summary = "Fila da curadoria (sem status, lista todas)")
    List<SubmissaoResposta> fila(@Parameter(hidden = true) @UsuarioLogado Usuario usuario,
            @RequestParam(required = false) StatusSubmissao status) {
        return submissoes.fila(usuario, status);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de uma submissão (autor ou curador)")
    SubmissaoResposta detalhar(@Parameter(hidden = true) @UsuarioLogado Usuario usuario, @PathVariable UUID id) {
        return submissoes.detalhar(id, usuario);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Altera a submissão (o vídeo é opcional); com ajuste solicitado, ela volta para a fila")
    SubmissaoResposta alterar(@Parameter(hidden = true) @UsuarioLogado Usuario usuario, @PathVariable UUID id,
            @Valid @ModelAttribute FormularioSubmissao formulario) {
        return submissoes.alterar(id, usuario, formulario.paraDados());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui a submissão não aprovada, com o vídeo e o consentimento")
    void excluir(@Parameter(hidden = true) @UsuarioLogado Usuario usuario, @PathVariable UUID id) {
        submissoes.excluir(id, usuario);
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Curador aprova e o sinal é publicado")
    SubmissaoResposta aprovar(@Parameter(hidden = true) @UsuarioLogado Usuario usuario, @PathVariable UUID id) {
        return submissoes.aprovar(id, usuario);
    }

    @PostMapping("/{id}/solicitar-ajuste")
    @Operation(summary = "Curador pede ajuste, com justificativa")
    SubmissaoResposta solicitarAjuste(@Parameter(hidden = true) @UsuarioLogado Usuario usuario, @PathVariable UUID id,
            @RequestBody Decisao decisao) {
        return submissoes.solicitarAjuste(id, usuario, decisao.motivo());
    }

    @PostMapping("/{id}/rejeitar")
    @Operation(summary = "Curador rejeita, com justificativa")
    SubmissaoResposta rejeitar(@Parameter(hidden = true) @UsuarioLogado Usuario usuario, @PathVariable UUID id,
            @RequestBody Decisao decisao) {
        return submissoes.rejeitar(id, usuario, decisao.motivo());
    }

    record Decisao(String motivo) {
    }
}
