package br.udesc.dicionariolibras.dicionario.infra;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.udesc.dicionariolibras.dicionario.dominio.Verbete;

public interface VerbeteRepository extends JpaRepository<Verbete, UUID> {

    List<Verbete> findByPalavraContainingIgnoreCaseOrderByPalavra(String palavra);

    Optional<Verbete> findFirstByPalavraIgnoreCase(String palavra);

    @Query("""
            select distinct v from Verbete v join v.sinais s
            where s.publicado = true and lower(v.palavra) like lower(concat('%', :termo, '%'))
            order by v.palavra""")
    List<Verbete> buscarPublicados(@Param("termo") String termo);
}
