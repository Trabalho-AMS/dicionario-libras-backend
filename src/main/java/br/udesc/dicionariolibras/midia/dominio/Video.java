package br.udesc.dicionariolibras.midia.dominio;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Vídeo de um sinal. O arquivo fica no bucket S3; aqui fica a chave e os metadados (RF04). */
@Entity
@Table(name = "video")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video {

    public static final Set<String> FORMATOS_ACEITOS = Set.of("mp4", "webm");
    public static final int DURACAO_MAXIMA_SEG = 30;
    public static final double TAMANHO_MAXIMO_MB = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Chave do objeto no bucket. A URL de acesso é gerada pelo armazenamento. */
    @Column(nullable = false, unique = true, length = 255)
    private String url;

    @Column(nullable = false, length = 10)
    private String formato;

    @Column(name = "duracao_seg", nullable = false)
    private int duracaoSeg;

    @Column(name = "tamanho_mb", nullable = false)
    private double tamanhoMB;

    @Column(nullable = false)
    private boolean transcodificado;

    public Video(String url, String formato, int duracaoSeg, double tamanhoMB) {
        this.url = url;
        this.formato = formato == null ? null : formato.toLowerCase();
        this.duracaoSeg = duracaoSeg;
        this.tamanhoMB = tamanhoMB;
        this.transcodificado = false;
    }

    public boolean validar() {
        return motivoInvalido().isEmpty();
    }

    /** Explica como corrigir o vídeo, conforme as diretrizes de acessibilidade. */
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

    /** Marca o vídeo como convertido para o formato de exibição (MP4). */
    public void transcodificar() {
        this.formato = "mp4";
        this.transcodificado = true;
    }
}
