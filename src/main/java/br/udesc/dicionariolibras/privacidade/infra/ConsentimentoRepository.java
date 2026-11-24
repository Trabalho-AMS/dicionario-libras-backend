package br.udesc.dicionariolibras.privacidade.infra;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;

public interface ConsentimentoRepository extends JpaRepository<Consentimento, UUID> {
}
