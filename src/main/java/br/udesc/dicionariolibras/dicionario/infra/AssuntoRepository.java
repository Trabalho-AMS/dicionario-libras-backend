package br.udesc.dicionariolibras.dicionario.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.dicionario.dominio.Assunto;

public interface AssuntoRepository extends JpaRepository<Assunto, Long> {
}
