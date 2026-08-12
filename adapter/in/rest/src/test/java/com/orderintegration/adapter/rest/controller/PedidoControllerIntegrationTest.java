package com.orderintegration.adapter.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderintegration.application.dto.ItemPedidoDTO;
import com.orderintegration.application.dto.PedidoRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para PedidoController
 * Testa fluxo completo: REST → Application → Domain → Persistence
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PedidoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveCriarPedidoComSucesso() throws Exception {
        // Given
        var itens = List.of(
                new ItemPedidoDTO("PROD-001", "Notebook Dell XPS 13", 1, BigDecimal.valueOf(5999.99)),
                new ItemPedidoDTO("PROD-002", "Mouse Logitech MX Master", 2, BigDecimal.valueOf(299.99)));

        var request = new PedidoRequestDTO("CLI-001", itens);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pedidoId", notNullValue()))
                .andExpect(jsonPath("$.codigoCliente", equalTo("CLI-001")))
                .andExpect(jsonPath("$.status", equalTo("CRIADO")))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.valorTotal", notNullValue()));
    }

    @Test
    void deveLancarExcecaoAoCriarPedidoSemItens() throws Exception {
        // Given
        var request = new PedidoRequestDTO("CLI-001", List.of());

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveLancarExcecaoAoCriarPedidoSemCliente() throws Exception {
        // Given
        var itens = List.of(
                new ItemPedidoDTO("PROD-001", "Notebook", 1, BigDecimal.valueOf(5999.99)));
        var request = new PedidoRequestDTO("", itens);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarPedidoPorIdComSucesso() throws Exception {
        // Given - Criar pedido
        var itens = List.of(
                new ItemPedidoDTO("PROD-001", "Notebook", 1, BigDecimal.valueOf(5999.99)));
        var request = new PedidoRequestDTO("CLI-001", itens);

        var createResponse = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var pedidoId = objectMapper.readTree(createResponse).get("pedidoId").asText();

        // When & Then - Buscar pedido
        mockMvc.perform(get("/api/pedidos/{pedidoId}", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId", equalTo(pedidoId)))
                .andExpect(jsonPath("$.codigoCliente", equalTo("CLI-001")))
                .andExpect(jsonPath("$.status", equalTo("CRIADO")));
    }

    @Test
    void deveLancarExcecaoAoBuscarPedidoInexistente() throws Exception {
        // Given
        String pedidoIdInexistente = "00000000-0000-0000-0000-000000000000";

        // When & Then
        mockMvc.perform(get("/api/pedidos/{pedidoId}", pedidoIdInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo", equalTo(404)));
    }

    @Test
    void deveValidarPedidoComSucesso() throws Exception {
        // Given - Criar pedido
        var itens = List.of(
                new ItemPedidoDTO("PROD-001", "Notebook", 1, BigDecimal.valueOf(5999.99)));
        var request = new PedidoRequestDTO("CLI-001", itens);

        var createResponse = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var pedidoId = objectMapper.readTree(createResponse).get("pedidoId").asText();

        // When & Then - Validar pedido
        mockMvc.perform(post("/api/pedidos/{pedidoId}/validar", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId", equalTo(pedidoId)))
                .andExpect(jsonPath("$.status", equalTo("VALIDADO")));
    }

    @Test
    void deveIniciarSincronizacaoApenasComPedidoValidado() throws Exception {
        // Given - Criar pedido
        var itens = List.of(
                new ItemPedidoDTO("PROD-001", "Notebook", 1, BigDecimal.valueOf(5999.99)));
        var request = new PedidoRequestDTO("CLI-001", itens);

        var createResponse = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var pedidoId = objectMapper.readTree(createResponse).get("pedidoId").asText();

        // Validar
        mockMvc.perform(post("/api/pedidos/{pedidoId}/validar", pedidoId))
                .andExpect(status().isOk());

        // When & Then - Sincronizar
        mockMvc.perform(post("/api/pedidos/{pedidoId}/sincronizar", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("SINCRONIZANDO")));
    }

    @Test
    void deveConfirmarSincronizacao() throws Exception {
        // Given - Criar → Validar → Sincronizar
        var itens = List.of(
                new ItemPedidoDTO("PROD-001", "Notebook", 1, BigDecimal.valueOf(5999.99)));
        var request = new PedidoRequestDTO("CLI-001", itens);

        var createResponse = mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        var pedidoId = objectMapper.readTree(createResponse).get("pedidoId").asText();

        mockMvc.perform(post("/api/pedidos/{pedidoId}/validar", pedidoId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/pedidos/{pedidoId}/sincronizar", pedidoId))
                .andExpect(status().isOk());

        // When & Then - Confirmar
        mockMvc.perform(post("/api/pedidos/{pedidoId}/confirmar-sincronizacao", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("SINCRONIZADO")));
    }
}
