package br.udesc.dicionariolibras.dicionario.infra;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.dicionario.dominio.Sinal;

public interface SinalRepository extends JpaRepository<Sinal, UUID> {
}
