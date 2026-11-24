package br.udesc.dicionariolibras.identidade.http;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.dicionariolibras.identidade.aplicacao.UsuarioResumo;
import br.udesc.dicionariolibras.identidade.aplicacao.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários")
class UsuarioController {

    private final UsuarioService usuarios;

    @GetMapping
    @Operation(summary = "Lista os usuários (de demonstração, enquanto não há login)")
    List<UsuarioResumo> listar() {
        return usuarios.listar();
    }
}
