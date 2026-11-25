package br.udesc.dicionariolibras.submissoes.infra;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.submissoes.dominio.Regiao;

public interface RegiaoRepository extends JpaRepository<Regiao, Long> {

    Optional<Regiao> findByUf(String uf);
}
