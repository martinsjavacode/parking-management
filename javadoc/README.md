# Documentação KDoc do Sistema de Gestão de Estacionamento

Esta documentação contém a documentação KDoc para as principais classes e componentes do sistema de gestão de estacionamento.

## Estrutura da Documentação

1. [Classes de Modelo do Domínio](domain_model_classes.md)
   - Parking
   - ParkingSpot
   - ParkingEvent
   - Revenue
   - WebhookEvent
   - PlateStatus
   - ParkingSpotStatus

2. [Regras Operacionais](operational_rules.md)
   - OperationalRules
   - DateTimeRules

3. [Handlers de Webhook](webhook_handlers.md)
   - ParkingWebhookHandler (Interface)
   - ParkingWebhookHandlerImpl
   - EntryWebhookHandler
   - ParkedWebhookHandler
   - ExitWebhookHandler

4. [Casos de Uso](use_cases.md)
   - CalculatePricingMultiplierHandler
   - GetParkingByCoordinatesOrThrowHandler
   - GetParkingSpotStatusHandler
   - GetPlateStatusHandler
   - UpdateOrInitializeDailyRevenueHandler
   - GetDailyBillingByParkingSectorHandler

5. [Controllers REST](controllers.md)
   - VehicleRestController
   - SpotRestController
   - RevenueRestController
   - WebhookEvent (Controller)

6. [Enums](enums.md)
   - EventType
   - CurrencyType
   - OccupancyActionType
   - ExceptionType
   - InternalCodeType

7. [Exceções](exceptions.md)
   - BusinessException
   - EntryEventNotFoundException
   - LicensePlateConflictException
   - LicensePlateNotFoundException
   - NoParkedEventFoundException
   - NoParkingOpenException
   - ParkingNotFoundException
   - ParkingSpotNotFoundException
   - RevenueNotFoundException

## Visão Geral do Sistema

O Sistema de Gestão de Estacionamento é uma aplicação desenvolvida em Kotlin com Spring Boot que gerencia estacionamentos com precificação dinâmica e controle de capacidade por setor. O sistema recebe eventos via webhook de um simulador externo para acompanhar a entrada, estacionamento e saída de veículos, registrando dados de receita de forma adequada.

### Principais Funcionalidades

1. **Gestão de eventos de estacionamento**:
   - ENTRY: Quando um veículo entra no estacionamento
   - PARKED: Quando um veículo estaciona em uma vaga específica
   - EXIT: Quando um veículo deixa o estacionamento

2. **Precificação dinâmica**:
   - Ocupação < 25%: desconto de 10% (multiplicador 0.9)
   - Ocupação entre 25% e 50%: preço base (multiplicador 1.0)
   - Ocupação entre 50% e 75%: acréscimo de 10% (multiplicador 1.1)
   - Ocupação > 75%: acréscimo de 25% (multiplicador 1.25)

3. **Controle de lotação**:
   - Monitoramento da ocupação de cada setor
   - Aplicação das regras de precificação dinâmica com base na ocupação

4. **Endpoints REST**:
   - Consulta de status de veículos por placa
   - Consulta de status de vagas por coordenadas
   - Consulta de receita por setor e data
   - Recebimento de eventos via webhook

5. **Persistência de dados**:
   - Estacionamentos e seus setores
   - Vagas de estacionamento
   - Eventos de estacionamento
   - Receitas diárias por setor
