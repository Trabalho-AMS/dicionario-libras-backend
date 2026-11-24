package br.udesc.dicionariolibras.compartilhado.aplicacao;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Ações sobre arquivos que precisam acompanhar o resultado da transação do banco. */
public final class Transacoes {

    private Transacoes() {
    }

    /** Executa a ação se a transação atual for desfeita (ex.: apagar um arquivo já enviado ao bucket). */
    public static void aoDesfazer(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        acao.run();
                    }
                }
            });
        }
    }

    /** Executa a ação só depois que a transação atual for confirmada (ou na hora, se não houver transação). */
    public static void depoisDeConfirmar(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acao.run();
                }
            });
        } else {
            acao.run();
        }
    }
}
