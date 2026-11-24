package br.udesc.dicionariolibras.dicionario.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.dicionariolibras.dicionario.aplicacao.ApoioResposta;
import br.udesc.dicionariolibras.dicionario.aplicacao.DicionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/apoio")
@RequiredArgsConstructor
@Tag(name = "Dicionário")
class DicionarioController {

    private final DicionarioService dicionario;

    @GetMapping
    @Operation(summary = "Regiões, assuntos e configurações de mão usados no formulário")
    ApoioResposta apoio() {
        return dicionario.apoio();
    }
}
