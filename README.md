# Documentação Técnica do Sistema de Gestão de Estacionamento

## 1. Visão Geral

Este sistema gerencia estacionamentos com precificação dinâmica e gerenciamento de capacidade por setor. Ele recebe eventos via webhook de um simulador externo para acompanhar a entrada, estacionamento e saída de veículos, registrando dados de receita de forma adequada.

## 2. Objetivos

- Implementar um serviço backend escalável e de fácil manutenção usando Kotlin, Spring Boot e PostgreSQL.
- Suportar precificação dinâmica baseada na ocupação de cada setor.
- Manter o acompanhamento preciso da receita por setor e dia.
- Fornecer APIs REST para status do veículo, status da vaga e relatórios de receita.
- Oferecer documentação completa usando OpenAPI/Swagger.

## 3. Escopo

- Tratar eventos de estacionamento: ENTRY (entrada), PARKED (estacionado), EXIT (saída).
- Gerenciar múltiplos setores de estacionamento com capacidade individual.
- Integrar-se com simulador externo via webhook para ingestão de eventos.
- Armazenar dados de estacionamento e receita em banco PostgreSQL.
- Disponibilizar endpoints REST para consumo por clientes.
- Permitir implantação containerizada com Docker Compose.

## 4. Premissas e Restrições

- O simulador envia webhooks confiáveis nos formatos JSON definidos.
- Os setores possuem horários fixos de funcionamento configurados no banco.
- Os multiplicadores para precificação dinâmica são fixos e pré-definidos.
- O sistema será executado em ambiente dockerizado para fácil implantação.
- A receita será atualizada apenas nos eventos de saída (EXIT).
- A implementação atual suporta concorrência básica, com planos para melhorias.

----

## Estrutura Geral do Projeto

- **Nome do projeto:** parking-management
- **Descrição resumida:** Sistema de Gestão de Estacionamento
- **Tecnologias usadas:** Kotlin 2.0.21 com Coroutines, Spring Boot 3.5.0, PostgreSQL

----

## Fluxo do Sistema

### Confirmação das regras de negócio implementadas:

#### Precificação Dinâmica

- Implementada e funcionando.

#### Controle de Lotação

- Preparado para cenários complexos.
- Aplica as regras de precificação dinâmica com base na ocupação do setor.

----

### Tratamento dos Webhooks:

#### **ENTRY (Entrada):**

1. Verifica se há alguma garagem em funcionamento no momento.
2. Caso positivo, registra o evento ENTRY com multiplicador padrão 1.0.

**Exemplo JSON recebido:**

```json
{
  "license_plate": "ZUL0001",
  "event_type": "ENTRY",
  "timestamp": "2023-10-01T10:00:00Z"
}
```
----
### **PARKED (Estacionado):**
1. Verifica se o veículo com a license_plate está registrado como ENTRY.
2. Caso sim, registra o evento PARKED aplicando o multiplicador definido pela regra de preço dinâmico:
   - Lotação < 25%: desconto de 10% (multiplicador 0.9).
   - Lotação ≤ 50%: preço base (multiplicador 1.0).
   - Lotação ≤ 75%: acréscimo de 10% (multiplicador 1.1).
   - Lotação ≤ 100%: acréscimo de 25% (multiplicador 1.25).
   - Lotação > 100%: não permite estacionar.
3. Caso não exista registro de receita para o dia, cria um registro inicial com valor zero.

**Exemplo JSON recebido:**
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```
----
### **EXIT (Saída):**
1. Verifica se o veículo com a license_plate está registrado como PARKED.
2. Caso sim, calcula o valor a ser cobrado com base no multiplicador definido no evento PARKED e no tempo estacionado.
3. Registra o evento de saída e atualiza a receita do dia para o setor correspondente.

**Exemplo JSON recebido:**
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2023-10-01T12:00:00.000Z",
  "event_type": "EXIT"
}
```
----
## Endpoints REST Implementados
### Controllers
Os endpoints REST estão organizados em três controllers principais, cada um responsável por um domínio específico:
1. `VehicleRestController`: Gerencia operações relacionadas a veículos, como status de entrada e permanência.
2. `SpotRestController`: Gerencia informações das vagas de estacionamento, como disponibilidade e ocupação.
3. `RevenueRestController`: Gerencia informações sobre receitas, como cálculo e consulta de valores por setor e data.
----
### **POST /plate-status**
**Request:**
```json
{
  "license_plate": "ZUL0001"
}
```
**Response**
```json
{
  "license_plate": "ZUL0001",
  "price_until_now": 0.00,
  "entry_time": "2025-01-01T12:00:00.000Z",
  "time_parked": "2025-01-01T12:00:00.000Z",
  "lat": -23.561684,
  "lng": -46.655981
}
```
----
### **POST /spots/status**
**Request:**
```json
{
  "lat": -23.561684,
  "lng": -46.655981
}
```
**Response**
```json
{
  "ocupied": false,
  "license_plate": "",
  "price_until_now": 0.00,
  "entry_time": "2025-01-01T12:00:00.000Z",
  "time_parked": "2025-01-01T12:00:00.000Z"
}
```
----
### **GET /revenues**
**Request:**
```json
{
  "date": "2025-01-01",
  "sector": "A"
}
```
**Response**
```json
{
  "amount": 0.00,
  "currency": "BRL",
  "timestamp": "2025-01-01T12:00:00.000Z"
}
```
----
## Estrutura das Tabelas no Banco de Dados

