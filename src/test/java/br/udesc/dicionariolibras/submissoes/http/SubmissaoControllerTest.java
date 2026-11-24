package br.udesc.dicionariolibras.submissoes.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import br.udesc.dicionariolibras.TestcontainersConfig;
import br.udesc.dicionariolibras.dicionario.infra.AssuntoRepository;
import br.udesc.dicionariolibras.dicionario.infra.ConfiguracaoMaoRepository;
import br.udesc.dicionariolibras.dicionario.infra.RegiaoRepository;
import br.udesc.dicionariolibras.midia.aplicacao.ArmazenamentoDeArquivos;

/** Fluxo completo do UC01 pela API: enviar, pedir ajuste, corrigir, aprovar e encontrar na busca. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({ TestcontainersConfig.class, SubmissaoControllerTest.BucketEmMemoria.class })
class SubmissaoControllerTest {

    static final String COLABORADORA = "11111111-1111-1111-1111-111111111111";
    static final String OUTRO_COLABORADOR = "22222222-2222-2222-2222-222222222222";
    static final String CURADORA = "33333333-3333-3333-3333-333333333333";

    @Autowired MockMvc mvc;
    @Autowired AssuntoRepository assuntos;
    @Autowired RegiaoRepository regioes;
    @Autowired ConfiguracaoMaoRepository configuracoes;
    @Autowired Map<String, byte[]> bucket;

    @Test
    void fluxoCompletoDoUc01() throws Exception {
        String id = criar("Escola", 12);

        // O vídeo foi para o bucket e a submissão aparece para a autora
        assertThat(bucket.keySet()).anyMatch(k -> k.startsWith("videos/"));
        mvc.perform(get("/api/submissoes/minhas").header("X-Usuario-Id", COLABORADORA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + id + "')].status").value("EM_ANALISE"));

        // Colaborador não acessa a fila; a curadora acessa
        mvc.perform(get("/api/submissoes").header("X-Usuario-Id", COLABORADORA)).andExpect(status().isForbidden());
        mvc.perform(get("/api/submissoes").param("status", "EM_ANALISE").header("X-Usuario-Id", CURADORA))
                .andExpect(status().isOk());

        // Pedir ajuste exige justificativa
        mvc.perform(post("/api/submissoes/{id}/solicitar-ajuste", id).header("X-Usuario-Id", CURADORA)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"\"}"))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(post("/api/submissoes/{id}/solicitar-ajuste", id).header("X-Usuario-Id", CURADORA)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Enquadre as mãos\"}"))
                .andExpect(jsonPath("$.status").value("AJUSTE_SOLICITADO"))
                .andExpect(jsonPath("$.justificativa").value("Enquadre as mãos"));

        // A autora corrige (sem trocar o vídeo) e a submissão volta para a fila
        mvc.perform(formulario(multipart("/api/submissoes/{id}", id).with(r -> {
            r.setMethod("PUT");
            return r;
        }), "Escola", 12, false).header("X-Usuario-Id", COLABORADORA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.sinal.verbete.acepcao").value("Acepção corrigida"));

        // Aprovada, o sinal é publicado e aparece na busca
        mvc.perform(post("/api/submissoes/{id}/aprovar", id).header("X-Usuario-Id", CURADORA))
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.sinal.publicado").value(true));
        mvc.perform(get("/api/verbetes").param("busca", "esco"))
                .andExpect(jsonPath("$[0].verbete.palavra").value("Escola"))
                .andExpect(jsonPath("$[0].sinais[0].video.url").exists());

        // Submissão aprovada não pode ser excluída
        mvc.perform(delete("/api/submissoes/{id}", id).header("X-Usuario-Id", COLABORADORA))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void excluirApagaOVideoDoBucket() throws Exception {
        String id = criar("Casa", 5);
        long videosAntes = bucket.keySet().stream().filter(k -> k.startsWith("videos/")).count();

        mvc.perform(delete("/api/submissoes/{id}", id).header("X-Usuario-Id", OUTRO_COLABORADOR))
                .andExpect(status().isUnprocessableContent());
        mvc.perform(delete("/api/submissoes/{id}", id).header("X-Usuario-Id", COLABORADORA))
                .andExpect(status().isNoContent());

        assertThat(bucket.keySet().stream().filter(k -> k.startsWith("videos/")).count()).isEqualTo(videosAntes - 1);
        mvc.perform(get("/api/submissoes/{id}", id).header("X-Usuario-Id", COLABORADORA))
                .andExpect(status().isNotFound());
    }

    @Test
    void recusaVideoLongoComMensagemDeComoCorrigir() throws Exception {
        mvc.perform(formulario(multipart("/api/submissoes"), "Livro", 45, true).header("X-Usuario-Id", COLABORADORA))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.mensagem").value("O vídeo tem 45 s; o limite é 30 s"));
    }

    @Test
    void exigeUsuario() throws Exception {
        mvc.perform(get("/api/submissoes/minhas")).andExpect(status().isUnauthorized());
    }

    private String criar(String palavra, int duracao) throws Exception {
        String corpo = mvc.perform(formulario(multipart("/api/submissoes"), palavra, duracao, true)
                .header("X-Usuario-Id", COLABORADORA))
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
                .param("assuntoId", assuntos.findAll().get(0).getId().toString())
                .param("regiaoId", regioes.findAll().get(0).getId().toString())
                .param("configuracaoMaoId", configuracoes.findAll().get(0).getId().toString())
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
                public void salvar(String chave, InputStream conteudo, long tamanhoBytes, String contentType) {
                    try (conteudo) {
                        bucket.put(chave, conteudo.readAllBytes());
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
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
