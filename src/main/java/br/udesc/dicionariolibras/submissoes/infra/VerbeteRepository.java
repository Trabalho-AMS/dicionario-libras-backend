package br.udesc.dicionariolibras.submissoes.infra;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.submissoes.dominio.Verbete;

public interface VerbeteRepository extends JpaRepository<Verbete, UUID> {

    Optional<Verbete> findFirstByPalavraIgnoreCase(String palavra);
}
