package com.orderintegration.application.port;

import com.orderintegration.application.dto.IdocResponse;

/**
 * Hexagonal Architecture Port para processamento de respostas iDoc
 * 
 * Responsabilidades:
 * - Processar confirmacoes de iDoc recebidas via fila
 * - Atualizar status de pedidos (SINCRONIZANDO > SINCRONIZADO)
 * - Registrar erros de processamento (> ERRO)
 * - Implementacoes: listeners de fila (Kafka, RabbitMQ, Azure Service Bus)
 */
public interface IdocResponsePort {

    /**
     * Processa uma resposta de iDoc bem-sucedida
     * Atualiza pedido de SINCRONIZANDO para SINCRONIZADO
     * 
     * @param response IdocResponse com dados de sucesso
     * @throws IdocResponseException se algo der errado na atualização
     */
    void processarRespostaIDocSucesso(IdocResponse response) throws IdocResponseException;

    /**
     * Processa uma resposta de iDoc com erro
     * Atualiza pedido para ERRO e registra mensagem
     * 
     * @param response IdocResponse com dados de erro
     * @throws IdocResponseException se algo der errado na atualização
     */
    void processarRespostaIdocErro(IdocResponse response) throws IdocResponseException;

    /**
     * Exception para erros no processamento de resposta iDoc
     */
    class IdocResponseException extends RuntimeException {
        public IdocResponseException(String message) {
            super(message);
        }

        public IdocResponseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
