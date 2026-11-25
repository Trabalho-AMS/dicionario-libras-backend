package br.udesc.dicionariolibras.midia.dominio;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Vídeo do sinal. O arquivo fica no bucket; {@code url} guarda a chave dele. */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video {

    public static final Set<String> FORMATOS_ACEITOS = Set.of("mp4", "webm");
    public static final int DURACAO_MAXIMA_SEG = 30;
    public static final double TAMANHO_MAXIMO_MB = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false, length = 10)
    private String formato;

    @Column(name = "duracao_seg", nullable = false)
    private int duracaoSeg;

    @Column(name = "tamanho_mb", nullable = false)
    private double tamanhoMB;

    public Video(String url, String formato, int duracaoSeg, double tamanhoMB) {
        this.url = url;
        this.formato = formato;
        this.duracaoSeg = duracaoSeg;
        this.tamanhoMB = tamanhoMB;
    }

    /** RF04: MP4 ou WebM, até 30 s e 50 MB. */
    public boolean validar() {
        return motivoInvalido().isEmpty();
    }

    /** Explica como corrigir o vídeo (diretriz de acessibilidade). */
    public Optional<String> motivoInvalido() {
        if (formato == null || !FORMATOS_ACEITOS.contains(formato)) {
            return Optional.of("Formato \"" + formato + "\" não aceito; envie MP4 ou WebM");
        }
        if (duracaoSeg <= 0) {
            return Optional.of("Não foi possível ler a duração do vídeo");
        }
        if (duracaoSeg > DURACAO_MAXIMA_SEG) {
            return Optional.of("O vídeo tem " + duracaoSeg + " s; o limite é " + DURACAO_MAXIMA_SEG + " s");
        }
        if (tamanhoMB > TAMANHO_MAXIMO_MB) {
            return Optional.of(String.format("O vídeo tem %.1f MB; o limite é %.0f MB", tamanhoMB, TAMANHO_MAXIMO_MB));
        }
        return Optional.empty();
    }
}
