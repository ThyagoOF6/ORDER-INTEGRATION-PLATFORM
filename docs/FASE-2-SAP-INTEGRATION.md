# Fase 2: SAP Integration (RFC + iDoc)

## Overview

Fase 2 implements synchronous (RFC) and asynchronous (iDoc) integration with SAP backend for order processing. Orders can be synchronized via:

- **RFC Call**: Immediate synchronous call to SAP (suitable for critical orders)
- **iDoc Publishing**: Asynchronous publishing to message queue (suitable for bulk processing)

## Architecture

### Hexagonal Pattern - SAP Integration

```
Application Layer (core/application)
├── Port: SapSyncPort
│   ├── syncronizarPedidoRfc(Pedido): String
│   └── publicarPedidoIdoc(Pedido): String
│
Adapter Layer (adapter/out/messaging/sap)
├── SapConnectorConfig (Configuration)
├── RfcConnector (RFC Implementation)
├── IdocPublisher (iDoc Implementation)
└── SapOrderAdapter (SapSyncPort Implementation)
```

### Data Flow - RFC Synchronous

```
1. REST: POST /pedidos/{id}/sincronizar-rfc
2. PedidoService.sincronizarComSapRfc()
   - Fetch order from repository
   - Validate order status
   - Call SapSyncPort.sincronizarPedidoRfc()
3. SapOrderAdapter.sincronizarPedidoRfc()
   - Delegate to RfcConnector.criarPedidoRfc()
4. RfcConnector
   - Build RFC request (customer, items, total)
   - Call SAP ZORDERS_CREATE module
   - Return transaction ID (e.g., "SAP-2024-001234")
5. PedidoService
   - Update order status: VALIDADO → SINCRONIZANDO → SINCRONIZADO
   - Save to database
6. REST Response: 200 OK with updated order
```

### Data Flow - iDoc Asynchronous

```
1. REST: POST /pedidos/{id}/publicar-idoc
2. PedidoService.publicarPedidoIdoc()
   - Fetch order from repository
   - Call SapSyncPort.publicarPedidoIdoc()
3. SapOrderAdapter.publicarPedidoIdoc()
   - Delegate to IdocPublisher.publicarPedidoIdoc()
4. IdocPublisher
   - Generate iDoc XML (ORDERS message type)
   - Publish to message queue
   - Return iDoc ID (UUID)
5. PedidoService
   - Update order status: VALIDADO → SINCRONIZANDO
   - Save to database
   - Return immediately
6. REST Response: 200 OK with order in SINCRONIZANDO state
7. [Async] Message queue → SAP processes iDoc → updates order status
```

## Configuration

### Environment Variables (.env)

```bash
# SAP RFC Connection
SAP_HOST=sap-erp.example.com          # SAP system hostname
SAP_PORT=3200                         # Gateway port
SAP_CLIENT=100                        # SAP client number
SAP_USER=DEVELOPER                    # RFC user
SAP_PASSWORD=secure_password          # RFC password
```

### Application Properties (application.yml)

```yaml
sap:
  rfc:
    host: ${SAP_HOST:localhost}
    port: ${SAP_PORT:3200}
    client: ${SAP_CLIENT:100}
    user: ${SAP_USER:DEVELOPER}
    password: ${SAP_PASSWORD:}
    language: EN
    function-module-create-order: ZORDERS_CREATE
    function-module-update-order: ZORDERS_UPDATE
    max-retries: 3
    retry-delay-ms: 5000
    connection-timeout-ms: 30000
  idoc:
    message-type: ORDERS
    process-code: CRMORD
    port-dest: /APP/ORDER_INTEGRATION
```

## REST API Endpoints

### Synchronous RFC Integration

```http
POST /pedidos/{pedidoId}/sincronizar-rfc

Request:
- Path: pedidoId = "abc-123-def"

Response (200 OK):
{
  "pedidoId": "abc-123-def",
  "codigoCliente": "CLI-001",
  "status": "SINCRONIZADO",
  "itens": [...],
  "valorTotal": 500.00,
  "criadoEm": "2026-12-08T10:00:00Z"
}

Error Responses:
- 404 Not Found: Pedido não encontrado
- 400 Bad Request: Pedido em estado inválido (não está VALIDADO)
- 503 Service Unavailable: SAP indisponível
```

### Asynchronous iDoc Publishing

```http
POST /pedidos/{pedidoId}/publicar-idoc

Request:
- Path: pedidoId = "abc-123-def"

Response (200 OK):
{
  "pedidoId": "abc-123-def",
  "codigoCliente": "CLI-001",
  "status": "SINCRONIZANDO",
  "itens": [...],
  "valorTotal": 500.00,
  "criadoEm": "2026-12-08T10:00:00Z"
}

Notes:
- Order returns immediately in SINCRONIZANDO state
- Actual SAP processing happens asynchronously
- Response queue (Phase 2.5) will update final status
```

## Example Workflows

### Workflow 1: Synchronous Order Processing

