# Documentação KDoc para Exceções

## BusinessException

```kotlin
/**
 * Exceção base para erros de negócio no sistema.
 * 
 * Esta classe serve como base para todas as exceções específicas de negócio,
 * fornecendo um formato padronizado para códigos de erro, mensagens e rastreamento.
 *
 * @property code Código interno do erro
 * @property message Mensagem técnica detalhando o erro
 * @property friendlyMessage Mensagem amigável para exibição ao usuário
 * @property traceId Identificador de rastreamento para correlacionar logs
 * @property type Tipo da exceção
 */
open class BusinessException(
    val code: String,
    override val message: String,
    val friendlyMessage: String,
    val traceId: String,
    val type: ExceptionType,
) : RuntimeException(message)
```

## EntryEventNotFoundException

```kotlin
/**
 * Exceção lançada quando um evento de entrada não é encontrado.
 * 
 * Esta exceção é lançada quando um evento PARKED é recebido,
 * mas não há um evento ENTRY correspondente para a placa.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class EntryEventNotFoundException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```

## LicensePlateConflictException

```kotlin
/**
 * Exceção lançada quando há conflito de placa de veículo.
 * 
 * Esta exceção é lançada quando um evento ENTRY é recebido,
 * mas já existe um evento ativo para a mesma placa.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class LicensePlateConflictException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```

## LicensePlateNotFoundException

```kotlin
/**
 * Exceção lançada quando uma placa de veículo não é encontrada.
 * 
 * Esta exceção é lançada quando uma consulta é feita para uma placa
 * que não está registrada no sistema.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class LicensePlateNotFoundException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```

## NoParkedEventFoundException

```kotlin
/**
 * Exceção lançada quando um evento de estacionamento não é encontrado.
 * 
 * Esta exceção é lançada quando um evento EXIT é recebido,
 * mas não há um evento PARKED correspondente para a placa.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class NoParkedEventFoundException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```

## NoParkingOpenException

```kotlin
/**
 * Exceção lançada quando não há estacionamentos abertos.
 * 
 * Esta exceção é lançada quando um evento ENTRY é recebido,
 * mas não há estacionamentos abertos no momento.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class NoParkingOpenException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```

## ParkingNotFoundException

```kotlin
/**
 * Exceção lançada quando um estacionamento não é encontrado.
 * 
 * Esta exceção é lançada quando uma consulta é feita para um estacionamento
 * que não está registrado no sistema.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class ParkingNotFoundException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```

## ParkingSpotNotFoundException

```kotlin
/**
 * Exceção lançada quando uma vaga de estacionamento não é encontrada.
 * 
 * Esta exceção é lançada quando uma consulta é feita para uma vaga
 * que não está registrada no sistema.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class ParkingSpotNotFoundException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```

## RevenueNotFoundException

```kotlin
/**
 * Exceção lançada quando um registro de receita não é encontrado.
 * 
 * Esta exceção é lançada quando uma consulta é feita para uma receita
 * que não está registrada no sistema.
 *
 * @param code Código interno do erro
 * @param message Mensagem técnica detalhando o erro
 * @param friendlyMessage Mensagem amigável para exibição ao usuário
 * @param traceId Identificador de rastreamento para correlacionar logs
 * @param type Tipo da exceção
 */
class RevenueNotFoundException(
    code: String,
    message: String,
    friendlyMessage: String,
    traceId: String,
    type: ExceptionType,
) : BusinessException(code, message, friendlyMessage, traceId, type)
```
