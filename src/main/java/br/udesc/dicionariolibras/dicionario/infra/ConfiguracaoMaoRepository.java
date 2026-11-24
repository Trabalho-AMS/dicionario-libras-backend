package br.udesc.dicionariolibras.dicionario.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.dicionario.dominio.ConfiguracaoMao;

public interface ConfiguracaoMaoRepository extends JpaRepository<ConfiguracaoMao, Long> {
}
