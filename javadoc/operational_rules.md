# Documentação KDoc para Regras Operacionais

## OperationalRules

```kotlin
/**
 * Objeto que contém as regras operacionais do sistema de estacionamento.
 * 
 * Este objeto singleton implementa as regras de negócio relacionadas à precificação dinâmica,
 * validação de coordenadas e cálculo de tarifas de estacionamento.
 */
object OperationalRules {
    /**
     * Determina o multiplicador de preço com base na taxa de ocupação do estacionamento.
     * 
     * Implementa a regra de precificação dinâmica:
     * - Ocupação < 25%: desconto de 10% (multiplicador 0.9)
     * - Ocupação entre 25% e 50%: preço base (multiplicador 1.0)
     * - Ocupação entre 50% e 75%: acréscimo de 10% (multiplicador 1.1)
     * - Ocupação > 75%: acréscimo de 25% (multiplicador 1.25)
     *
     * @param occupancyRate Taxa de ocupação do estacionamento em porcentagem (0-100)
     * @return Multiplicador de preço a ser aplicado
     */
    fun priceMultiplierForOccupancyRate(occupancyRate: Int): Double =
        when {
            occupancyRate < 25 -> 0.9
            occupancyRate < 50 -> 1.0
            occupancyRate < 75 -> 1.1
            else -> 1.25
        }

    /**
     * Valida se as coordenadas geográficas fornecidas são válidas.
     * 
     * Verifica se as coordenadas não são nulas e estão dentro dos limites válidos:
     * - Latitude: entre -90.0 e 90.0
     * - Longitude: entre -180.0 e 180.0
     *
     * @param latitude Coordenada de latitude a ser validada
     * @param longitude Coordenada de longitude a ser validada
     * @throws IllegalStateException Se as coordenadas forem inválidas
     */
    fun assertValidCoordinates(
        latitude: Double?,
        longitude: Double?,
    ) {
        val notNull = latitude != null && longitude != null
        check(notNull && (latitude in -90.0..90.0) && (longitude in -180.0..180.0)) {
            "Invalid latitude or longitude"
        }
    }

    /**
     * Calcula o valor a ser cobrado pelo estacionamento.
     * 
     * O cálculo é baseado no tempo de permanência, preço base, limite de duração
     * e multiplicador de preço dinâmico.
     *
     * @param entryTime Horário de entrada do veículo
     * @param exitTime Horário de saída do veículo
     * @param basePrice Preço base do estacionamento
     * @param durationLimitMinutes Duração limite em minutos para o período base de cobrança
     * @param priceMultiplier Multiplicador de preço baseado na ocupação
     * @return Valor a ser cobrado pelo estacionamento
     */
    fun calculateParkingFee(
        entryTime: LocalDateTime,
        exitTime: LocalDateTime,
        basePrice: BigDecimal,
        durationLimitMinutes: Int,
        priceMultiplier: Double,
    ): BigDecimal {
        // Calculate the duration in minutes
        val durationMinutes = Duration.between(entryTime, exitTime).toMinutes()

        // Convert the duration into proportional "periods" based on the duration limit
        val period =
            BigDecimal(durationMinutes).divide(
                // Convert duration limit to BigDecimal
                BigDecimal(durationLimitMinutes),
                // Division precision
                10,
                RoundingMode.HALF_UP,
            )

        // Calculate the base amount proportional to the period
        val amountBase = period.multiply(basePrice)

        // Multiply by the price multiplier and return the final amount with 2 decimal places
        return amountBase.multiply(BigDecimal.valueOf(priceMultiplier))
            .setScale(2, RoundingMode.HALF_UP)
    }
}
```

## DateTimeRules

```kotlin
/**
 * Objeto que contém regras relacionadas a datas e horários no sistema.
 * 
 * Este objeto singleton implementa funções utilitárias para manipulação
 * e validação de datas e horários no contexto do estacionamento.
 */
object DateTimeRules {
    /**
     * Verifica se um horário está dentro do período de funcionamento do estacionamento.
     *
     * @param time Horário a ser verificado
     * @param openHour Horário de abertura do estacionamento
     * @param closeHour Horário de fechamento do estacionamento
     * @return true se o horário estiver dentro do período de funcionamento, false caso contrário
     */
    fun isWithinOperatingHours(time: LocalTime, openHour: LocalTime, closeHour: LocalTime): Boolean {
        return time.isAfter(openHour) && time.isBefore(closeHour)
    }
    
    /**
     * Calcula a duração em minutos entre dois horários.
     *
     * @param start Horário de início
     * @param end Horário de fim
     * @return Duração em minutos
     */
    fun durationInMinutes(start: LocalDateTime, end: LocalDateTime): Long {
        return Duration.between(start, end).toMinutes()
    }
}
```
