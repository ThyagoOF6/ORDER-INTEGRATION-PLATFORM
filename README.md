# Order Integration Platform

[🇧🇷 Português](README.pt-BR.md) | 🇺🇸 English

Order integration platform with hexagonal architecture, ready for integration with ERP systems (SAP) and Azure.

## Overview

This project demonstrates enterprise-grade Java patterns using Domain-Driven Design (DDD), Hexagonal Architecture, and quality software engineering practices. The structure is ready to scale from a prototype to a production system.

### Technologies

| Layer               | Technology                 | Version |
| ------------------- | --------------------------- | ------ |
| **Language**        | Java                       | 21 LTS |
| **Framework**       | Spring Boot                | 3.3.2  |
| **Build**           | Gradle                     | 8.5    |
| **Database**        | PostgreSQL                 | 16     |
| **Orchestration**   | Docker Compose             | v2+    |
| **CI/CD**           | GitHub Actions             | Latest |
| **Observability**   | OpenTelemetry + Prometheus | Latest |
| **Testing**         | JUnit 5 + Mockito          | 5.10+  |
| **Coverage**        | JaCoCo                     | 0.8.11 |

## Architecture

### Module Structure

```
order-integration-platform/
│
├── core/
│   ├── domain/               # Pure business logic (no external dependencies)
│   │   ├── src/main/java/com/orderintegration/core/domain/
│   │   │   ├── order/
│   │   │   │   ├── Pedido.java              # Aggregate Root
│   │   │   │   ├── ItemPedido.java          # Value Object
│   │   │   │   ├── PedidoId.java            # Value Object (ID)
│   │   │   │   ├── StatusPedido.java        # State enum
│   │   │   │   └── events/
│   │   │   │       ├── PedidoCriadoEvent.java
│   │   │   │       ├── PedidoSincronizadoEvent.java    # Phase 2
│   │   │   │       └── PedidoErroSincronizacaoEvent.java # Phase 2
│   │   │   └── common/
│   │   │       └── DomainEvent.java         # Base class for events
│   │   └── src/test/java/   # Unit tests (>95% coverage)
│   │       ├── PedidoTest.java
│   │       └── ItemPedidoTest.java
│   │
│   └── application/          # Use cases (Phase 1 + 2)
│       ├── service/
│       │   ├── PedidoService.java       # 8 use cases (CRUD + RFC/iDoc)
│       │   └── dto/
│       │       ├── PedidoRequestDTO.java
│       │       ├── PedidoResponseDTO.java
│       │       └── ItemPedidoDTO.java
│       └── port/
│           ├── PedidoRepositoryPort.java      # Persistence port
│           └── SapSyncPort.java               # SAP integration port (Phase 2)
│
├── adapter/
│   ├── in/
│   │   └── rest/             # REST Controllers (Phase 1 + 2)
│   │       ├── PedidoController.java        # 8 endpoints (CRUD + RFC/iDoc)
│   │       └── exception/
│   │           └── GlobalExceptionHandler.java
│   │
│   └── out/
│       ├── persistence/      # JPA Repositories (Phase 1)
│       │   ├── PedidoJpaRepositoryAdapter.java
│       │   ├── entity/
│       │   │   ├── PedidoJpaEntity.java
│       │   │   └── ItemPedidoJpaEntity.java
│       │   └── repository/
│       │       └── PedidoSpringDataRepository.java
│       │
│       └── messaging/        # SAP Integration (Phase 2) ✅
│           ├── sap/
│           │   ├── RfcConnector.java          # Synchronous RFC calls
│           │   ├── IdocPublisher.java         # Asynchronous iDoc publishing
│           │   ├── SapOrderAdapter.java       # Port implementation
│           │   └── SapConnectorConfig.java    # Configuration binding
│           └── test/
│               ├── RfcConnectorTest.java
│               ├── IdocPublisherTest.java
│               └── SapOrderAdapterTest.java
│
├── infrastructure/
│   ├── config/               # Spring configurations
│   │   ├── SecurityConfig.java
│   │   ├── PersistenceConfig.java
│   │   └── ObservabilityConfig.java
│   │
│   └── observability/        # Telemetry and metrics
│       ├── TracingConfig.java
│       ├── MetricsConfig.java
│       └── HealthIndicators.java
│
├── bootstrap/                # Application entry point
│   ├── src/main/java/com/orderintegration/bootstrap/
│   │   └── OrderIntegrationApplication.java
│   └── src/main/resources/
│       ├── application.yml   # Main configuration
│       └── application-*.yml # Profiles (dev, test, prod)
│
├── docs/
│   ├── adr/                  # Architecture Decision Records
│   │   ├── 0001-hexagonal-architecture.md
│   │   ├── 0002-domain-events.md
│   │   └── 0003-postgresql-database.md
│   └── ...
│
├── build.gradle              # Main Gradle configuration (multi-module)
├── settings.gradle           # Module definitions
├── docker-compose.yml        # Local infrastructure (PostgreSQL + LocalStack + Kafka)
├── Dockerfile                # Production build (multi-stage)
└── .github/workflows/        # CI/CD pipelines
    └── ci-cd.yml             # Build, test, quality, Docker, security
```

