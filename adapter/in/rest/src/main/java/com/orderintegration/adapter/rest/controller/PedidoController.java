package com.orderintegration.adapter.rest.controller;

import com.orderintegration.application.dto.PedidoRequestDTO;
import com.orderintegration.application.dto.PedidoResponseDTO;
import com.orderintegration.application.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller para Pedidos
 * Hexagonal Architecture: Adapter (in/rest)
 */
@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "API para gerenciar pedidos de integração")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido", description = "Cria um novo pedido com itens")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> criarPedido(
            @Valid @RequestBody PedidoRequestDTO request) {
        PedidoResponseDTO response = pedidoService.criarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{pedidoId}")
    @Operation(summary = "Buscar pedido por ID", description = "Retorna os detalhes de um pedido específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> buscarPorId(
            @PathVariable String pedidoId) {
        PedidoResponseDTO response = pedidoService.buscarPorId(pedidoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pedidoId}/validar")
    @Operation(summary = "Validar pedido", description = "Valida o pedido e muda seu status para VALIDADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido validado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Pedido em estado inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> validarPedido(
            @PathVariable String pedidoId) {
        PedidoResponseDTO response = pedidoService.validarPedido(pedidoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pedidoId}/sincronizar")
    @Operation(summary = "Iniciar sincronização", description = "Inicia a sincronização do pedido com SAP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização iniciada"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Pedido em estado inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> iniciarSincronizacao(
            @PathVariable String pedidoId) {
        PedidoResponseDTO response = pedidoService.iniciarSincronizacao(pedidoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pedidoId}/confirmar-sincronizacao")
    @Operation(summary = "Confirmar sincronização", description = "Confirma que a sincronização com SAP foi bem-sucedida")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sincronização confirmada"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Pedido em estado inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> confirmarSincronizacao(
            @PathVariable String pedidoId) {
        PedidoResponseDTO response = pedidoService.confirmarSincronizacao(pedidoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pedidoId}/registrar-erro")
    @Operation(summary = "Registrar erro na sincronização", description = "Registra um erro ocorrido durante sincronização")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Erro registrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Pedido em estado inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> registrarErro(
            @PathVariable String pedidoId,
            @RequestParam String mensagem) {
        PedidoResponseDTO response = pedidoService.registrarErroSincronizacao(pedidoId, mensagem);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pedidoId}/sincronizar-rfc")
    @Operation(summary = "Sincronizar com SAP via RFC", description = "Sincroniza o pedido com SAP usando chamada RFC síncrona")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido sincronizado com RFC"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Pedido em estado inválido"),
            @ApiResponse(responseCode = "503", description = "Serviço SAP indisponível"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> sincronizarComSapRfc(
            @PathVariable String pedidoId) {
        PedidoResponseDTO response = pedidoService.sincronizarComSapRfc(pedidoId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{pedidoId}/publicar-idoc")
    @Operation(summary = "Publicar pedido como iDoc", description = "Publica o pedido como iDoc para processamento assíncrono em SAP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido publicado como iDoc"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "400", description = "Pedido em estado inválido"),
            @ApiResponse(responseCode = "503", description = "Fila de mensagens indisponível"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<PedidoResponseDTO> publicarPedidoIdoc(
            @PathVariable String pedidoId) {
        PedidoResponseDTO response = pedidoService.publicarPedidoIdoc(pedidoId);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(PedidoService.PedidoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handlePedidoNotFound(PedidoService.PedidoNaoEncontradoException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PedidoService.SyncComSapException.class)
    public ResponseEntity<ErrorResponse> handleSyncComSapException(PedidoService.SyncComSapException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Erro na sincronização com SAP: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Resposta de erro padronizada
     */
    record ErrorResponse(int codigo, String mensagem) {
    }
}
