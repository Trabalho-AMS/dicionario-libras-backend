package br.udesc.dicionariolibras.dicionario.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.dicionario.dominio.Regiao;

public interface RegiaoRepository extends JpaRepository<Regiao, Long> {
}
