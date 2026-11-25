package br.udesc.dicionariolibras.submissoes.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.udesc.dicionariolibras.submissoes.dominio.Submissao;
import br.udesc.dicionariolibras.submissoes.dominio.SubmissaoRepository;
import lombok.RequiredArgsConstructor;

/** Implementação do repositório de submissões com JPA e PostgreSQL. */
@Repository
@RequiredArgsConstructor
class SubmissaoRepositoryJpa implements SubmissaoRepository {

    private final SubmissaoJpaRepository jpa;

    @Override
    public Submissao salvar(Submissao submissao) {
        return jpa.save(submissao);
    }

    @Override
    public Optional<Submissao> buscarPorId(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Submissao> listarTodas() {
        return jpa.findAllByOrderByDataEnvioDesc();
    }

    @Override
    public void excluir(Submissao submissao) {
        jpa.delete(submissao);
    }
}
