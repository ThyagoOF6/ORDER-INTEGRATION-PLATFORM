# ADR 0001: Usar Arquitetura Hexagonal (Ports & Adapters)

## Status

Aceito

## Contexto

O projeto precisa de uma arquitetura que seja:

- Testável em isolamento
- Independente de frameworks e tecnologias específicas
- Fácil de evoluir e modificar sem impacto em cascata
- Clara separação entre lógica de negócio e infraestrutura

## Decisão

Adotaremos **Arquitetura Hexagonal** (Ports & Adapters), organizada em camadas:

1. **Domain** (núcleo): Entidades, Agregados, Value Objects, Regras de negócio puras
2. **Application**: Use Cases, Commands, Queries, Handlers
3. **Adapters**:
   - **In**: REST Controllers (entrada)
   - **Out**: Persistência, Integração com ERP, Mensageria (saída)
4. **Infrastructure**: Configuração, Segurança, Observabilidade

## Consequências

### Positivas

- ✅ Lógica de negócio totalmente isolada (facilita testes unitários)
- ✅ Fácil trocar implementações (ex: mudar banco, ERP)
- ✅ Framework-agnostic (poderia ser Quarkus, Micronaut, etc.)
- ✅ Escalável e manutenível em longo prazo
- ✅ Demonstra entendimento de arquitetura enterprise

### Negativas

- ❌ Mais camadas = mais código inicial
- ❌ Curva de aprendizado para times não familiarizados
- ❌ Pode parecer over-engineering para projetos simples

## Alternativas Consideradas

- **Layered Architecture**: Simples, mas acoplamento maior
- **Microserviços**: Overhead desnecessário no MVP
- **Clean Architecture**: Similar, mas menos flexible para adaptar

## Referências

- Alistair Cockburn - Hexagonal Architecture
- Vaughn Vernon - Domain-Driven Design Distilled
