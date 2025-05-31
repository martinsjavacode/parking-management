# Documentação KDoc para Classes de Modelo do Domínio

## Parking

```kotlin
/**
 * Representa um estacionamento no sistema.
 * 
 * Esta classe contém todas as informações necessárias para definir um estacionamento,
 * incluindo seu setor, preço base, capacidade máxima e horários de funcionamento.
 *
 * @property id Identificador único do estacionamento, pode ser nulo para novas instâncias
 * @property sector Nome do setor do estacionamento (ex: "A", "B", "Norte", etc.)
 * @property basePrice Preço base por período de estacionamento
 * @property maxCapacity Capacidade máxima de veículos que o estacionamento pode acomodar
 * @property openHour Horário de abertura do estacionamento
 * @property closeHour Horário de fechamento do estacionamento
 * @property durationLimitMinutes Duração limite em minutos para o período base de cobrança
 * @property spots Fluxo de vagas de estacionamento associadas a este estacionamento
 */
data class Parking(
    val id: Long?,
    val sector: String,
    val basePrice: BigDecimal,
    val maxCapacity: Int,
    val openHour: LocalTime,
    val closeHour: LocalTime,
    val durationLimitMinutes: Int,
    val spots: Flow<ParkingSpot>,
)
```

## ParkingSpot

```kotlin
/**
 * Representa uma vaga individual de estacionamento.
 * 
 * Esta classe contém informações sobre a localização geográfica da vaga
 * e sua associação com um estacionamento específico.
 *
 * @property id Identificador único da vaga, pode ser nulo para novas instâncias
 * @property parkingId Identificador do estacionamento ao qual esta vaga pertence
 * @property latitude Coordenada de latitude da vaga
 * @property longitude Coordenada de longitude da vaga
 */
data class ParkingSpot(
    val id: Long?,
    val parkingId: Long,
    val latitude: Double,
    val longitude: Double,
)
```

## ParkingEvent

```kotlin
/**
 * Representa um evento relacionado ao estacionamento de um veículo.
 * 
 * Esta classe registra eventos como entrada, estacionamento e saída de veículos,
 * incluindo informações sobre localização, horários e valores pagos.
 *
 * @property id Identificador único do evento, pode ser nulo para novas instâncias
 * @property licensePlate Placa do veículo associado ao evento
 * @property latitude Coordenada de latitude onde o evento ocorreu
 * @property longitude Coordenada de longitude onde o evento ocorreu
 * @property entryTime Horário em que o veículo entrou no estacionamento
 * @property exitTime Horário em que o veículo saiu do estacionamento, nulo para eventos que não são de saída
 * @property eventType Tipo do evento (ENTRY, PARKED, EXIT)
 * @property priceMultiplier Multiplicador de preço aplicado ao evento, baseado na ocupação do estacionamento
 * @property amountPaid Valor pago pelo estacionamento, aplicável apenas para eventos de saída
 */
data class ParkingEvent(
    val id: Long? = null,
    val licensePlate: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val entryTime: LocalDateTime,
    var exitTime: LocalDateTime? = null,
    var eventType: EventType,
    val priceMultiplier: Double = 1.0,
    val amountPaid: BigDecimal = BigDecimal.ZERO,
)
```

## Revenue

```kotlin
/**
 * Representa a receita gerada por um estacionamento em uma data específica.
 * 
 * Esta classe armazena informações sobre o valor acumulado de receita
 * para um determinado estacionamento em uma data específica.
 *
 * @property id Identificador único do registro de receita, pode ser nulo para novas instâncias
 * @property parkingId Identificador do estacionamento ao qual esta receita está associada
 * @property date Data à qual esta receita se refere
 * @property amount Valor total da receita
 * @property currency Tipo de moeda da receita (ex: BRL, USD)
 */
data class Revenue(
    val id: Long? = null,
    val parkingId: Long,
    val date: LocalDate,
    val amount: BigDecimal,
    val currency: CurrencyType,
)
```

## WebhookEvent

```kotlin
/**
 * Representa um evento recebido via webhook de sistemas externos.
 * 
 * Esta classe contém os dados brutos recebidos de sistemas externos,
 * como simuladores de estacionamento, para processamento pelo sistema.
 *
 * @property licensePlate Placa do veículo associado ao evento
 * @property eventType Tipo do evento (ENTRY, PARKED, EXIT)
 * @property timestamp Timestamp do evento, usado principalmente para eventos de entrada
 * @property entryTime Horário de entrada, usado para eventos de entrada
 * @property exitTime Horário de saída, usado para eventos de saída
 * @property lat Coordenada de latitude onde o evento ocorreu
 * @property lng Coordenada de longitude onde o evento ocorreu
 */
data class WebhookEvent(
    val licensePlate: String,
    val eventType: EventType,
    val timestamp: LocalDateTime? = null,
    val entryTime: LocalDateTime? = null,
    val exitTime: LocalDateTime? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)
```

## PlateStatus

```kotlin
/**
 * Representa o status atual de um veículo no estacionamento.
 * 
 * Esta classe contém informações sobre a localização, tempo de permanência
 * e valor acumulado até o momento para um veículo estacionado.
 *
 * @property licensePlate Placa do veículo
 * @property priceUntilNow Valor acumulado até o momento
 * @property entryTime Horário de entrada do veículo
 * @property timeParked Horário em que o veículo foi estacionado
 * @property latitude Coordenada de latitude onde o veículo está estacionado
 * @property longitude Coordenada de longitude onde o veículo está estacionado
 */
data class PlateStatus(
    val licensePlate: String,
    val priceUntilNow: BigDecimal,
    val entryTime: LocalDateTime,
    val timeParked: LocalDateTime,
    val latitude: Double,
    val longitude: Double,
)
```

## ParkingSpotStatus

```kotlin
/**
 * Representa o status atual de uma vaga de estacionamento.
 * 
 * Esta classe contém informações sobre a ocupação da vaga,
 * incluindo detalhes do veículo estacionado, se houver.
 *
 * @property occupied Indica se a vaga está ocupada
 * @property licensePlate Placa do veículo que ocupa a vaga, se houver
 * @property priceUntilNow Valor acumulado até o momento para o veículo na vaga
 * @property entryTime Horário de entrada do veículo que ocupa a vaga
 * @property timeParked Horário em que o veículo foi estacionado na vaga
 */
data class ParkingSpotStatus(
    val occupied: Boolean,
    val licensePlate: String = "",
    val priceUntilNow: BigDecimal = BigDecimal.ZERO,
    val entryTime: LocalDateTime? = null,
    val timeParked: LocalDateTime? = null,
)
```
