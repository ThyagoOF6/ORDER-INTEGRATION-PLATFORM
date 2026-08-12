# ADR 0003: Usar PostgreSQL como Banco de Dados

## Status

Aceito

## Contexto

Projeto requer banco relacional que:

- Roda localmente com facilidade (para desenvolvimento)
- Tem versão em nuvem (Azure Database for PostgreSQL)
- Suporta JSON natively (para eventos, logs estruturados)
- É open source (reduz custos)
- Tem comunidade forte

## Decisão

**PostgreSQL** como banco de dados principal.

Benefícios específicos:

- Array e JSON tipos nativos (útil para persistir eventos)
- Window functions e CTEs (analytics)
- JSONB com índices GiST/GIN (performance)
- Full-text search built-in
- Suporta multi-version concurrency control (MVCC)

## Padrão de Versioning

Usar **Flyway** para migrations:

- Versionamento automático (V001**, V002**, etc.)
- Rollback seguro
- Histórico auditável

## Consequências

### Positivas

- ✅ Desenvolvimento local fácil com Docker
- ✅ Upgrade simples para Azure (managed service)
- ✅ Performance excelente para queries complexas
- ✅ Licensing: gratuito

### Negativas

- ❌ Não tem suporte nativo a sharding como MongoDB
- ❌ Transações ACID podem impactar throughput em alta concorrência
- ❌ Menos familiar para times vindos de SQL Server/Oracle

## Alternativas Consideradas

- **MySQL**: Simples, mas menos features avançadas
- **Azure SQL Database (SQL Server)**: Proprietary, mais caro
- **MongoDB**: NoSQL, maior complexidade para queries relacionais
- **Cosmos DB**: Azure-nativo, mas eventual consistency

## Roadmap

- Fase 1: Local com PostgreSQL
- Fase 6: Migrar para Azure Database for PostgreSQL
- Fase 7+: Considerar sharding se necessário
