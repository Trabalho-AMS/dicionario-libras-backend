package br.udesc.dicionariolibras.arquitetura;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Garante a regra das camadas de cada módulo: cada pasta só fala com a vizinha.
 *
 * <pre>
 * http → aplicacao → dominio ← infra
 * </pre>
 */
class CamadasTest {

    private static final Path MODULOS = Path.of("src/main/java/br/udesc/dicionariolibras");
    private static final Pattern IMPORT = Pattern.compile("^import (?:static )?br\\.udesc\\.dicionariolibras\\.(\\w+)\\.(\\w+)\\.",
            Pattern.MULTILINE);

    /** Camadas que cada camada pode importar (de qualquer módulo), além dela mesma. */
    private static final Map<String, Set<String>> PERMITIDAS = Map.of(
            "http", Set.of("aplicacao"),
            "aplicacao", Set.of("dominio"),
            "dominio", Set.of(),
            "infra", Set.of("dominio"));

    @Test
    void cadaPastaSoFalaComAVizinha() throws IOException {
        List<String> violacoes = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(MODULOS)) {
            for (Path arquivo : arquivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                Path relativo = MODULOS.relativize(arquivo);
                if (relativo.getNameCount() < 3 || relativo.getName(0).toString().equals("compartilhado")) {
                    continue; // classe principal e pacote compartilhado (configuração transversal)
                }
                String camada = relativo.getName(1).toString();
                Matcher m = IMPORT.matcher(Files.readString(arquivo));
                while (m.find()) {
                    String modulo = m.group(1);
                    String camadaImportada = m.group(2);
                    boolean permitido = camadaImportada.equals(camada)
                            || PERMITIDAS.getOrDefault(camada, Set.of()).contains(camadaImportada)
                            // exceções de negócio comuns ficam no domínio compartilhado
                            || (modulo.equals("compartilhado") && camadaImportada.equals("dominio")
                                    && !camada.equals("http"));
                    if (!permitido) {
                        violacoes.add(relativo + " (" + camada + ") importa " + modulo + "." + camadaImportada);
                    }
                }
            }
        }
        assertThat(violacoes).as("imports que pulam camadas").isEmpty();
    }
}
