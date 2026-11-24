package br.udesc.dicionariolibras.identidade.aplicacao;

import java.util.UUID;

import br.udesc.dicionariolibras.identidade.dominio.Perfil;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;

public record UsuarioResumo(UUID id, String nome, String email, Perfil perfil) {

    public static UsuarioResumo de(Usuario u) {
        return new UsuarioResumo(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }
}
