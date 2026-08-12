# FASE 3 - Event Sourcing & Message Broker Integration

## Status: ✅ Implementada (parcial - fundação completa)

Data: Agosto 2026
Commit: (a definir no push)

## Visão Geral

A Fase 3 introduz **Event Sourcing** como mecanismo de auditoria completa do sistema
e ativa a integração real com **Apache Kafka** como message broker, substituindo os
stubs criados na Fase 2.5 por listeners funcionais (`@KafkaListener`).

### Objetivos alcançados

1. **Event Store**: Tabela `domain_events` (PostgreSQL) para persistir todos os
   eventos de domínio de forma imutável (append-only).
2. **Publicação de Eventos de Domínio**: `Pedido` agora emite eventos em todas as
   transições relevantes de estado (criação, sincronização, erro).
3. **Kafka real**: Docker Compose com Zookeeper + Kafka + Kafka UI; listeners
   `IdocResponseListener` e `ErrorQueueListener` agora consomem tópicos reais via
   `@KafkaListener` (antes eram apenas stubs aguardando binding).
4. **Testes**: 11 novos testes unitários cobrindo Event Store e publicação de eventos.

### Não incluído nesta fase (próximos passos / Fase 3b)

- CQRS Read Models dedicados (`PedidoQueryModel`) e endpoints de query otimizados.
- Scheduler para republicar eventos não publicados (`is_published = false`) no Kafka.
- Publicação efetiva dos eventos do Event Store para o tópico `domain-events`
  (atualmente os eventos são persistidos no Event Store, mas o "relay" para o Kafka
  ainda não foi implementado — ver seção Limitações).

## Arquitetura

```
┌─────────────┐     eventos de domínio      ┌──────────────────┐
│   Pedido    │ ───────────────────────────▶ │ EventPublisherSvc │
│ (Aggregate) │                              └──────────────────┘
└─────────────┘                                       │
                                                       ▼
                                        ┌──────────────────────────┐
                                        │ DomainEventRepositoryPort │
                                        │  (Hexagonal Port)         │
                                        └──────────────────────────┘
                                                       │
                                                       ▼
                                  ┌────────────────────────────────────┐
                                  │ DomainEventJpaRepositoryAdapter     │
                                  │ (mapeia DTO ↔ Entity, persiste)     │
                                  └────────────────────────────────────┘
                                                       │
                                                       ▼
                                        tabela domain_events (Postgres)


┌──────────────┐   iDoc response (Kafka)   ┌───────────────────────┐
│  SAP / iDoc  │ ─────────────────────────▶ │ IdocResponseListener  │
│   (externo)  │   sap-idoc-response topic  │  @KafkaListener       │
└──────────────┘                            └───────────────────────┘
                                                       │
                                                       ▼
                                          IdocResponseService (Fase 2.5)
                                          SINCRONIZANDO → SINCRONIZADO/ERRO
```

## Componentes Criados

### 1. Event Store Infrastructure

| Arquivo                                | Descrição                                                                                                                                                  |
| -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `V2__create_event_store_table.sql`     | Migration Flyway: cria tabela `domain_events` com 7 índices (event_id, aggregate_id, aggregate_type, event_type, created_at, is_published, correlation_id) |
| `DomainEventJpaEntity.java`            | Entidade JPA (sem Lombok, seguindo convenção do projeto) com builder manual                                                                                |
| `DomainEventDto.java`                  | DTO usado pela camada de aplicação (Hexagonal: application não depende de JPA)                                                                             |
| `DomainEventRepositoryPort.java`       | Porta hexagonal com 13 operações (persistir, buscar por ID/aggregate/tipo/correlação, marcar como publicado, contar, deletar)                              |
| `DomainEventSpringDataRepository.java` | Repositório Spring Data JPA com queries customizadas (`@Query`)                                                                                            |
| `DomainEventJpaRepositoryAdapter.java` | Implementação do port, mapeia DTO ↔ Entity                                                                                                                 |

### 2. Domain Event Publishing

| Arquivo                             | Mudança                                                                                         |
| ----------------------------------- | ----------------------------------------------------------------------------------------------- |
| `DomainEvent.java`                  | Adicionados métodos abstratos `getAggregateId()` e `toPayload()`                                |
| `PedidoCriadoEvent.java`            | Implementa `toPayload()` (codigoCliente, pedidoId)                                              |
| `PedidoSincronizadoEvent.java`      | Implementa `toPayload()` (transacaoSapId, statusSap)                                            |
| `PedidoErroSincronizacaoEvent.java` | Implementa `toPayload()` (codigoErro, mensagemErro, tentativa)                                  |
| `Pedido.java`                       | `confirmarSincronizacao()` e `registrarErro()` agora emitem eventos (antes só `criar()` emitia) |
| `EventPublisherService.java` (novo) | Extrai eventos pendentes do agregado, persiste no Event Store, limpa a lista                    |
| `PedidoService.java`                | Injeta `EventPublisherService`, chama `publicarEventos()` após cada operação que muda estado    |
| `IdocResponseService.java`          | Idem, para os fluxos de confirmação/erro de iDoc                                                |