### Architectural Patterns

#### 1. Hexagonal Architecture (Ports & Adapters)

The application is organized in concentric layers:

- **Core (Domain)**: Pure business logic, framework-independent
- **Application**: Use cases, coordination between aggregates
- **Adapters**: Concrete implementations (REST, database, messaging)
- **Infrastructure**: Configuration, security, observability

**Benefit**: Easy to test, technology-independent, ready for future changes.

#### 2. Domain-Driven Design (DDD)

- **Aggregates**: `Pedido` (Aggregate Root) contains `ItemPedido` (entities/value objects)
- **Value Objects**: `PedidoId`, `ItemPedido` - immutable, equality by value
- **Domain Events**: `PedidoCriadoEvent` - communication between aggregates
- **State Enums**: `StatusPedido` - domain state machine

**Benefit**: Code that reflects business language, easy collaboration with domain experts.

#### 3. SAP Integration Pattern (RFC + iDoc) - Phase 2

Integration with ERP systems using a hybrid pattern:

- **RFC (Remote Function Call)**: Synchronous, immediate response, ideal for validations
- **iDoc (Intermediate Document)**: Asynchronous, queue publishing, ideal for batch processing
- **SapSyncPort**: Hexagonal interface to decouple synchronization logic

**Benefit**: Flexibility to choose between sync and async per operation.

#### 4. CQRS Ready (Command Query Responsibility Segregation)

Structure prepared for separation of responsibilities:

- **Commands**: Operations that change state (create, validate, synchronize order)
- **Queries**: Read-only operations (fetch order, list orders)

Implementation in Phase 3b.

#### 5. Event Sourcing

- All domain events persisted in an append-only Event Store
- Complete audit trail of changes
- Foundation for state reconstruction via event replay

Implemented in Phase 3 (foundation); full replay/read models in Phase 3b.

### State Machine

```
Pedido (Initial State)
  │
  └─────────────────────────────────────────┐
              CRIADO                         │
              (created)                      │
                  │                          │
                  ├─> validar()              │
                  │                          │
                  v                          │
              VALIDADO                       │
              (validated)                    │
                  │                          │
                  ├─> iniciarSincronizacao() │
                  │                          │
                  v                          │
           SINCRONIZANDO                     │
           (synchronizing)                   │
              /       \                      │
             /         \                     │
            v           v                    │
      SINCRONIZADO    ERRO                   │
      (success)     (error)                  │
           │           │                     │
           └───────────┴─────────────────────┘
              (terminal states)
```

## Implementation Checklist

### ✅ Phase 0 - Scaffolding (Complete)

- [x] Hexagonal structure with 8 Gradle modules
- [x] Domain Layer: Aggregates (Pedido), Value Objects (PedidoId, ItemPedido)
- [x] Base Domain Events (DomainEvent, PedidoCriadoEvent)
- [x] State Machine (CRIADO → VALIDADO → SINCRONIZANDO → SINCRONIZADO/ERRO)
- [x] > 95% JaCoCo code coverage
- [x] GitHub Actions CI/CD pipeline
- [x] Docker Compose (PostgreSQL 16 + LocalStack)
- [x] 3 Architecture Decision Records (ADRs 001-003)

### ✅ Phase 1 - Application Layer + REST (Complete)