### Tabela: Parking
```postgresql
CREATE TABLE IF NOT EXISTS parking
(
    id                     BIGINT GENERATED BY DEFAULT AS IDENTITY,
    sector_name            VARCHAR(50)    NOT NULL,
    base_price             NUMERIC(10, 2) NOT NULL,
    max_capacity           INT            NOT NULL,
    open_hour              TIME           NOT NULL,
    close_hour             TIME           NOT NULL,
    duration_limit_minutes INT            NOT NULL,
    CONSTRAINT PK_PARKING PRIMARY KEY (id)
);

CREATE INDEX IX_PARKING_SECTOR_NAME ON parking (sector_name);
```

### Tabela: ParkingSpot
```postgresql
CREATE TABLE IF NOT EXISTS parking_spots
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY,
    parking_id BIGINT        NOT NULL REFERENCES parking (id),
    latitude   NUMERIC(9, 6) NOT NULL,
    longitude  NUMERIC(9, 6) NOT NULL,
    CONSTRAINT PK_PARKING_SPOT PRIMARY KEY (id)
);

CREATE INDEX IDX_PARKING_SPOT_PARKING_ID ON parking_spots (parking_id);
```

### Tabela: ParkingEvent
```postgresql
CREATE TABLE IF NOT EXISTS parking_events
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY,
    license_plate   VARCHAR(20) NOT NULL,
    latitude        NUMERIC(9, 6),
    longitude       NUMERIC(9, 6),
    entry_time      TIMESTAMP   NOT NULL,
    exit_time       TIMESTAMP,
    event_type      VARCHAR(10) NOT NULL,
    price_multiplier NUMERIC(3, 2),
    amount_paid     NUMERIC(10, 2),
    CONSTRAINT PK_PARKING_EVENT PRIMARY KEY (id),
    CONSTRAINT CK_PARKING_EVENT_TYPE CHECK (event_type IN ('ENTRY', 'PARKED', 'EXIT'))
);

CREATE INDEX IDX_PARKING_EVENT_LICENSE_PLATE ON parking_events (license_plate);
CREATE INDEX IDX_PARKING_EVENT_EVENT_TYPE ON parking_events (event_type);
```

### Tabela: Revenue
```postgresql
CREATE TABLE IF NOT EXISTS revenues
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY,
    parking_id BIGINT         NOT NULL REFERENCES parking (id),
    date       DATE           NOT NULL,
    amount     NUMERIC(10, 2) NOT NULL,
    currency   VARCHAR(10)    NOT NULL,
    CONSTRAINT PK_REVENUE PRIMARY KEY (id)
);

CREATE INDEX IDX_REVENUE_ON_PARKING_DATE ON revenues (parking_id, date);
```
----
## Passo a Passo para Configuração e Execução

### Como rodar o projeto localmente:
1. Clone o repositório.
2. Configure a variável de ambiente:
   - **SERVER_PORT=3003**
3. Execute o banco de dados PostgreSQL localmente:
   - O Docker Compose do projeto sobe automaticamente o container PostgreSQL ao iniciar a aplicação.
4. Compile e inicie a aplicação.
    ```shell
    ./gradlew clean build bootRun --refresh-dependencies
    ```
5. Execute o simulador com o comando:
    ```shell
   docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
   ```
----
## Documentação OpenAPI

O projeto utiliza o plugin org.springdoc.openapi-gradle-plugin para gerar a documentação OpenAPI.

O Swagger UI fica disponível em:
```
http://localhost:{SERVER_PORT}/swagger-ui.html
```

### **Exemplo de anotações OpenAPI para o** `SpotRestController`:
```kotlin
@Operation(
    summary = "Check the status of a parking spot",
    tags = ["Parking Spot"],
    requestBody =
        io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Coordinates of the parking spot to be queried",
            required = true,
            content = [
                io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = io.swagger.v3.oas.annotations.media.Schema(implementation = SpotStatusRequest::class),
                ),
            ],
        ),
    responses = [
        io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Parking spot status",
            content = [
                io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = io.swagger.v3.oas.annotations.media.Schema(implementation = SpotStatusResponse::class),
                ),
            ],
        ),
        io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid event data",
            content = [
                io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = io.swagger.v3.oas.annotations.media.Schema(),
                ),
            ],
        ),
        io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = [
                io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = io.swagger.v3.oas.annotations.media.Schema(),
                ),
            ],
        ),
    ],
)
```
