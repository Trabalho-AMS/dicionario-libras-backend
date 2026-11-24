package br.udesc.dicionariolibras.privacidade.aplicacao;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.udesc.dicionariolibras.compartilhado.aplicacao.ArquivoRecebido;
import br.udesc.dicionariolibras.compartilhado.aplicacao.Transacoes;
import br.udesc.dicionariolibras.compartilhado.dominio.NaoEncontradoException;
import br.udesc.dicionariolibras.compartilhado.dominio.RegraDeNegocioException;
import br.udesc.dicionariolibras.midia.aplicacao.ArmazenamentoDeArquivos;
import br.udesc.dicionariolibras.privacidade.dominio.Consentimento;
import br.udesc.dicionariolibras.privacidade.infra.ConsentimentoRepository;
import lombok.RequiredArgsConstructor;

/** Interface pública do módulo privacidade: registro do aceite do termo de uso de imagem (LGPD). */
@Service
@RequiredArgsConstructor
@Transactional
public class ConsentimentoService {

    /** Versão vigente do termo de uso de imagem. */
    public static final String VERSAO_TERMO = "1.0";

    private final ConsentimentoRepository consentimentos;
    private final ArmazenamentoDeArquivos armazenamento;

    public Consentimento registrar(boolean aceiteTermo, boolean sinalizanteMenor, ArquivoRecebido termoResponsavel) {
        if (!aceiteTermo) {
            throw new RegraDeNegocioException("É preciso aceitar o termo de uso de imagem para enviar o vídeo");
        }
        String chaveTermo = null;
        if (sinalizanteMenor && termoResponsavel != null) {
            chaveTermo = "termos/" + UUID.randomUUID() + extensao(termoResponsavel.nome());
            armazenamento.salvar(chaveTermo, termoResponsavel.abrir(), termoResponsavel.tamanhoBytes(),
                    termoResponsavel.contentType());
            String chave = chaveTermo;
            Transacoes.aoDesfazer(() -> armazenamento.remover(chave));
        }
        Consentimento consentimento = new Consentimento(VERSAO_TERMO, sinalizanteMenor, chaveTermo);
        if (!consentimento.ehValido()) {
            throw new RegraDeNegocioException(
                    "Sinalizante menor de 18 anos: anexe o termo assinado pelo responsável legal (LGPD, art. 14)");
        }
        return consentimentos.save(consentimento);
    }

    @Transactional(readOnly = true)
    public Consentimento buscar(UUID id) {
        return consentimentos.findById(id).orElseThrow(() -> new NaoEncontradoException("Consentimento não encontrado"));
    }

    @Transactional(readOnly = true)
    public ConsentimentoResumo resumo(UUID id) {
        Consentimento c = buscar(id);
        return new ConsentimentoResumo(c.getId(), c.getVersaoTermo(), c.getDataHora(), c.isSinalizanteMenor(),
                c.getTermoResponsavel() != null, c.isRevogado());
    }

    /** Exclui o consentimento e o termo do responsável quando o titular desiste da submissão. */
    public void excluir(UUID id) {
        consentimentos.findById(id).ifPresent(c -> {
            consentimentos.delete(c);
            if (c.getTermoResponsavel() != null) {
                Transacoes.depoisDeConfirmar(() -> armazenamento.remover(c.getTermoResponsavel()));
            }
        });
    }

    private static String extensao(String nome) {
        if (nome == null || nome.lastIndexOf('.') < 0) {
            return "";
        }
        return nome.substring(nome.lastIndexOf('.')).toLowerCase();
    }
}