- [x] PedidoService: 6 use cases (create, fetch, validate, start sync, confirm sync, register error)
- [x] PedidoRepositoryPort: Hexagonal port interface
- [x] PedidoJpaRepositoryAdapter: Spring Data JPA implementation
- [x] DTOs: ItemPedidoDTO, PedidoRequestDTO, PedidoResponseDTO
- [x] JPA Entities: PedidoJpaEntity, ItemPedidoJpaEntity
- [x] REST Controller: 6 endpoints (POST /pedidos, GET /pedidos/{id}, POST /validar, etc)
- [x] Integration Tests: 8 test cases with MockMvc + H2
- [x] Spring Validation (@NotBlank, @NotEmpty, @Valid)
- [x] Exception Handling: PedidoNaoEncontradoException, GlobalExceptionHandler
- [x] Flyway Migration: V1\_\_create_pedido_tables.sql
- [x] OpenAPI/Swagger annotations

### ✅ Phase 2 - SAP Integration (Complete)

- [x] SapSyncPort: Hexagonal port interface (sincronizarPedidoRfc, publicarPedidoIdoc)
- [x] RfcConnector: Synchronous RFC adapter
  - [x] criarPedidoRfc() with @Retryable
  - [x] atualizarStatusPedidoRfc()
  - [x] Mock implementation with simulated latency
  - [x] Production-ready structure (TODO: SAP JCo library)
- [x] IdocPublisher: Asynchronous iDoc adapter
  - [x] publicarPedidoIdoc() method
  - [x] gerarIdocXml() with SAP ORDERS format
  - [x] Mock queue publishing
  - [x] Production-ready for Kafka/RabbitMQ/Azure Service Bus
- [x] SapOrderAdapter: Hexagonal adapter implementing SapSyncPort
- [x] Domain Events: PedidoSincronizadoEvent, PedidoErroSincronizacaoEvent
- [x] PedidoService enhancements:
  - [x] SapSyncPort injection
  - [x] sincronizarComSapRfc() use case
  - [x] publicarPedidoIdoc() use case
  - [x] Error handling and logging
- [x] REST Controller enhancements:
  - [x] POST /pedidos/{id}/sincronizar-rfc endpoint
  - [x] POST /pedidos/{id}/publicar-idoc endpoint
  - [x] Exception handler for SyncComSapException (503 Service Unavailable)
- [x] SapConnectorConfig: @ConfigurationProperties for SAP settings
- [x] Application Configuration: application.yml with SAP section
- [x] Environment Variables: .env.example with SAP credentials
- [x] Gradle Dependencies: spring-retry, spring-boot-starter-aop
- [x] Unit Tests:
  - [x] RfcConnectorTest (4+ test methods)
  - [x] IdocPublisherTest (4+ test methods)
  - [x] SapOrderAdapterTest (4+ test methods)
- [x] Documentation:
  - [x] ADR-004: SAP Integration Pattern Decision
  - [x] FASE-2-SAP-INTEGRATION.md: Complete guide with examples

### ✅ Phase 2.5 - Response Queue Listeners (Complete)

- [x] IdocResponse DTO for iDoc confirmations
- [x] IdocResponsePort interface (hexagonal port)
- [x] IdocResponseService with state transitions
- [x] IdocResponseListener for the success queue
- [x] ErrorQueueListener for the error queue
- [x] 8 unit tests (listeners + service)
- [x] Auto-update status SINCRONIZANDO → SINCRONIZADO/ERRO
- [x] Complete documentation (FASE-2.5-RESPONSE-QUEUE.md)

### 🔄 Phase 3 - Event Sourcing & Message Broker (Foundation Complete)

- [x] Event Store: `domain_events` table (Flyway V2) with 7 indexes
- [x] DomainEventRepositoryPort + DomainEventJpaRepositoryAdapter (DTO/Entity mapping)
- [x] Pedido emits events on every transition (creation, sync, error)
- [x] EventPublisherService: persists pending events into the Event Store
- [x] Real Kafka: docker-compose with Zookeeper + Kafka + Kafka UI
- [x] `@KafkaListener` activated on IdocResponseListener and ErrorQueueListener
- [x] 11 new unit tests (EventPublisherService + Adapter)
- [x] Complete documentation (FASE-3-EVENT-SOURCING.md)
- [ ] Event Store → Kafka relay (republishing scheduler) - Phase 3b
- [ ] Dedicated CQRS Read Models - Phase 3b

## Build & Test Status

```
Build: ✅ SUCCESS (18s, 24 actionable tasks)
Compilation: ✅ 0 errors, 0 warnings
Unit Tests: ✅ 15+ with >95% JaCoCo coverage (Phase 0)
Integration Tests: ✅ 8 tests (Phase 1)
Code Quality: ✅ Managed by SonarQube
Security Scan: ✅ Trivy integrated in CI/CD
Docker: ✅ Multi-stage build, security scanning
Git: ✅ synchronized master branch
```

