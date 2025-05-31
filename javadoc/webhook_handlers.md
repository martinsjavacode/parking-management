# Documentação KDoc para Handlers de Webhook

## ParkingWebhookHandler (Interface)

```kotlin
/**
 * Interface para processamento de eventos de webhook relacionados a estacionamento.
 * 
 * Define o contrato para processamento de eventos recebidos via webhook
 * de sistemas externos, como simuladores de estacionamento.
 */
interface ParkingWebhookHandler {
    /**
     * Processa um evento de webhook.
     *
     * @param event O evento de webhook a ser processado
     */
    suspend fun execute(event: WebhookEvent)
}
```

## ParkingWebhookHandlerImpl

```kotlin
/**
 * Implementação do handler de eventos de webhook para estacionamento.
 * 
 * Esta classe direciona os eventos recebidos para os handlers específicos
 * com base no tipo de evento (ENTRY, PARKED, EXIT).
 *
 * @property entryWebhookHandler Handler para eventos de entrada
 * @property parkedWebhookHandler Handler para eventos de estacionamento
 * @property exitWebhookHandler Handler para eventos de saída
 */
@Service
class ParkingWebhookHandlerImpl(
    private val entryWebhookHandler: EntryWebhookHandler,
    private val parkedWebhookHandler: ParkedWebhookHandler,
    private val exitWebhookHandler: ExitWebhookHandler,
) : ParkingWebhookHandler {
    /**
     * Executa o processamento do evento de webhook, direcionando para o handler apropriado.
     *
     * @param event O evento de webhook a ser processado
     */
    override suspend fun execute(event: WebhookEvent) {
        when (event.eventType) {
            EventType.ENTRY -> entryWebhookHandler.handle(event)
            EventType.PARKED -> parkedWebhookHandler.handle(event)
            EventType.EXIT -> exitWebhookHandler.handle(event)
        }
    }
}
```

## EntryWebhookHandler

```kotlin
/**
 * Handler para processamento de eventos de entrada de veículos no estacionamento.
 * 
 * Esta classe é responsável por validar e processar eventos do tipo ENTRY,
 * verificando se o estacionamento está aberto e se não há conflitos de placa.
 *
 * @property messageSource Fonte de mensagens para internacionalização
 * @property traceContext Contexto de rastreamento para logs
 * @property parkingEventRepository Repositório para eventos de estacionamento
 * @property parkingRepository Repositório para configurações de estacionamento
 */
@Component
class EntryWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val parkingRepository: ParkingRepositoryPort,
) {
    /**
     * Processa um evento de entrada de veículo.
     * 
     * Valida os dados do evento, verifica se há estacionamentos abertos
     * e registra o evento de entrada se todas as condições forem atendidas.
     *
     * @param event O evento de webhook a ser processado
     * @throws IllegalArgumentException Se o tipo de evento for inválido
     * @throws IllegalStateException Se o horário de entrada for nulo
     * @throws LicensePlateConflictException Se já existir um evento ativo para a placa
     * @throws NoParkingOpenException Se não houver estacionamentos abertos
     */
    @Transactional
    suspend fun handle(event: WebhookEvent) {
        // Implementação omitida para brevidade
    }
}
```

## ParkedWebhookHandler

```kotlin
/**
 * Handler para processamento de eventos de estacionamento de veículos.
 * 
 * Esta classe é responsável por validar e processar eventos do tipo PARKED,
 * aplicando o multiplicador de preço dinâmico com base na ocupação do setor.
 *
 * @property messageSource Fonte de mensagens para internacionalização
 * @property traceContext Contexto de rastreamento para logs
 * @property parkingEventRepository Repositório para eventos de estacionamento
 * @property getParkingByCoordinatesOrThrowHandler Handler para obter estacionamento por coordenadas
 * @property calculatePricingMultiplierHandler Handler para calcular multiplicador de preço
 * @property initializeDailyRevenueHandler Handler para inicializar receita diária
 */
@Component
class ParkedWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val getParkingByCoordinatesOrThrowHandler: GetParkingByCoordinatesOrThrowHandler,
    private val calculatePricingMultiplierHandler: CalculatePricingMultiplierHandler,
    private val initializeDailyRevenueHandler: UpdateOrInitializeDailyRevenueHandler,
) {
    /**
     * Processa um evento de estacionamento de veículo.
     * 
     * Valida os dados do evento, busca o evento de entrada correspondente,
     * calcula o multiplicador de preço dinâmico e registra o evento de estacionamento.
     * Também inicializa o registro de receita diária se necessário.
     *
     * @param event O evento de webhook a ser processado
     * @throws IllegalArgumentException Se o tipo de evento for inválido
     * @throws EntryEventNotFoundException Se não for encontrado um evento de entrada para a placa
     */
    @Transactional
    suspend fun handle(event: WebhookEvent) {
        // Implementação omitida para brevidade
    }
}
```

## ExitWebhookHandler

```kotlin
/**
 * Handler para processamento de eventos de saída de veículos do estacionamento.
 * 
 * Esta classe é responsável por validar e processar eventos do tipo EXIT,
 * calculando o valor a ser cobrado e atualizando a receita diária.
 *
 * @property messageSource Fonte de mensagens para internacionalização
 * @property traceContext Contexto de rastreamento para logs
 * @property parkingEventRepository Repositório para eventos de estacionamento
 * @property getParkingByCoordinatesOrThrowHandler Handler para obter estacionamento por coordenadas
 * @property updateDailyRevenueHandler Handler para atualizar receita diária
 */
@Component
class ExitWebhookHandler(
    private val messageSource: MessageSource,
    private val traceContext: TraceContext,
    private val parkingEventRepository: ParkingEventRepositoryPort,
    private val getParkingByCoordinatesOrThrowHandler: GetParkingByCoordinatesOrThrowHandler,
    private val updateDailyRevenueHandler: UpdateOrInitializeDailyRevenueHandler,
) {
    /**
     * Processa um evento de saída de veículo.
     * 
     * Valida os dados do evento, busca o evento de estacionamento correspondente,
     * calcula o valor a ser cobrado e atualiza a receita diária.
     *
     * @param event O evento de webhook a ser processado
     * @throws IllegalArgumentException Se o tipo de evento for inválido
     * @throws NoParkedEventFoundException Se não for encontrado um evento de estacionamento para a placa
     */
    @Transactional
    suspend fun handle(event: WebhookEvent) {
        // Implementação omitida para brevidade
    }
}
```
