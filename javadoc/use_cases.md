# Documentação KDoc para Casos de Uso

## CalculatePricingMultiplierHandler (Interface)

```kotlin
/**
 * Interface para cálculo do multiplicador de preço dinâmico.
 * 
 * Define o contrato para calcular o multiplicador de preço com base
 * na ocupação do estacionamento em determinadas coordenadas.
 */
interface CalculatePricingMultiplierHandler {
    /**
     * Calcula o multiplicador de preço para as coordenadas fornecidas.
     *
     * @param latitude Coordenada de latitude
     * @param longitude Coordenada de longitude
     * @return O multiplicador de preço calculado
     */
    suspend fun handle(latitude: Double, longitude: Double): Double
}
```

## CalculatePricingMultiplierHandlerImpl

```kotlin
/**
 * Implementação do handler para cálculo do multiplicador de preço dinâmico.
 * 
 * Esta classe calcula o multiplicador de preço com base na taxa de ocupação
 * do estacionamento nas coordenadas fornecidas.
 *
 * @property parkingCustomQueryRepository Repositório para consultas personalizadas de estacionamento
 */
@Service
class CalculatePricingMultiplierHandlerImpl(
    private val parkingCustomQueryRepository: ParkingCustomQueryRepositoryPort,
) : CalculatePricingMultiplierHandler {
    /**
     * Calcula o multiplicador de preço para as coordenadas fornecidas.
     * 
     * Primeiro valida as coordenadas, depois calcula a taxa de ocupação
     * e finalmente determina o multiplicador de preço com base nessa taxa.
     *
     * @param latitude Coordenada de latitude
     * @param longitude Coordenada de longitude
     * @return O multiplicador de preço calculado
     */
    override suspend fun handle(
        latitude: Double,
        longitude: Double,
    ): Double {
        OperationalRules.assertValidCoordinates(latitude, longitude)

        val occupancyRate = calculateOccupancyRate(latitude, longitude)
        return OperationalRules.priceMultiplierForOccupancyRate(occupancyRate)
    }

    /**
     * Calcula a taxa de ocupação do estacionamento nas coordenadas fornecidas.
     *
     * @param latitude Coordenada de latitude
     * @param longitude Coordenada de longitude
     * @return A taxa de ocupação em porcentagem (0-100)
     */
    private suspend fun calculateOccupancyRate(
        latitude: Double,
        longitude: Double,
    ): Int {
        val (maxCapacity, spotOccupancy) =
            parkingCustomQueryRepository.findParkingCapacityAndOccupancy(
                latitude,
                longitude,
            )

        return spotOccupancy.percentOf(maxCapacity)
    }
}
```

## GetParkingByCoordinatesOrThrowHandler (Interface)

```kotlin
/**
 * Interface para obter informações de estacionamento por coordenadas.
 * 
 * Define o contrato para buscar um estacionamento com base em coordenadas geográficas,
 * lançando uma exceção se não for encontrado.
 */
interface GetParkingByCoordinatesOrThrowHandler {
    /**
     * Busca um estacionamento com base nas coordenadas fornecidas.
     *
     * @param latitude Coordenada de latitude
     * @param longitude Coordenada de longitude
     * @return O estacionamento encontrado
     * @throws ParkingNotFoundException Se nenhum estacionamento for encontrado nas coordenadas
     */
    suspend fun handle(latitude: Double, longitude: Double): Parking
}
```

## GetParkingSpotStatusHandler (Interface)

```kotlin
/**
 * Interface para obter o status de uma vaga de estacionamento.
 * 
 * Define o contrato para verificar o status de uma vaga de estacionamento
 * com base em suas coordenadas geográficas.
 */
interface GetParkingSpotStatusHandler {
    /**
     * Obtém o status de uma vaga de estacionamento nas coordenadas fornecidas.
     *
     * @param latitude Coordenada de latitude
     * @param longitude Coordenada de longitude
     * @return O status da vaga de estacionamento
     */
    suspend fun handle(latitude: Double, longitude: Double): ParkingSpotStatus
}
```

## GetPlateStatusHandler (Interface)

```kotlin
/**
 * Interface para obter o status de um veículo por placa.
 * 
 * Define o contrato para verificar o status atual de um veículo
 * no estacionamento com base em sua placa.
 */
interface GetPlateStatusHandler {
    /**
     * Obtém o status de um veículo com base na placa fornecida.
     *
     * @param licensePlate Placa do veículo
     * @return O status do veículo
     * @throws LicensePlateNotFoundException Se nenhum veículo com a placa fornecida for encontrado
     */
    suspend fun handle(licensePlate: String): PlateStatus
}
```

## UpdateOrInitializeDailyRevenueHandler (Interface)

```kotlin
/**
 * Interface para atualizar ou inicializar a receita diária.
 * 
 * Define o contrato para atualizar a receita diária de um estacionamento
 * ou inicializá-la se ainda não existir.
 */
interface UpdateOrInitializeDailyRevenueHandler {
    /**
     * Atualiza ou inicializa a receita diária para o estacionamento nas coordenadas fornecidas.
     *
     * @param eventType Tipo do evento que está gerando a atualização
     * @param latitude Coordenada de latitude
     * @param longitude Coordenada de longitude
     * @param amountPaid Valor pago, usado apenas para eventos de saída
     * @return A receita atualizada
     */
    suspend fun handle(
        eventType: EventType,
        latitude: Double,
        longitude: Double,
        amountPaid: BigDecimal = BigDecimal.ZERO,
    ): Revenue
}
```

## GetDailyBillingByParkingSectorHandler (Interface)

```kotlin
/**
 * Interface para obter a receita diária por setor de estacionamento.
 * 
 * Define o contrato para consultar a receita acumulada em um determinado
 * dia para um setor específico de estacionamento.
 */
interface GetDailyBillingByParkingSectorHandler {
    /**
     * Obtém a receita diária para o setor e data fornecidos.
     *
     * @param date Data para consulta da receita
     * @param sector Nome do setor de estacionamento
     * @return A receita encontrada
     * @throws RevenueNotFoundException Se nenhuma receita for encontrada para o setor e data
     */
    suspend fun handle(date: LocalDate, sector: String): Revenue
}
```
