package br.udesc.dicionariolibras.privacidade.aplicacao;

import java.time.Instant;
import java.util.UUID;

public record ConsentimentoResumo(UUID id, String versaoTermo, Instant dataHora, boolean sinalizanteMenor,
        boolean termoResponsavelEnviado, boolean revogado) {
}
