# Documentação KDoc para Enums

## EventType

```kotlin
/**
 * Enum que representa os tipos de eventos de estacionamento.
 * 
 * Define os possíveis estados de um veículo no sistema de estacionamento.
 */
enum class EventType {
    /**
     * Evento de entrada do veículo no estacionamento.
     * Registrado quando o veículo passa pela cancela de entrada.
     */
    ENTRY,
    
    /**
     * Evento de estacionamento do veículo em uma vaga.
     * Registrado quando o veículo é detectado em uma vaga específica.
     */
    PARKED,
    
    /**
     * Evento de saída do veículo do estacionamento.
     * Registrado quando o veículo passa pela cancela de saída.
     */
    EXIT,
}
```

## CurrencyType

```kotlin
/**
 * Enum que representa os tipos de moeda suportados pelo sistema.
 * 
 * Define as moedas que podem ser usadas para registrar receitas.
 */
enum class CurrencyType {
    /**
     * Real Brasileiro.
     */
    BRL,
    
    /**
     * Dólar Americano.
     */
    USD,
    
    /**
     * Euro.
     */
    EUR,
}
```

## OccupancyActionType

```kotlin
/**
 * Enum que representa os tipos de ação relacionados à ocupação de vagas.
 * 
 * Define as possíveis ações que podem ser tomadas com base na ocupação do estacionamento.
 */
enum class OccupancyActionType {
    /**
     * Ação de permitir estacionamento.
     * Aplicada quando há vagas disponíveis.
     */
    ALLOW,
    
    /**
     * Ação de aplicar desconto.
     * Aplicada quando a ocupação está abaixo de um limite mínimo.
     */
    DISCOUNT,
    
    /**
     * Ação de aplicar preço normal.
     * Aplicada quando a ocupação está em níveis normais.
     */
    NORMAL,
    
    /**
     * Ação de aplicar sobretaxa.
     * Aplicada quando a ocupação está acima de um limite máximo.
     */
    SURCHARGE,
    
    /**
     * Ação de negar estacionamento.
     * Aplicada quando o estacionamento está completamente ocupado.
     */
    DENY,
}
```

## ExceptionType

```kotlin
/**
 * Enum que representa os tipos de exceção no sistema.
 * 
 * Define as categorias de exceções que podem ocorrer durante a execução.
 */
enum class ExceptionType {
    /**
     * Exceção de validação.
     * Ocorre quando os dados fornecidos não atendem aos requisitos.
     */
    VALIDATION,
    
    /**
     * Exceção de negócio.
     * Ocorre quando uma regra de negócio é violada.
     */
    BUSINESS,
    
    /**
     * Exceção de persistência.
     * Ocorre durante operações de banco de dados.
     */
    PERSISTENCE,
    
    /**
     * Exceção de requisição de persistência.
     * Ocorre quando há problemas com os dados a serem persistidos.
     */
    PERSISTENCE_REQUEST,
    
    /**
     * Exceção de sistema.
     * Ocorre devido a problemas internos do sistema.
     */
    SYSTEM,
}
```

## InternalCodeType

```kotlin
/**
 * Enum que representa os códigos internos de erro do sistema.
 * 
 * Define códigos específicos para diferentes tipos de erro,
 * facilitando a identificação e tratamento.
 */
enum class InternalCodeType {
    /**
     * Código para erro de evento de webhook não encontrado.
     */
    WEBHOOK_CODE_EVENT_NOT_FOUND,
    
    /**
     * Código para erro de conflito de placa em evento de entrada.
     */
    WEBHOOK_ENTRY_LICENSE_PLATE_CONFLICT,
    
    /**
     * Código para erro de nenhum estacionamento aberto em evento de entrada.
     */
    WEBHOOK_ENTRY_NO_PARKING_OPEN,
    
    /**
     * Código para erro de estacionamento não encontrado.
     */
    PARKING_NOT_FOUND,
    
    /**
     * Código para erro de vaga de estacionamento não encontrada.
     */
    PARKING_SPOT_NOT_FOUND,
    
    /**
     * Código para erro de placa não encontrada.
     */
    LICENSE_PLATE_NOT_FOUND,
    
    /**
     * Código para erro de receita não encontrada.
     */
    REVENUE_NOT_FOUND,
    
    // Outros códigos omitidos para brevidade
    
    /**
     * Retorna o código numérico associado ao tipo de erro.
     *
     * @return O código numérico do erro
     */
    fun code(): String {
        // Implementação omitida para brevidade
        return "ERR-${this.name}"
    }
    
    /**
     * Retorna a chave de mensagem associada ao tipo de erro.
     *
     * @return A chave de mensagem para internacionalização
     */
    fun messageKey(): String {
        // Implementação omitida para brevidade
        return "error.${this.name.lowercase()}"
    }
}
```
