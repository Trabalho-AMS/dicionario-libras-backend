package br.udesc.dicionariolibras.submissoes.dominio;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;

/**
 * Sinal enviado por um colaborador: campos do verbete, região, vídeo e consentimento do sinalizante.
 * O vídeo fica no bucket; aqui fica a chave do arquivo e os metadados.
 */
@Entity
@Table(name = "submissao")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submissao {

    public static final Set<String> FORMATOS_ACEITOS = Set.of("mp4", "webm");
    public static final int DURACAO_MAXIMA_SEG = 30;
    public static final double TAMANHO_MAXIMO_MB = 50;
    public static final String VERSAO_TERMO = "1.0";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String palavra;

    @Column(nullable = false, length = 1000)
    private String acepcao;

    @Column(length = 500)
    private String exemplo;

    @Column(name = "classe_gramatical", length = 40)
    private String classeGramatical;

    @Column(nullable = false, length = 60)
    private String assunto;

    /** UF da variação regional; nulo é o sinal padrão nacional. */
    @Column(length = 2)
    private String regiao;

    @Column(name = "configuracao_mao", nullable = false, length = 10)
    private String configuracaoMao;

    @Column(name = "video_chave", nullable = false)
    private String videoChave;

    @Column(name = "video_formato", nullable = false, length = 10)
    private String videoFormato;

    @Column(name = "video_duracao_seg", nullable = false)
    private int videoDuracaoSeg;

    @Column(name = "video_tamanho_mb", nullable = false)
    private double videoTamanhoMB;

    @Column(name = "versao_termo", nullable = false, length = 20)
    private String versaoTermo;

    @Column(name = "consentimento_em", nullable = false)
    private Instant consentimentoEm;

    @Column(name = "sinalizante_menor", nullable = false)
    private boolean sinalizanteMenor;

    /** Chave, no bucket, do termo assinado pelo responsável legal (sinalizante menor de 18 anos). */
    @Column(name = "termo_responsavel")
    private String termoResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSubmissao status;

    @Column(name = "data_envio", nullable = false)
    private Instant dataEnvio;

    @Column(name = "atualizada_em")
    private Instant atualizadaEm;

    /**
     * Cria a submissão "Em análise". Exige o aceite do termo de uso de imagem e, se o sinalizante for
     * menor de 18 anos, o termo do responsável legal (LGPD, art. 14).
     */
    public Submissao(DadosDoSinal dados, VideoDoSinal video, boolean aceiteTermo, boolean sinalizanteMenor,
            String termoResponsavel) {
        if (!aceiteTermo) {
            throw new RegraDeNegocioException("É preciso aceitar o termo de uso de imagem para enviar o vídeo");
        }
        if (sinalizanteMenor && (termoResponsavel == null || termoResponsavel.isBlank())) {
            throw new RegraDeNegocioException(
                    "Sinalizante menor de 18 anos: anexe o termo assinado pelo responsável legal (LGPD, art. 14)");
        }
        alterarDados(dados);
        trocarVideo(video);
        this.versaoTermo = VERSAO_TERMO;
        this.consentimentoEm = Instant.now();
        this.sinalizanteMenor = sinalizanteMenor;
        this.termoResponsavel = termoResponsavel;
        this.status = StatusSubmissao.EM_ANALISE;
        this.dataEnvio = Instant.now();
    }

    public void alterarDados(DadosDoSinal dados) {
        this.palavra = dados.palavra().trim();
        this.acepcao = dados.acepcao().trim();
        this.exemplo = dados.exemplo();
        this.classeGramatical = dados.classeGramatical();
        this.assunto = dados.assunto();
        this.regiao = dados.regiao();
        this.configuracaoMao = dados.configuracaoMao();
        if (id != null) {
            this.atualizadaEm = Instant.now();
        }
    }

    /** Troca o vídeo; ele já precisa ter passado em {@link #motivoVideoInvalido}. */
    public void trocarVideo(VideoDoSinal video) {
        motivoVideoInvalido(video.formato(), video.duracaoSeg(), video.tamanhoMB()).ifPresent(motivo -> {
            throw new RegraDeNegocioException(motivo);
        });
        this.videoChave = video.chave();
        this.videoFormato = video.formato();
        this.videoDuracaoSeg = video.duracaoSeg();
        this.videoTamanhoMB = video.tamanhoMB();
        if (id != null) {
            this.atualizadaEm = Instant.now();
        }
    }

    /** Regras do RF04, com uma mensagem que diz como corrigir (diretriz de acessibilidade). */
    public static Optional<String> motivoVideoInvalido(String formato, int duracaoSeg, double tamanhoMB) {
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

    /** Campos do verbete e do sinal informados pelo colaborador. */
    public record DadosDoSinal(String palavra, String acepcao, String exemplo, String classeGramatical,
            String assunto, String regiao, String configuracaoMao) {
    }

    /** Vídeo já guardado no bucket. */
    public record VideoDoSinal(String chave, String formato, int duracaoSeg, double tamanhoMB) {
    }
}
