package br.udesc.dicionariolibras.midia.aplicacao;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.dicionariolibras.compartilhado.aplicacao.ArquivoRecebido;
import static br.udesc.dicionariolibras.compartilhado.aplicacao.Transacoes.aoDesfazer;
import static br.udesc.dicionariolibras.compartilhado.aplicacao.Transacoes.depoisDeConfirmar;
import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.midia.dominio.Video;
import br.udesc.dicionariolibras.midia.infra.VideoRepository;
import lombok.RequiredArgsConstructor;

/** Interface pública do módulo mídia: upload, consulta e remoção de vídeos. */
@Service
@RequiredArgsConstructor
@Transactional
public class MidiaService {

    private static final Duration VALIDADE_URL = Duration.ofHours(1);

    private final VideoRepository videos;
    private final ArmazenamentoDeArquivos armazenamento;

    /**
     * Valida e guarda o vídeo do sinal (RF04). A duração é lida pelo navegador e informada no envio;
     * formato e tamanho são conferidos aqui.
     */
    public UUID enviar(ArquivoRecebido arquivo, int duracaoSeg) {
        if (arquivo == null) {
            throw new RegraDeNegocioException("Envie o vídeo do sinal");
        }
        String formato = formatoDe(arquivo);
        String chave = "videos/" + UUID.randomUUID() + "." + formato;
        Video video = new Video(chave, formato, duracaoSeg, arquivo.tamanhoMB());
        video.motivoInvalido().ifPresent(motivo -> {
            throw new RegraDeNegocioException(motivo);
        });

        armazenamento.salvar(chave, arquivo.abrir(), arquivo.tamanhoBytes(), arquivo.contentType());
        // Se a transação falhar depois do upload, o arquivo não fica órfão no bucket
        aoDesfazer(() -> armazenamento.remover(chave));
        return videos.save(video).getId();
    }

    @Transactional(readOnly = true)
    public VideoResumo resumo(UUID videoId) {
        Video v = buscar(videoId);
        String url = armazenamento.urlTemporaria(v.getUrl(), VALIDADE_URL).toString();
        return new VideoResumo(v.getId(), url, v.getFormato(), v.getDuracaoSeg(), v.getTamanhoMB());
    }

    /** Apaga o registro agora e o arquivo do bucket depois que a transação for confirmada (LGPD). */
    public void remover(UUID videoId) {
        videos.findById(videoId).ifPresent(v -> {
            videos.delete(v);
            depoisDeConfirmar(() -> armazenamento.remover(v.getUrl()));
        });
    }

    private Video buscar(UUID id) {
        return videos.findById(id).orElseThrow(() -> new NaoEncontradoException("Vídeo não encontrado"));
    }

    private static String formatoDe(ArquivoRecebido arquivo) {
        String tipo = arquivo.contentType() == null ? "" : arquivo.contentType().toLowerCase(Locale.ROOT);
        if (tipo.startsWith("video/")) {
            return tipo.substring("video/".length());
        }
        String nome = arquivo.nome() == null ? "" : arquivo.nome();
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? tipo : nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }
}
