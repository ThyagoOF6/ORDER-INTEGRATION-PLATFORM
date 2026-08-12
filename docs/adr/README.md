# Architecture Decision Records (ADRs)

Registro de decisões arquiteturais importantes que definem a estrutura e filosofia do projeto.

## O que são ADRs?

ADRs documentam:

- **Contexto**: Por que estamos considerando isso?
- **Decisão**: O que decidimos fazer?
- **Consequências**: Quais são os trade-offs?

Formato baseado em [ADR - Michael Nygard](http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions)

## ADRs Implementadas

| #                                        | Título                | Status    | Impacto |
| ---------------------------------------- | --------------------- | --------- | ------- |
| [0001](./0001-hexagonal-architecture.md) | Arquitetura Hexagonal | ✅ Aceito | Alto    |
| [0002](./0002-domain-events.md)          | Domain Events         | ✅ Aceito | Alto    |
| [0003](./0003-postgresql-database.md)    | PostgreSQL            | ✅ Aceito | Alto    |

## ADRs Planejados

| #    | Título                           | Fase |
| ---- | -------------------------------- | ---- |
| 0004 | Outbox Pattern                   | 7    |
| 0005 | CQRS Simplificado                | 2    |
| 0006 | Circuit Breaker com Resilience4j | 4    |
| 0007 | OpenTelemetry para Tracing       | 5    |
| 0008 | IaC com Bicep                    | 6    |

## Como Propor um ADR

1. Crie um arquivo `docs/adr/NNNN-titulo-kebab-case.md`
2. Use o template abaixo
3. Abra PR para discussão
4. Merge após consenso

## Template

```markdown
# ADR NNNN: [Título]

## Status

Proposto | Aceito | Superado | Descontinuado

## Contexto

[Descrever o problema, restrições, contexto]

## Decisão

[Descrever o que decidimos fazer]

## Consequências

### Positivas

- ✅ Benefício 1
- ✅ Benefício 2

### Negativas

- ❌ Tradeoff 1
- ❌ Tradeoff 2

## Alternativas Consideradas

- [Alternativa 1]: Motivo de rejeição
- [Alternativa 2]: Motivo de rejeição

## Referências

- [Link 1]
- [Link 2]
```

## Discussão

Para revisar ou propor novos ADRs, abra uma issue com tag `[ADR]`.