### Phase 0 (COMPLETE ✅)

✅ Project scaffolding with 8 Gradle modules
✅ Domain layer: Aggregate Root (Pedido), Value Objects, Domain Events
✅ 15 unit tests with >95% JaCoCo coverage
✅ Gradle multi-module build with Java 21
✅ Docker: PostgreSQL 16 + LocalStack
✅ GitHub Actions: build → test → quality → docker → security
✅ 3 Architecture Decision Records (ADRs)
✅ Complete technical documentation

**Status**: ✅ DONE | Commit: `25884d6`

---

### Phase 1 (COMPLETE ✅)

✅ Application Layer: `PedidoService` with 6 use cases
✅ REST Controller: 6 CRUD + validation endpoints
✅ JPA Repository: persistence with Spring Data (PedidoJpaRepositoryAdapter)
✅ Integration Tests: 8 tests with MockMvc + H2
✅ OpenAPI/Swagger documentation
✅ Data validation with Spring Validation (@NotBlank, @NotEmpty, @Valid)
✅ Typed DTOs (PedidoRequestDTO, PedidoResponseDTO, ItemPedidoDTO)
✅ Flyway migrations (V1\_\_create_pedido_tables.sql)
✅ Spring profiles (dev, test, prod)

**Output**: Functional REST API with PostgreSQL persistence | Commit: `232e308`

---

### Phase 2 (COMPLETE ✅ - SAP Integration)

✅ **Hexagonal Architecture**: SapSyncPort interface (integration port)
✅ **RFC Connector**: Synchronous integration with SAP ERP via RFC (RfcConnector.java)
✅ **iDoc Publisher**: Asynchronous integration via iDoc XML (IdocPublisher.java)
✅ **Adapter Pattern**: SapOrderAdapter implementing SapSyncPort
✅ **Domain Events**: PedidoSincronizadoEvent, PedidoErroSincronizacaoEvent
✅ **PedidoService**: 2 new use cases (sincronizarComSapRfc, publicarPedidoIdoc)
✅ **REST Endpoints**: 2 new endpoints (/sincronizar-rfc, /publicar-idoc)
✅ **Unit Tests**: 13+ tests (RfcConnectorTest, IdocPublisherTest, SapOrderAdapterTest)
✅ **Configuration**: SapConnectorConfig, application.yml, .env.example
✅ **Retry Logic**: @Retryable with exponential backoff (3 attempts, 5s delay)
✅ **Documentation**: ADR-004, FASE-2-SAP-INTEGRATION.md

**Output**: Complete SAP integration (synchronous RFC + asynchronous iDoc) | Commit: `a51888c`

---

### Phase 2.5 (COMPLETE ✅ - Response Queue Listeners)

✅ **IdocResponse DTO**: Structure for iDoc confirmations (idocId, pedidoId, status, errorCode, errorMessage)
✅ **IdocResponsePort**: Hexagonal interface for response processing
✅ **IdocResponseService**: Orchestrates state transitions (SINCRONIZANDO → SINCRONIZADO/ERRO)
✅ **IdocResponseListener**: Listener component for the success queue (sap-idoc-response)
✅ **ErrorQueueListener**: Listener component for the error queue (sap-idoc-error)
✅ **Atomic Transactions**: @Transactional to guarantee state consistency
✅ **8 Unit Tests**: Validates success/error flows, null-safety, exception handling
✅ **Documentation**: FASE-2.5-RESPONSE-QUEUE.md with Kafka/RabbitMQ/Azure Service Bus examples
✅ **Temporal Decoupling**: Responses return 202 Accepted, updates happen asynchronously

**Output**: Fully automated asynchronous synchronization with auditability | Commit: `ea23c1f`

---

### Phase 3 (Foundation Complete 🔄 - Event Sourcing & Kafka)

✅ **Event Store**: `domain_events` table with 7 indexes, append-only, full audit trail
✅ **Domain Event Publishing**: Pedido emits events on creation/sync/error
✅ **EventPublisherService**: persists domain events into the Event Store
✅ **Real Kafka**: Zookeeper + Kafka + Kafka UI via docker-compose
✅ **Active Listeners**: `@KafkaListener` consuming the `sap-idoc-response`/`sap-idoc-error` topics
✅ **11 Unit Tests**: EventPublisherService + DomainEventJpaRepositoryAdapter
✅ **Documentation**: FASE-3-EVENT-SOURCING.md with architecture and known limitations
⏳ **Pending (Phase 3b)**: Event Store→Kafka relay, CQRS read models, query endpoints