### 3. Message Broker (Kafka)

| Arquivo                              | Mudança                                                                                                            |
| ------------------------------------ | ------------------------------------------------------------------------------------------------------------------ |
| `docker-compose.yml`                 | Adicionado Zookeeper + Kafka + Kafka UI (porta 8090)                                                               |
| `adapter/out/messaging/build.gradle` | Dependência `spring-kafka` (+ `spring-kafka-test` para testes) e `jackson-databind`                                |
| `bootstrap/build.gradle`             | Dependências Kafka no módulo executável                                                                            |
| `application.yml`                    | Configuração `spring.kafka.*` (bootstrap-servers, producer, consumer, listener) e `messaging.topics.*`             |
| `IdocResponseListener.java`          | Agora possui `@KafkaListener(topics = "${messaging.topics.idoc-response-success}")`, desserializa JSON via Jackson |
| `ErrorQueueListener.java`            | Idem, tópico `sap-idoc-error`                                                                                      |

### 4. Testes (11 novos)

- `EventPublisherServiceTest` (4 testes): persistência de evento de criação, limpeza de eventos, no-op sem eventos pendentes, payload de erro de sincronização.
- `DomainEventJpaRepositoryAdapterTest` (7 testes): persistir e mapear, buscar por ID (encontrado/não encontrado), listar por aggregate, marcar como publicado (sucesso/erro), buscar não publicados, contar por aggregate.

## Correções realizadas durante a implementação

1. **Pacote incorreto**: `DomainEventRepositoryPort` estava declarado como
   `com.orderintegration.core.application.port` mas o arquivo físico está em
   `application/port` — corrigido para `com.orderintegration.application.port`
   (consistente com `SapSyncPort`, `PedidoRepositoryPort`, etc).
2. **Lombok removido**: o projeto não usa Lombok em nenhum outro lugar (convenção:
   getters/setters manuais + `org.slf4j.Logger`). As classes novas (`DomainEventJpaEntity`,
   `DomainEventDto`) foram reescritas sem Lombok, com builder manual.
3. **`columnDefinition` específico de Postgres**: `jsonb` e
   `TIMESTAMP WITH TIME ZONE` foram removidos da entidade JPA (mantidos apenas na
   migration SQL) para não quebrar o perfil de teste com H2.
4. **Bugs pré-existentes desbloqueados** (não relacionados à Fase 3, mas que impediam
   a compilação de testes):
   - `IdocPublisherTest`, `RfcConnectorTest`, `SapOrderAdapterTest` passavam
     `BigDecimal` como quantidade em `ItemPedido.criar()` (assinatura espera `Integer`).
   - `adapter/in/rest` não tinha `spring-tx`, `spring-boot-starter-data-jpa` e `h2`
     como dependências de teste.

## Limitações conhecidas (Fase 3b / Fase 4)

- **Relay Event Store → Kafka não implementado**: os eventos de domínio são
  persistidos no Event Store (`domain_events`, `is_published = false`), mas ainda não
  há um scheduler/publisher que os envie de fato para o tópico `domain-events` do
  Kafka. Isso deve ser implementado como um `@Scheduled` job que consulta
  `findUnpublishedEvents()` e publica via `KafkaTemplate`.
- **Sem CQRS Read Models dedicados**: as consultas continuam usando o mesmo modelo
  de escrita (`PedidoJpaEntity`). Um modelo de leitura otimizado (`PedidoQueryModel`)
  fica para uma fase futura.
- **`PedidoControllerIntegrationTest` pré-existente continua falhando** por não
  encontrar `@SpringBootConfiguration` (dependência circular entre `adapter/in/rest`
  e `bootstrap`) — problema pré-existente, fora do escopo desta fase.

## Como testar localmente

```powershell
# Subir Postgres + Kafka + Kafka UI
docker-compose up -d

# Rodar a suíte de testes (exceto o teste de integração pré-existente quebrado)
./gradlew test -x :adapter:in:rest:test

# Ver tópicos Kafka criados
# Acessar http://localhost:8090 (Kafka UI)
```

## Próximos passos (Fase 3b)

1. Implementar `@Scheduled` job para publicar eventos pendentes do Event Store no Kafka.
2. Criar `PedidoQueryModel` + `PedidoQueryRepository` (CQRS read side).
3. Adicionar endpoints de consulta otimizados (`GET /pedidos?status=SINCRONIZADO`).
4. Corrigir `PedidoControllerIntegrationTest` (dependência circular de módulos).
