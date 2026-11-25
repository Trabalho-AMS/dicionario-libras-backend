package br.udesc.dicionariolibras.dicionario.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.dicionario.dominio.ConfiguracaoMao;

public interface ConfiguracaoMaoRepository extends JpaRepository<ConfiguracaoMao, Long> {

    Optional<ConfiguracaoMao> findByCodigo(String codigo);
}
