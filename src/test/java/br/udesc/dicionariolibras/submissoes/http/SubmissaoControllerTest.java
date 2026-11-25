package br.udesc.dicionariolibras.submissoes.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import br.udesc.dicionariolibras.TestcontainersConfig;
import br.udesc.dicionariolibras.submissoes.dominio.ArmazenamentoDeArquivos;

/** CRUD das submissões pela API, com o PostgreSQL do Testcontainers e o bucket em memória. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestcontainersConfig.class, SubmissaoControllerTest.BucketEmMemoria.class })
class SubmissaoControllerTest {

    @Autowired MockMvc mvc;
    @Autowired Map<String, byte[]> bucket;

    @Test
    void crudCompleto() throws Exception {
        // Criar: o vídeo vai para o bucket e a submissão fica Em análise
        String id = criar("Escola", 12);
        assertThat(bucket.keySet()).anyMatch(k -> k.startsWith("videos/"));

        // Listar e detalhar
        mvc.perform(get("/api/submissoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + id + "')].palavra").value("Escola"));
        mvc.perform(get("/api/submissoes/{id}", id))
                .andExpect(jsonPath("$.video.url").exists())
                .andExpect(jsonPath("$.versaoTermo").value("1.0"));

        // Alterar sem trocar o vídeo
        mvc.perform(formulario(multipart("/api/submissoes/{id}", id).with(r -> {
            r.setMethod("PUT");
            return r;
        }), "Escola", 12, false))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.acepcao").value("Acepção corrigida"))
                .andExpect(jsonPath("$.atualizadaEm").exists());

        // Excluir apaga a submissão e o vídeo do bucket
        long videosAntes = bucket.keySet().stream().filter(k -> k.startsWith("videos/")).count();
        mvc.perform(delete("/api/submissoes/{id}", id)).andExpect(status().isNoContent());
        assertThat(bucket.keySet().stream().filter(k -> k.startsWith("videos/")).count()).isEqualTo(videosAntes - 1);
        mvc.perform(get("/api/submissoes/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void recusaVideoLongoComMensagemDeComoCorrigir() throws Exception {
        mvc.perform(formulario(multipart("/api/submissoes"), "Livro", 45, true))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("O vídeo tem 45 s; o limite é 30 s"));
    }

    @Test
    void exigeOAceiteDoTermo() throws Exception {
        MockMultipartHttpServletRequestBuilder semAceite = formulario(multipart("/api/submissoes"), "Mesa", 5, true);
        semAceite.with(r -> {
            r.removeParameter("aceiteTermo");
            return r;
        });
        mvc.perform(semAceite)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("É preciso aceitar o termo de uso de imagem para enviar o vídeo"));
    }

    private String criar(String palavra, int duracao) throws Exception {
        String corpo = mvc.perform(formulario(multipart("/api/submissoes"), palavra, duracao, true))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    private MockMultipartHttpServletRequestBuilder formulario(MockMultipartHttpServletRequestBuilder req,
            String palavra, int duracao, boolean comVideo) {
        if (comVideo) {
            req.file(new MockMultipartFile("video", "sinal.mp4", "video/mp4", new byte[] { 1, 2, 3 }));
        }
        req.param("palavra", palavra)
                .param("acepcao", comVideo ? "Acepção original" : "Acepção corrigida")
                .param("assunto", "Educação")
                .param("regiao", "SC")
                .param("configuracaoMao", "CM01")
                .param("duracaoSeg", String.valueOf(duracao))
                .param("aceiteTermo", "true");
        return req;
    }

    /** Substitui o bucket S3 por um mapa em memória. */
    @TestConfiguration
    static class BucketEmMemoria {

        @Bean
        Map<String, byte[]> bucket() {
            return new ConcurrentHashMap<>();
        }

        @Bean
        @Primary
        ArmazenamentoDeArquivos armazenamentoEmMemoria(Map<String, byte[]> bucket) {
            return new ArmazenamentoDeArquivos() {
                @Override
                public void salvar(String chave, byte[] conteudo, String contentType) {
                    bucket.put(chave, conteudo);
                }

                @Override
                public URL urlTemporaria(String chave, Duration validade) {
                    try {
                        return URI.create("http://bucket.teste/" + chave).toURL();
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }

                @Override
                public void remover(String chave) {
                    bucket.remove(chave);
                }
            };
        }
    }
}
