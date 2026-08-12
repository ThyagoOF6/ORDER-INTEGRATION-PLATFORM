# ADR 0002: Usar Domain Events para Comunicação Entre Agregados

## Status

Aceito

## Contexto

Múltiplos subdomínios precisam reagir a eventos que acontecem no domínio. Ex:

- Quando um pedido é criado, o inventário precisa ser reservado
- Quando um pedido é sincronizado com ERP, auditoria precisa registrar
- Quando há erro, notificação precisa alertar operações

Sem mecanismo centralizado, teríamos acoplamento direto entre agregados.

## Decisão

Implementaremos **Domain Events** como padrão de comunicação:

1. Agregados publicam eventos de domínio que representam fatos que aconteceram
2. Eventos são imutáveis e contêm dados relevantes do fato
3. Handlers assíncronos consomem eventos e disparar ações
4. Eventos são persistidos juntamente com o agregado (para auditoria)

```java
// Agregado publica evento
Pedido pedido = Pedido.criar(codigoCliente, itens);
// pedido.eventos() -> [PedidoCriadoEvent]

// Handler consome evento
@EventListener
public void aoPedidoSerCriado(PedidoCriadoEvent evento) {
    // Validar, enviar para ERP, etc.
}
```

## Consequências

### Positivas

- ✅ Desacoplamento entre agregados
- ✅ Auditoria natural (eventos são fatos)
- ✅ Fácil adicionar comportamentos novos
- ✅ Suporta escalabilidade com mensageria
- ✅ Testável sem complexidade de mocks

### Negativas

- ❌ Eventual consistency (não transacional)
- ❌ Complexidade em debugar fluxos assíncronos
- ❌ Necessário tratamento de idempotência

## Alternativas Consideradas

- **Direct method calls**: Simples, mas acoplamento
- **Message Queue**: Mais complexo, mas melhor para distribuído
- **Event Sourcing completo**: Poder demais para MVP

## Implementação Futura

- Fase 2: Migrar para mensageria real (Azure Service Bus)
- Fase 3: Implementar Outbox Pattern para garantia de entrega
