package br.udesc.dicionariolibras.submissoes.infra;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.submissoes.dominio.Submissao;

/** Consultas geradas pelo Spring Data JPA. Usada só pelo {@link SubmissaoRepositoryJpa}. */
interface SubmissaoJpaRepository extends JpaRepository<Submissao, UUID> {

    List<Submissao> findAllByOrderByDataEnvioDesc();
}
