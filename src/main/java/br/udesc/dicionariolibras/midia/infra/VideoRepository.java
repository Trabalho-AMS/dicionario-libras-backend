package br.udesc.dicionariolibras.midia.infra;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.udesc.dicionariolibras.midia.dominio.Video;

public interface VideoRepository extends JpaRepository<Video, UUID> {
}
