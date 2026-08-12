package com.orderintegration.adapter.messaging.sap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de conexão com SAP (RFC)
 * Properties vinculadas em application.yml:
 * sap:
 * rfc:
 * host: localhost
 * port: 3200
 * client: 100
 * user: DEVELOPER
 * password: ${SAP_PASSWORD}
 * language: EN
 */
@Configuration
@ConfigurationProperties(prefix = "sap.rfc")
public class SapConnectorConfig {

    // RFC Connection
    private String host = "localhost";
    private Integer port = 3200;
    private String client = "100";
    private String user = "DEVELOPER";
    private String password;
    private String language = "EN";

    // RFC Function Module para criar pedido em SAP
    private String functionModuleCreateOrder = "ZORDERS_CREATE";
    private String functionModuleUpdateOrder = "ZORDERS_UPDATE";

    // Retry policy
    private Integer maxRetries = 3;
    private Long retryDelayMs = 5000L;
    private Long connectionTimeoutMs = 30000L;

    // iDoc settings
    private String idocMessageType = "ORDERS";
    private String idocProcessCode = "CRMORD";
    private String idocPortDest = "/APP/ORDER_INTEGRATION";

    public SapConnectorConfig() {
    }

    // Getters e Setters
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFunctionModuleCreateOrder() {
        return functionModuleCreateOrder;
    }

    public void setFunctionModuleCreateOrder(String functionModuleCreateOrder) {
        this.functionModuleCreateOrder = functionModuleCreateOrder;
    }

    public String getFunctionModuleUpdateOrder() {
        return functionModuleUpdateOrder;
    }

    public void setFunctionModuleUpdateOrder(String functionModuleUpdateOrder) {
        this.functionModuleUpdateOrder = functionModuleUpdateOrder;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(Long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public Long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(Long connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public String getIdocMessageType() {
        return idocMessageType;
    }

    public void setIdocMessageType(String idocMessageType) {
        this.idocMessageType = idocMessageType;
    }

    public String getIdocProcessCode() {
        return idocProcessCode;
    }

    public void setIdocProcessCode(String idocProcessCode) {
        this.idocProcessCode = idocProcessCode;
    }

    public String getIdocPortDest() {
        return idocPortDest;
    }

    public void setIdocPortDest(String idocPortDest) {
        this.idocPortDest = idocPortDest;
    }
}
