package br.udesc.dicionariolibras.submissoes.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório das submissões. A camada de aplicação depende só desta interface;
 * a implementação com JPA/PostgreSQL fica em {@code infra}.
 */
public interface SubmissaoRepository {

    Submissao salvar(Submissao submissao);

    Optional<Submissao> buscarPorId(UUID id);

    /** Todas as submissões, das mais recentes para as mais antigas. */
    List<Submissao> listarTodas();

    void excluir(Submissao submissao);
}
