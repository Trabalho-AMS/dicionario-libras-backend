package br.udesc.dicionariolibras.dicionario.infra;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.dicionario.dominio.Verbete;

public interface VerbeteRepository extends JpaRepository<Verbete, UUID> {

    List<Verbete> findByPalavraContainingIgnoreCaseOrderByPalavra(String palavra);
}
