package br.udesc.dicionariolibras.submissoes.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.submissoes.dominio.ConfiguracaoMao;

public interface ConfiguracaoMaoRepository extends JpaRepository<ConfiguracaoMao, Long> {

    Optional<ConfiguracaoMao> findByCodigo(String codigo);
}
