# Documentação KDoc para Controllers REST

## VehicleRestController

```kotlin
/**
 * Controller REST para operações relacionadas a veículos.
 * 
 * Fornece endpoints para consultar informações sobre veículos
 * estacionados no sistema.
 *
 * @property getPlateStatusHandler Handler para obter status de veículos por placa
 */
@RestController
class VehicleRestController(
    private val getPlateStatusHandler: GetPlateStatusHandler,
) {
    /**
     * Endpoint para consultar o status de um veículo por placa.
     * 
     * @param request Requisição contendo a placa do veículo
     * @return Resposta com o status do veículo
     * @throws LicensePlateNotFoundException Se nenhum veículo com a placa fornecida for encontrado
     */
    @Operation(
        summary = "Get vehicle status by license plate",
        tags = ["Vehicle"]
        // Anotações OpenAPI omitidas para brevidade
    )
    @PostMapping("/plate-status")
    suspend fun retrievePlateStatus(
        @RequestBody request: LicensePlateRequest,
    ): ResponseEntity<PlateStatusResponse> {
        val plateStatus = getPlateStatusHandler.handle(request.licensePlate)
        return ResponseEntity.ok(plateStatus.toResponse())
    }
}
```

## SpotRestController

```kotlin
/**
 * Controller REST para operações relacionadas a vagas de estacionamento.
 * 
 * Fornece endpoints para consultar informações sobre vagas
 * específicas no sistema.
 *
 * @property getParkingSpotStatusHandler Handler para obter status de vagas
 */
@RestController
class SpotRestController(
    private val getParkingSpotStatusHandler: GetParkingSpotStatusHandler,
) {
    /**
     * Endpoint para consultar o status de uma vaga por coordenadas.
     * 
     * @param request Requisição contendo as coordenadas da vaga
     * @return Resposta com o status da vaga
     * @throws ParkingSpotNotFoundException Se nenhuma vaga for encontrada nas coordenadas
     */
    @Operation(
        summary = "Check the status of a parking spot",
        tags = ["Parking Spot"]
        // Anotações OpenAPI omitidas para brevidade
    )
    @PostMapping("/spot-status")
    suspend fun changeSpotStatus(
        @RequestBody request: SpotStatusRequest,
    ): ResponseEntity<SpotStatusResponse> {
        val spotStatus = getParkingSpotStatusHandler.handle(request.lat, request.lng)
        return ResponseEntity.ok(spotStatus.toResponse())
    }
}
```

## RevenueRestController

```kotlin
/**
 * Controller REST para operações relacionadas a receitas.
 * 
 * Fornece endpoints para consultar informações sobre receitas
 * geradas por setor e data.
 *
 * @property getDailyBillingByParkingSectorHandler Handler para obter receita diária por setor
 */
@RestController
@RequestMapping("/revenue")
class RevenueRestController(
    private val getDailyBillingByParkingSectorHandler: GetDailyBillingByParkingSectorHandler,
) {
    /**
     * Endpoint para consultar a receita acumulada por setor e data.
     * 
     * @param request Requisição contendo o setor e a data para consulta
     * @return Resposta com a receita acumulada
     * @throws RevenueNotFoundException Se nenhuma receita for encontrada para o setor e data
     */
    @Operation(
        summary = "Get accumulated revenue by sector and date",
        tags = ["Revenue"]
        // Anotações OpenAPI omitidas para brevidade
    )
    @GetMapping
    suspend fun billingConsultation(
        @RequestBody request: DailyBillingRequest,
    ): ResponseEntity<DailyBillingResponse> {
        val revenue = getDailyBillingByParkingSectorHandler.handle(request.date, request.sector)
        val dailyBillingResponse =
            DailyBillingResponse(
                amount = revenue.amount,
                currency = revenue.currency,
                timestamp = revenue.date.atTime(LocalTime.now()),
            )

        return ResponseEntity.ok(dailyBillingResponse)
    }
}
```

## WebhookEvent (Controller)

```kotlin
/**
 * Controller REST para receber eventos de webhook.
 * 
 * Fornece um endpoint para receber eventos de sistemas externos,
 * como simuladores de estacionamento.
 *
 * @property webHookHandlerParking Handler para processar eventos de webhook
 */
@RestController
@RequestMapping("/webhook")
class WebhookEvent(
    private val webHookHandlerParking: ParkingWebhookHandler,
) {
    /**
     * Endpoint para receber e processar eventos de webhook.
     * 
     * @param event O evento de webhook a ser processado
     */
    @Operation(
        summary = "Handle parking events",
        tags = ["Webhooks"]
        // Anotações OpenAPI omitidas para brevidade
    )
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    suspend fun handleWebhook(
        @RequestBody event: WebhookEvent,
    ) {
        webHookHandlerParking.execute(event)
    }
}
```
