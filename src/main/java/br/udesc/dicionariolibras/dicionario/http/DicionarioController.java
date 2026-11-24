package br.udesc.dicionariolibras.dicionario.http;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.udesc.dicionariolibras.dicionario.aplicacao.ApoioResposta;
import br.udesc.dicionariolibras.dicionario.aplicacao.DicionarioService;
import br.udesc.dicionariolibras.dicionario.aplicacao.DicionarioService.VerbetePublicado;
import br.udesc.dicionariolibras.dicionario.aplicacao.SinalResumo;
import br.udesc.dicionariolibras.dicionario.aplicacao.SinalResumo.VerbeteResumo;
import br.udesc.dicionariolibras.midia.aplicacao.MidiaService;
import br.udesc.dicionariolibras.midia.aplicacao.VideoResumo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/** Consulta pública do dicionário (não exige usuário). */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Dicionário")
class DicionarioController {

    private final DicionarioService dicionario;
    private final MidiaService midia;

    @GetMapping("/apoio")
    @Operation(summary = "Regiões, assuntos e configurações de mão")
    ApoioResposta apoio() {
        return dicionario.apoio();
    }

    @GetMapping("/verbetes")
    @Operation(summary = "Busca verbetes publicados pela palavra")
    List<VerbeteResposta> buscar(@RequestParam(defaultValue = "") String busca) {
        return dicionario.buscar(busca).stream().map(this::resposta).toList();
    }

    @GetMapping("/verbetes/{id}")
    @Operation(summary = "Verbete publicado com o sinal padrão e as variações regionais")
    VerbeteResposta verbete(@PathVariable UUID id) {
        return resposta(dicionario.verbetePublicado(id));
    }

    private VerbeteResposta resposta(VerbetePublicado v) {
        return new VerbeteResposta(v.verbete(),
                v.sinais().stream().map(s -> new SinalComVideo(s, midia.resumo(s.videoId()))).toList());
    }

    record VerbeteResposta(VerbeteResumo verbete, List<SinalComVideo> sinais) {
    }

    record SinalComVideo(SinalResumo sinal, VideoResumo video) {
    }
}
