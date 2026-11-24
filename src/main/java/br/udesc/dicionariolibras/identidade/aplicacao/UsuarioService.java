package br.udesc.dicionariolibras.identidade.aplicacao;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import br.udesc.dicionariolibras.identidade.infra.UsuarioRepository;
import lombok.RequiredArgsConstructor;

/** Interface pública do módulo identidade. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

    /** O protótipo ainda não tem login: as submissões são feitas pela colaboradora de demonstração (migração V3). */
    static final UUID USUARIO_PADRAO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final UsuarioRepository usuarios;

    public Usuario usuarioAtual() {
        return buscar(USUARIO_PADRAO);
    }

    public Usuario buscar(UUID id) {
        return usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
    }

    /** Resumo de um usuário para outros módulos; devolve null se o id for nulo. */
    public UsuarioResumo resumo(UUID id) {
        return id == null ? null : usuarios.findById(id).map(UsuarioResumo::de).orElse(null);
    }
}