```bash
# 1. Create order
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "codigoCliente": "CLI-001",
    "itens": [
      {
        "codigoProduto": "PROD-001",
        "descricao": "Product A",
        "quantidade": 5,
        "precoUnitario": 100.00
      }
    ]
  }'

# Response: 201 Created with pedidoId = "abc-123"

# 2. Validate order
curl -X POST http://localhost:8080/api/pedidos/abc-123/validar

# Response: 200 OK, status = VALIDADO

# 3. Synchronize with SAP (RFC - blocking)
curl -X POST http://localhost:8080/api/pedidos/abc-123/sincronizar-rfc

# Response: 200 OK, status = SINCRONIZADO
# SAP order created, transaction ID in logs
```

### Workflow 2: Asynchronous Order Processing

```bash
# 1. Create and validate order (same as above)
curl -X POST http://localhost:8080/api/pedidos
curl -X POST http://localhost:8080/api/pedidos/abc-123/validar

# 2. Publish to queue (non-blocking)
curl -X POST http://localhost:8080/api/pedidos/abc-123/publicar-idoc

# Response: 200 OK, status = SINCRONIZANDO
# Returns immediately, queue processes asynchronously

# 3. Check order status (eventual consistency)
curl -X GET http://localhost:8080/api/pedidos/abc-123

# Response: status = SINCRONIZANDO (then SINCRONIZADO later)
```

## Testing

### Unit Tests

```bash
# Run SAP adapter tests
./gradlew.bat test --tests "*SapOrderAdapterTest"
./gradlew.bat test --tests "*RfcConnectorTest"
./gradlew.bat test --tests "*IdocPublisherTest"

# Run all Fase 2 tests
./gradlew.bat adapter:out:messaging:test
```

### Integration Tests

```bash
# Run with H2 in-memory DB (no external SAP needed)
./gradlew.bat adapter:in:rest:test

# Test endpoints:
POST /pedidos - Create order
GET /pedidos/{id} - Retrieve order
POST /pedidos/{id}/validar - Validate
POST /pedidos/{id}/sincronizar-rfc - RFC sync (mocked)
POST /pedidos/{id}/publicar-idoc - iDoc publish (mocked)
```

### Manual Testing

Development/mocked SAP responses:

- RFC calls return `SAP-{timestamp}` transaction IDs
- iDoc publishing returns UUID identifiers
- No actual SAP connection required

Production deployment:

- Add SAP JCo library to classpath
- Configure real SAP credentials in .env
- Test against SAP sandbox/QA environment first

## Error Handling

### RFC Call Failures

```
Exception: SapSyncException
Causes:
- Connection timeout (>30s)
- RFC module not found
- Invalid parameters
- SAP authorization failed

Flow:
1. RfcConnector throws RfcException
2. SapOrderAdapter catches → throws SapSyncException
3. PedidoService catches → updates order with error message
4. REST Controller catches → returns 503 Service Unavailable
```

### iDoc Publishing Failures

```
Exception: IdocException
Causes:
- Message queue unavailable
- XML generation failed
- Queue broker connectivity

Flow:
1. IdocPublisher throws IdocException
2. SapOrderAdapter catches → throws SapSyncException
3. PedidoService catches → updates order with error message
4. REST Controller catches → returns 503 Service Unavailable
```

## Logging

Logs are prefixed by component:

```
[RfcConnector] Iniciando RFC para criar pedido: abc-123
[RfcConnector] Pedido criado em SAP com sucesso. Transação: SAP-2024-001234

[IdocPublisher] Publicando iDoc para pedido: abc-123
[IdocPublisher] iDoc publicado com sucesso. ID: xxxxxxxx-xxxx-xxxx

[SapOrderAdapter] Iniciando sincronização RFC do pedido: abc-123
[SapOrderAdapter] Pedido sincronizado com RFC. Transação SAP: SAP-2024-001234

[PedidoService] Iniciando sincronização RFC com SAP para pedido: abc-123
[PedidoService] Pedido sincronizado com RFC. Transação SAP: SAP-2024-001234
```

Enable DEBUG logging for detailed RFC/iDoc payloads:

```yaml
logging:
  level:
    com.orderintegration.adapter.messaging.sap: DEBUG
```

## Next Steps (Phase 2.5+)

1. **Response Queue Listeners**: Listen for SAP iDoc confirmations
2. **Order Status Updates**: Automatically transition from SINCRONIZANDO → SINCRONIZADO
3. **Error Recovery**: Implement retry mechanisms for failed iDoc processing
4. **Audit Trail**: Log all SAP interactions for compliance
5. **Monitoring**: Metrics for RFC response times, iDoc throughput
6. **SAP JCo Integration**: Replace mock RfcConnector with real SAP JCo library

## References

- [ADR-004: SAP Integration Pattern](../adr/ADR-004-SAP-Integration-Pattern.md)
- [Application Layer](../architecture/ARCHITECTURE.md#application-layer)
- [Hexagonal Architecture](../architecture/ARCHITECTURE.md#hexagonal-pattern)
- SAP RFC Documentation: https://help.sap.com/
- iDoc Standards: https://launchpad.support.sap.com/
