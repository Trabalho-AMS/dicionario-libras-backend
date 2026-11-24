package br.udesc.dicionariolibras.submissoes.infra;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.submissoes.dominio.StatusSubmissao;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;

public interface SubmissaoRepository extends JpaRepository<Submissao, UUID> {

    List<Submissao> findByStatusOrderByDataEnvio(StatusSubmissao status);

    List<Submissao> findByAutorIdOrderByDataEnvioDesc(UUID autorId);

    List<Submissao> findAllByOrderByDataEnvioDesc();
}
