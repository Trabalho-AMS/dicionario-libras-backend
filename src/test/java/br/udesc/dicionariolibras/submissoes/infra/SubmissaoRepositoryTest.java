package br.udesc.dicionariolibras.submissoes.infra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.dicionariolibras.TestcontainersConfig;
import br.udesc.dicionariolibras.dicionario.dominio.Sinal;
import br.udesc.dicionariolibras.dicionario.dominio.Verbete;
import br.udesc.dicionariolibras.dicionario.infra.AssuntoRepository;
import br.udesc.dicionariolibras.dicionario.infra.ConfiguracaoMaoRepository;
import br.udesc.dicionariolibras.dicionario.infra.RegiaoRepository;
import br.udesc.dicionariolibras.dicionario.infra.VerbeteRepository;
import br.udesc.dicionariolibras.identidade.dominio.Perfil;
import br.udesc.dicionariolibras.identidade.dominio.Usuario;
import br.udesc.dicionariolibras.identidade.infra.UsuarioRepository;
import br.udesc.dicionariolibras.midia.dominio.Video;
import br.udesc.dicionariolibras.midia.infra.VideoRepository;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;
import br.udesc.dicionariolibras.privacidade.infra.ConsentimentoRepository;
import br.udesc.dicionariolibras.submissoes.dominio.StatusSubmissao;
import br.udesc.dicionariolibras.submissoes.dominio.Submissao;
import br.udesc.dicionariolibras.submissoes.dominio.SubmissaoAprovada;

/** Persiste o fluxo completo do UC01 no PostgreSQL e confere a publicação do evento. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
@Transactional
@RecordApplicationEvents
class SubmissaoRepositoryTest {

    @Autowired UsuarioRepository usuarios;
    @Autowired ConsentimentoRepository consentimentos;
    @Autowired VideoRepository videos;
    @Autowired VerbeteRepository verbetes;
    @Autowired AssuntoRepository assuntos;
    @Autowired RegiaoRepository regioes;
    @Autowired ConfiguracaoMaoRepository configuracoes;
    @Autowired SubmissaoRepository submissoes;
    @Autowired ApplicationEvents eventos;

    @Test
    void salvaSubmissaoAprovadaEPublicaEvento() {
        Usuario autor = usuarios.save(new Usuario("Ana", "ana@exemplo.com", Perfil.COLABORADOR));
        Usuario curador = usuarios.save(new Usuario("Caio", "caio@exemplo.com", Perfil.CURADOR));
        Consentimento consentimento = consentimentos.save(new Consentimento("1.0", false, null));
        Video video = videos.save(new Video("videos/pai-rs.mp4", "mp4", 8, 4.2));

        Verbete verbete = Verbete.builder()
                .palavra("Pai").acepcao("Genitor").assunto(assuntos.findAll().get(0)).build();
        Sinal sinal = new Sinal(regioes.findAll().get(0), configuracoes.findAll().get(0), video.getId());
        verbete.adicionarSinal(sinal);
        verbetes.saveAndFlush(verbete);

        Submissao submissao = new Submissao(autor, consentimento, sinal.getId());
        submissao.enviar(consentimento);
        submissoes.saveAndFlush(submissao);

        submissao.aprovar(curador);
        submissoes.saveAndFlush(submissao);

        assertThat(submissoes.findByStatusOrderByDataEnvio(StatusSubmissao.APROVADA)).containsExactly(submissao);
        assertThat(eventos.stream(SubmissaoAprovada.class))
                .singleElement()
                .satisfies(e -> assertThat(e.sinalId()).isEqualTo(sinal.getId()));
        assertThat(regioes.count()).isEqualTo(27);
    }
}
