# Order Integration Platform

Plataforma de integração de pedidos enterprise-grade desenvolvida em Java com Spring Boot, DDD e Arquitetura Hexagonal.

## 🎯 Objetivo

Criar uma plataforma que:

- Recebe pedidos de múltiplos canais
- Valida regras de negócio complexas
- Sincroniza com sistemas ERP (SAP-ready)
- Mantém auditoria completa via eventos
- Demonstra padrões avançados de arquitetura e código

## 🏗️ Arquitetura

```
order-integration-platform/
├── core/
│   ├── domain/          # Lógica pura de negócio (sem dependências)
│   └── application/     # Use cases, commands, handlers
├── adapter/
│   ├── in/rest/         # Controllers REST, OpenAPI
│   └── out/             # Persistência, ERP, Mensageria
├── infrastructure/
│   ├── config/          # Beans, Segurança, Resiliência
│   └── observability/   # Logging, Metrics, Tracing
└── bootstrap/           # Aplicação principal Spring Boot
```

### Padrões Aplicados

- ✅ Domain-Driven Design (DDD)
- ✅ Hexagonal Architecture (Ports & Adapters)
- ✅ Domain Events
- ✅ CQRS (simplificado)
- ✅ Repository Pattern
- ✅ Value Objects & Aggregates

## 🛠️ Stack Técnico

| Componente          | Tecnologia                                     |
| ------------------- | ---------------------------------------------- |
| **Language**        | Java 21                                        |
| **Framework**       | Spring Boot 3.3                                |
| **Build**           | Gradle (multi-módulo)                          |
| **Banco**           | PostgreSQL 16                                  |
| **Testing**         | JUnit 5, Mockito, TestContainers, AssertJ      |
| **Quality**         | SonarQube, JaCoCo (>80%), Checkstyle, SpotBugs |
| **API**             | REST + OpenAPI/Swagger                         |
| **Segurança**       | OAuth2 + JWT (Microsoft Entra ID)              |
| **Observabilidade** | OpenTelemetry, Prometheus, Micrometer          |
| **CI/CD**           | GitHub Actions                                 |
| **IaC**             | Bicep / Terraform (Phase 6+)                   |

## 🚀 Quick Start

### Pré-requisitos

- Java 21+
- Docker & Docker Compose
- Git

### Setup Local

1. **Clone o repositório**

   ```bash
   git clone https://github.com/seu-user/order-integration-platform.git
   cd order-integration-platform
   ```

2. **Subir infraestrutura local**

   ```bash
   docker-compose up -d
   ```

3. **Build e testes**

   ```bash
   ./gradlew build
   ```

4. **Rodar a aplicação**

   ```bash
   ./gradlew bootRun -p bootstrap
   ```

5. **Acessar**
   - API: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - Actuator: http://localhost:8080/api/actuator/health

### Testes

```bash
# Todos os testes
./gradlew test

# Com cobertura
./gradlew test jacocoTestReport

# Ver relatório
open build/reports/jacoco/test/html/index.html

# SonarQube local
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000
```

## 📋 Fases de Desenvolvimento

### Fase 0: Fundação ✅

- [x] Setup Gradle multi-módulo
- [x] Estrutura de pastas (Domain, Application, Adapters)
- [x] Testes base (JUnit 5, Mockito)
- [x] Docker Compose
- [x] CI/CD inicial

### Fase 1: Core de Pedidos (Semana 1)

- [ ] Modelar `Pedido`, `Item`, `Cliente`, `Status`
- [ ] Persistência com JPA/Flyway
- [ ] CRUD REST com validações
- [ ] OpenAPI/Swagger

### Fase 2: Processamento Assíncrono (Semana 2)

- [ ] Integrar Azure Service Bus
- [ ] Publicar eventos `PedidoCriado`
- [ ] Worker consumidor
- [ ] Retry, dead-letter, correlação

### Fase 3: Conector ERP (Semana 3)

- [ ] Adapter ERP Mock
- [ ] Padrão Circuit Breaker
- [ ] Timeout e retry strategy
- [ ] Auditoria de sincronização

### Fase 4: Segurança (Semana 4)

- [ ] OAuth2 + Microsoft Entra ID
- [ ] RBAC (Admin, Operador, Leitor)
- [ ] Rate limiting
- [ ] Key Vault para secrets

### Fase 5: Observabilidade (Semana 5)

- [ ] OpenTelemetry + tracing distribuído
- [ ] Application Insights
- [ ] Métricas de negócio
- [ ] Dashboards e alertas

### Fase 6: Deploy (Semana 6)

- [ ] Docker multi-stage
- [ ] Azure Container Apps
- [ ] Bicep/Terraform
- [ ] Pipeline CD completa

### Fase 7: Diferenciais (Semana 7-8)

- [ ] Outbox Pattern
- [ ] Testes de contrato (Pact)
- [ ] Multi-tenant básico
- [ ] Load testing
- [ ] ADRs

## 📚 Documentação

- [ADRs](./docs/adr/) - Decisões arquiteturais
- [C4 Diagrams](./docs/diagrams/) - Visualizações (em progresso)
- [API Docs](http://localhost:8080/api/swagger-ui.html) - Swagger interativo

## 🧪 Cobertura de Testes

```
Domain Layer:        95%+ (crítico)
Application Layer:   80%+
Adapter Layer:       70%+ (infrastructure-dependent)
Overall Target:      >80%
```

## 📊 Métricas

Monitorar via actuator:

```bash
curl http://localhost:8080/api/actuator/metrics
curl http://localhost:8080/api/actuator/metrics/http.requests.total
curl http://localhost:8080/api/actuator/prometheus
```

## 🔐 Segurança

- [x] Dependency scanning (GitHub Dependabot)
- [x] Secret scanning
- [x] OWASP Top 10 hardened
- [ ] Penetration testing (Phase 6+)

## 📈 Performance

- Métricas P99: <200ms (API)
- Throughput: >1000 pedidos/min (local)
- Database connection pool: 10 connections
- Retry exponential backoff: 100ms, 500ms, 2s

## 🤝 Contribuindo

1. Fork o repositório
2. Crie uma branch (`git checkout -b feature/xyz`)
3. Commit suas mudanças (`git commit -am 'Add feature'`)
4. Push para a branch (`git push origin feature/xyz`)
5. Abra um Pull Request

## 📝 Licença

MIT

## 👤 Autor

[Seu Nome] - Portfolio de Engenharia de Software

## 🔗 Links Úteis

- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Spring Boot Best Practices](https://spring.io/guides)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Azure Documentation](https://docs.microsoft.com/azure)

---

**Status**: Em Desenvolvimento 🚧
**Última Atualização**: Agosto 2026