**Output**: Complete event audit trail + active real Kafka integration

---

### Phase 4 (Performance - 1 week)

- [ ] Caching: Redis for frequent queries
- [ ] Batch Processing: bulk synchronization
- [ ] Load Testing: performance validation
- [ ] Database Tuning: indexes, optimized queries

**Output**: System ready for production

### Phase 5 (Monitoring - 1 week)

- [ ] OpenTelemetry: distributed tracing
- [ ] Prometheus: business metrics
- [ ] Grafana: dashboards
- [ ] AlertManager: alert rules

**Output**: Complete observability

### Phase 6 (Advanced DDD - 1 week)

- [ ] Multi-tenancy: support for multiple companies
- [ ] Aggregate Versioning: aggregate versioning
- [ ] Policy Patterns: complex business rules
- [ ] Saga Pattern: distributed transactions

**Output**: Advanced features

### Phase 7 (Event Sourcing - 1 week)

- [ ] Event Store: event persistence
- [ ] Aggregate Reconstruction: reconstruction via events
- [ ] Time Travel Queries: complete history
- [ ] Compliance: regulatory auditing

**Output**: Fully auditable system

## Getting Started

### Prerequisites

- Java 21+
- Git
- Docker and Docker Compose
- PowerShell (Windows) or Bash (Linux/Mac)

### Local Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/ThyagoOF6/ORDER-INTEGRATION-PLATFORM.git
   cd order-integration-platform
   ```

2. **Start the database**

   ```bash
   docker-compose up -d
   ```

   PostgreSQL will be available at:
   - Host: `localhost:5432`
   - User: `orderintegration`
   - Password: `dev_password_123`
   - Database: `order_integration_platform`

3. **Build the project**

   ```bash
   ./gradlew build
   ```

4. **Run the tests**

   ```bash
   ./gradlew test
   ```

5. **Run the application** (Phase 1+)

   ```bash
   ./gradlew bootRun -p bootstrap
   ```

   The application will be available at `http://localhost:8080/api`

### Useful Commands

```bash
# Full build
./gradlew clean build

# Tests only
./gradlew test

# JaCoCo coverage report
./gradlew jacocoTestReport
# Open: core/domain/build/reports/jacoco/test/html/index.html

# SonarQube analysis
./gradlew sonarqube

# Docker build
docker build -t order-integration-platform:latest .

# Run in Docker
docker run -p 8080:8080 order-integration-platform:latest

# Stop infrastructure
docker-compose down

# Application logs
docker-compose logs -f postgres
```

## Tests

### Coverage

- **Domain Layer**: >95% (15 tests)
- **Application Layer**: (Phase 1)
- **Adapter Layer**: (Phase 1+)

### Running Tests

```bash
# All tests
./gradlew test

# A specific module
./gradlew :core:domain:test

# With detailed output
./gradlew test --info
```

### Example: PedidoTest

```java
@Test
void deveCriarPedidoComStatusCriado() {
    // Given
    String codigoCliente = "CLI-001";
    List<ItemPedido> itens = List.of(
        ItemPedido.criar("PROD-001", "Notebook", 2, BigDecimal.valueOf(5000))
    );

    // When
    Pedido pedido = Pedido.criar(codigoCliente, itens);

    // Then
    assertEquals(StatusPedido.CRIADO, pedido.getStatus());
    assertEquals(1, pedido.getItens().size());
}
```

## Configuration

### application.yml

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  datasource:
    url: jdbc:postgresql://localhost:5432/order_integration_platform
    username: orderintegration
    password: dev_password_123

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.microsoftonline.com/{tenant-id}/v2.0
          jwk-set-uri: https://login.microsoftonline.com/{tenant-id}/discovery/v2.0/keys

server:
  port: 8080
  servlet:
    context-path: /api

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Profiles

- **dev**: Local development with detailed logs
- **test**: Tests with in-memory H2 database
- **prod**: Production with optimizations

```bash
./gradlew bootRun -p bootstrap --args='--spring.profiles.active=dev'
```

## Architecture Decisions

See [docs/adr/](docs/adr/) for complete Architecture Decision Records:

