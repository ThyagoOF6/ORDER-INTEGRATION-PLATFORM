package com.orderintegration.application.dto;

import java.time.Instant;

/**
 * Representa a resposta de um iDoc enviado para SAP
 * 
 * Estrutura de mensagem recebida da fila de respostas:
 * {
 * "idocId": "uuid-string",
 * "pedidoId": "uuid-string",
 * "status": "ACEITO|REJEITADO|PROCESSADO",
 * "sapMessageId": "SAP-MSG-12345",
 * "errorCode": null or "E001",
 * "errorMessage": null or "Invalid customer code",
 * "processedAt": "2026-08-12T15:30:00Z"
 * }
 */
public class IdocResponse {
    private String idocId;
    private String pedidoId;
    private String status; // ACEITO, REJEITADO, PROCESSADO
    private String sapMessageId;
    private String errorCode;
    private String errorMessage;
    private Instant processedAt;

    // Constructors
    public IdocResponse() {
    }

    public IdocResponse(String idocId, String pedidoId, String status,
            String sapMessageId, String errorCode, String errorMessage,
            Instant processedAt) {
        this.idocId = idocId;
        this.pedidoId = pedidoId;
        this.status = status;
        this.sapMessageId = sapMessageId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.processedAt = processedAt;
    }

    // Factory method
    public static IdocResponse sucesso(String idocId, String pedidoId,
            String sapMessageId) {
        return new IdocResponse(
                idocId,
                pedidoId,
                "PROCESSADO",
                sapMessageId,
                null,
                null,
                Instant.now());
    }

    public static IdocResponse erro(String idocId, String pedidoId,
            String errorCode, String errorMessage) {
        return new IdocResponse(
                idocId,
                pedidoId,
                "REJEITADO",
                null,
                errorCode,
                errorMessage,
                Instant.now());
    }

    // Getters and Setters
    public String getIdocId() {
        return idocId;
    }

    public void setIdocId(String idocId) {
        this.idocId = idocId;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSapMessageId() {
        return sapMessageId;
    }

    public void setSapMessageId(String sapMessageId) {
        this.sapMessageId = sapMessageId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public boolean isProcessado() {
        return "PROCESSADO".equals(status);
    }

    public boolean isRejeitado() {
        return "REJEITADO".equals(status);
    }

    @Override
    public String toString() {
        return "IdocResponse{" +
                "idocId='" + idocId + '\'' +
                ", pedidoId='" + pedidoId + '\'' +
                ", status='" + status + '\'' +
                ", sapMessageId='" + sapMessageId + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
