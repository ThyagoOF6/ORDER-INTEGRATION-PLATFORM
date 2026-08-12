# ADR-004: SAP Integration Pattern - RFC vs iDoc

**Date**: December 2026
**Status**: Accepted
**Context**: Enterprise integration with SAP backend for order processing and synchronization

## Problem Statement
Order Integration Platform needs to communicate with SAP ERP for order creation and status management. This requires deciding between synchronous (RFC) and asynchronous (iDoc) integration patterns.

## Decision
Implement **hybrid integration approach**:
1. **RFC (Remote Function Call)**: Synchronous integration for order creation and status updates
2. **iDoc (Intermediate Document)**: Asynchronous integration for notification and audit trail

## Rationale

### RFC (Synchronous)
- **Advantages**:
  - Immediate consistency - order created/updated in SAP confirmed before returning response
  - Simple error handling - RFC call fails immediately, easy to propagate error
  - Real-time status visibility
  - No message queue dependency
  
- **Disadvantages**:
  - Blocks client request until SAP responds
  - Network latency impact on API response time
  - SAP system downtime blocks orders
  
- **Use Cases**:
  - Initial order creation (clients expect immediate confirmation)
  - Urgent status updates
  - Integration testing and development

### iDoc (Asynchronous)
- **Advantages**:
  - Non-blocking - order published to queue immediately
  - Fault tolerance - queue survives SAP downtime
  - Scalable - orders queued for batch processing
  - Audit trail - every order tracked through messaging middleware
  - Decoupled systems - platform independent of SAP processing speed
  
- **Disadvantages**:
  - Eventual consistency - delay before SAP processes
  - Requires message broker infrastructure (Kafka, RabbitMQ)
  - Complex debugging (errors hidden in async processing)
  - Order status updates must poll or listen to response queue
  
- **Use Cases**:
  - Bulk order processing
  - Non-urgent updates
  - Production deployments
  - Integration with multiple SAP systems

## Architecture

```
Order Intake (REST API)
    ↓
[SapSyncPort Interface] (Hexagonal Port)
    ├→ [RfcConnector] → SAP RFC Call → Immediate Response
    └→ [IdocPublisher] → Message Queue → Async Processing
```

### Implementation Details

**SapSyncPort** (core/application/port)
- Port interface defining contract
- Methods: `sincronizarPedidoRfc()`, `publicarPedidoIdoc()`
- Both return transaction IDs for tracking

**RfcConnector** (adapter/out/messaging/sap)
- Encapsulates RFC connection logic
- Retry policy: 3 attempts with exponential backoff
- Timeout: 30 seconds per call
- Mock implementation for development (SAP JCo in production)

**IdocPublisher** (adapter/out/messaging/sap)
- Generates iDoc XML in standard SAP ORDERS format
- Publishes to message queue
- UUID generation for tracking
- Fields included: cliente, pedido, items, valores, status

**SapOrderAdapter** (adapter/out/messaging/sap)
- Implements SapSyncPort
- Coordinates RFC and iDoc calls
- Error translation from SAP to application exceptions

## Configuration

```yaml
sap:
  rfc:
    host: ${SAP_HOST}
    port: ${SAP_PORT}
    client: ${SAP_CLIENT}
    user: ${SAP_USER}
    password: ${SAP_PASSWORD}
    language: EN
    function-module-create-order: ZORDERS_CREATE
    max-retries: 3
    connection-timeout-ms: 30000
```

## Integration Points

**PedidoService** (core/application/service)
- New methods: `syncronizarComSapRfc()`, `publicarPedidoIdoc()`
- Called explicitly by REST endpoints
- Error handling converts SAP exceptions to application exceptions

**REST Endpoints** (adapter/in/rest)
- `POST /pedidos/{id}/sincronizar-rfc`: Trigger synchronous RFC call
- `POST /pedidos/{id}/publicar-idoc`: Trigger asynchronous iDoc publishing
- HTTP 503 Service Unavailable for SAP connectivity issues

## Consequences

### Positive
- Flexible integration - choose sync/async per order
- Testable - mock implementations easily
- Scalable - iDoc approach enables bulk processing
- Observable - transaction IDs for tracking

### Negative
- Dual systems add complexity
- Requires SAP RFC function modules (ZORDERS_CREATE, ZORDERS_UPDATE)
- Development requires SAP landscape or mock service
- Message queue infrastructure needed for production

## Alternatives Considered

1. **RFC Only**
   - Simpler implementation
   - Rejected: poor scaling, SAP downtime impact

2. **iDoc Only**
   - Simpler queue architecture
   - Rejected: no immediate order confirmation

3. **SOAP/REST via SAP PI**
   - Standards-based
   - Rejected: additional middleware complexity

## Future Enhancements

1. **Phase 2.5**: Implement response queue listeners for iDoc confirmations
2. **Phase 3**: Add async order status notifications
3. **Phase 4**: Event sourcing for all SAP interactions
4. **Phase 5**: SAP AIF (Application Interface Framework) integration

## References

- SAP RFC Documentation: https://help.sap.com/viewer/753088fc4491456e8dab7490579602da/
- iDoc Standards: https://launchpad.support.sap.com/#/notes/
- Spring Integration SAP: https://docs.spring.io/spring-integration/docs/current/reference/html/sap.html