1. **Hexagonal Architecture** - Why this pattern?
2. **Domain Events** - How aggregates communicate
3. **PostgreSQL** - Why not other databases?

## Performance

### Expected Metrics

| Operation                  | Target | Status          |
| --------------------------- | ------ | --------------- |
| Create Order               | <100ms | ✅ Implemented  |
| Fetch Order                 | <50ms  | ✅ Implemented  |
| Validate Order              | <50ms  | ✅ Implemented  |
| Synchronize with SAP (RFC)  | <5s    | ✅ Implemented  |
| Publish iDoc (async)        | <500ms | ✅ Implemented  |
| List Orders (100 items)     | <200ms | ⏳ Phase 4      |

### Optimization Roadmap

- Query caching (Phase 4)
- Batch processing for bulk synchronization (Phase 4)
- Advanced database indexing (Phase 4)
- HikariCP connection pooling (✅ already configured)

## Security

### Implemented

✅ Spring Security baseline (Spring Boot 3.3.2)
✅ Java 21 LTS with security patches until 2029
✅ Dependencies managed with Spring Boot BOM
✅ SonarQube quality gate for vulnerabilities
✅ Docker with non-root user
✅ Environment variables for secrets (.env.example)
✅ Input validation with Spring Validation
✅ Exception handling with ResponseEntity
✅ Retry logic with exponential backoff (RFC adapter)

### Roadmap

- OAuth2/OIDC with Microsoft Entra ID (Phase 3b)
- Azure Key Vault for secrets management (Phase 3b)
- Granular RBAC with resource-level permissions (Phase 3b)
- Encryption at rest (PostgreSQL) (Phase 5)
- mTLS between services (Phase 5)
- Rate limiting and DDoS protection (Phase 5)

## CI/CD

Automated GitHub Actions pipeline:

```
Push → Checkout → Setup Java → Build → Test → Coverage →
SonarQube → Docker Build → Trivy Scan → Push Registry
```

**File**: `.github/workflows/ci-cd.yml`

Runs on every push to `master` and pull requests.

## Commit Convention

```
feat: adds new functionality
fix: fixes a bug
docs: updates documentation
test: adds/updates tests
refactor: refactors code
perf: improves performance
chore: miscellaneous tasks
```

Example:

```
git commit -m "feat: implements PedidoService with use cases"
```

## Contributing

1. Create a branch: `git checkout -b feature/your-feature`
2. Commit with descriptive messages
3. Push the branch: `git push origin feature/your-feature`
4. Open a Pull Request

All pushes automatically go through CI/CD.

## Full Roadmap

| Phase   | Duration | Focus                       | Status               |
| ------- | -------- | ---------------------------- | -------------------- |
| **0**   | 1 day    | Scaffolding + Domain         | ✅ Complete          |
| **1**   | 2 weeks  | REST API + Persistence       | ✅ Complete          |
| **2**   | 2 weeks  | SAP Integration (RFC/iDoc)   | ✅ Complete          |
| **2.5** | 1 week   | Response Queue Listeners     | ✅ Complete          |
| **3**   | 2 weeks  | Event Sourcing + Messaging   | 🔄 Foundation complete |
| **4**   | 1 week   | Performance + Caching        | ⏳ Planned           |
| **5**   | 1 week   | Monitoring + Observability   | ⏳ Planned           |
| **6**   | 1 week   | Advanced DDD Patterns        | ⏳ Planned           |
| **7**   | 1 week   | Event Sourcing + Auditing    | ⏳ Planned           |

**Progress**: 4.5/9 phases complete (50%)
**Estimated total**: 10 weeks for a fully production-ready system

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Domain-Driven Design (Eric Evans)](https://www.domainlanguage.com/ddd/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [PostgreSQL 16](https://www.postgresql.org/docs/16/)
- [Gradle Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)

## License

MIT - See LICENSE for details

## Contact

Thyago Oliveira Ferreira - Java Developer
Portfolio: Order Integration Platform
GitHub: [@ThyagoOF6](https://github.com/ThyagoOF6)

---

**Last updated**: August 2026
**Version**: 1.4.0 (Phase 3 - Event Sourcing Foundation Complete)
**Status**: Ready for Phase 3b (Kafka relay + CQRS read models)
**Build**: ✅ SUCCESS | **Tests**: 34+ unit tests (>95% coverage Phases 0-3) | **Commits**: 8
