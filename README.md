# Documentação Técnica do Sistema de Gestão de Estacionamento

## 1. Visão Geral

O Sistema de Gestão de Estacionamento é uma solução robusta e escalável para gerenciar estacionamentos com precificação dinâmica e controle de capacidade por setor. O sistema recebe eventos via webhook de um simulador externo para monitorar a entrada, estacionamento e saída de veículos, registrando dados de receita de forma precisa e confiável. Utiliza Redis para garantir consistência em operações concorrentes e PostgreSQL para persistência de dados.

## 2. Objetivos

- Implementar um serviço backend escalável e de fácil manutenção usando Kotlin, Spring Boot, Redis e PostgreSQL
- Suportar precificação dinâmica baseada na ocupação de cada setor
- Manter o acompanhamento preciso da receita por setor e dia
- Fornecer APIs REST para status do veículo, status da vaga e relatórios de receita
- Garantir consistência em operações concorrentes através de bloqueios distribuídos
- Oferecer documentação completa usando OpenAPI/Swagger
- Garantir alta cobertura de testes e qualidade de código

## 3. Escopo

- Tratar eventos de estacionamento: ENTRY (entrada), PARKED (estacionado), EXIT (saída)
- Gerenciar múltiplos setores de estacionamento com capacidade individual
- Integrar-se com simulador externo via webhook para ingestão de eventos
- Armazenar dados de estacionamento e receita em banco PostgreSQL
- Disponibilizar endpoints REST para consumo por clientes
- Permitir implantação containerizada com Docker Compose
- Implementar testes unitários, de integração e de carga

## 4. Premissas e Restrições

- O simulador envia webhooks confiáveis nos formatos JSON definidos
- Os setores possuem horários fixos de funcionamento configurados no banco
- Os multiplicadores para precificação dinâmica são fixos e pré-definidos
- O sistema será executado em ambiente dockerizado para fácil implantação
- A receita será atualizada apenas nos eventos de saída (EXIT)
- A implementação atual suporta concorrência básica, com planos para melhorias

## 5. Arquitetura

### 5.1 Visão Geral da Arquitetura

O projeto segue uma arquitetura hexagonal (também conhecida como ports and adapters), com clara separação entre domínio, aplicação e infraestrutura. Esta abordagem permite:

- Isolamento do domínio de negócios das preocupações técnicas
- Facilidade para substituir componentes externos sem afetar a lógica de negócios
- Melhor testabilidade dos componentes individuais
- Evolução independente das diferentes camadas do sistema

### 5.2 Camadas da Arquitetura

#### 5.2.1 Camada de Domínio

Contém as regras de negócio e entidades centrais do sistema, independentes de frameworks e tecnologias externas:

- **Modelos de Domínio**: Representam conceitos de negócio como `Parking`, `ParkingSpot`, `ParkingEvent` e `Revenue`
- **Portas (Ports)**: Interfaces que definem como a aplicação interage com o mundo externo
- **Regras de Negócio**: Implementam lógicas como precificação dinâmica e cálculo de tarifas

#### 5.2.2 Camada de Aplicação

Orquestra o fluxo de dados entre o domínio e as interfaces externas:

- **Casos de Uso**: Implementam operações específicas do sistema, como processamento de eventos de estacionamento
- **Serviços de Aplicação**: Coordenam múltiplas operações de domínio para atender requisitos de negócio

#### 5.2.3 Camada de Adaptadores

Conecta o sistema ao mundo externo:

- **Adaptadores de Entrada**: Controllers REST e handlers de webhook que recebem solicitações externas
- **Adaptadores de Saída**: Implementações de repositórios para persistência de dados

### 5.3 Decisões Técnicas

#### 5.3.1 Linguagem e Framework

- **Kotlin**: Escolhido por sua concisão, segurança de tipos e suporte a programação funcional e orientada a objetos
- **Spring Boot**: Fornece infraestrutura robusta para desenvolvimento de aplicações web, injeção de dependências e configuração

#### 5.3.2 Persistência

- **PostgreSQL**: Banco de dados relacional escolhido por sua confiabilidade, recursos avançados e bom desempenho
- **Spring Data R2DBC**: Utilizado para acesso reativo ao banco de dados, permitindo operações não-bloqueantes

#### 5.3.3 Concorrência e Distribuição

- **Kotlin Coroutines**: Implementação de programação assíncrona para melhor utilização de recursos e escalabilidade
- **Suspending Functions**: Utilizadas para operações assíncronas sem callbacks complexos
- **Redis**: Utilizado para implementar mecanismos de bloqueio distribuído e controle de idempotência

#### 5.3.4 Tratamento de Erros

- **Exceções Personalizadas**: Hierarquia de exceções para diferentes tipos de erros do sistema
- **Manipulador Global de Exceções**: Converte exceções em respostas HTTP apropriadas

#### 5.3.5 Internacionalização

- **MessageSource**: Utilizado para mensagens de erro e notificações em múltiplos idiomas
- **Locale Context**: Mantém informações sobre o idioma preferido do usuário

### 5.4 Uso do Redis no Sistema

O Redis é utilizado como componente fundamental para garantir a consistência e confiabilidade do sistema em um ambiente distribuído:

#### 5.4.1 Mecanismo de Bloqueio Distribuído

- **Implementação**: Através da classe `RedisDistributedLockAdapter` que implementa a interface `DistributedLockPort`
- **Finalidade**: Prevenir condições de corrida quando múltiplos veículos tentam estacionar na mesma vaga simultaneamente
- **Funcionamento**: 
  - Quando um veículo tenta estacionar, o sistema adquire um bloqueio exclusivo para as coordenadas da vaga
  - O bloqueio é associado à placa do veículo e tem um tempo de expiração configurável
  - Apenas o veículo que possui o bloqueio pode completar a operação de estacionamento
  - O bloqueio é liberado após a conclusão da operação ou em caso de falha

#### 5.4.2 Controle de Idempotência

- **Implementação**: Através dos métodos `checkAndMarkIdempotency` e `releaseIdempotencyKey` da interface `DistributedLockPort`
- **Finalidade**: Garantir que eventos duplicados não sejam processados múltiplas vezes
- **Funcionamento**:
  - Cada evento recebido é marcado com uma chave de idempotência baseada nas coordenadas da vaga
  - Se um evento com as mesmas coordenadas já foi processado, o sistema rejeita o evento duplicado
  - As chaves de idempotência são liberadas quando um veículo deixa a vaga (evento EXIT)

#### 5.4.3 Convenções de Nomenclatura de Chaves

- **Bloqueios**: Utilizam o prefixo `parking:lock:{latitude}_{longitude}`
- **Idempotência**: Utilizam o prefixo `parking:idempotency:{latitude}_{longitude}`

#### 5.4.4 Benefícios da Utilização do Redis

- **Alta Performance**: Operações em memória com baixa latência
- **Expiração Automática**: Chaves podem ser configuradas para expirar automaticamente, evitando bloqueios permanentes
- **Escalabilidade**: Permite que múltiplas instâncias da aplicação compartilhem o mesmo mecanismo de bloqueio
- **Confiabilidade**: Garante a integridade das operações mesmo em ambientes com alta concorrência

## 6. Estrutura do Projeto

### 6.1 Organização de Pacotes

```
io.github.martinsjavacode.parkingmanagement/
├── adapters/
│   ├── extension/                  # Extensões para conversão entre modelos
│   │   ├── NumberExtension.kt
│   │   ├── parking/
│   │   ├── revenue/
│   │   └── vehicle/
│   ├── inbound/                    # Adaptadores de entrada (controllers)
│   │   ├── event/
│   │   │   └── WebhookEvent.kt     # Endpoint para receber eventos de webhook
│   │   └── rest/                   # Controllers REST
│   │       ├── parking/
│   │       ├── revenue/
│   │       ├── spot/
│   │       └── vehicle/
│   └── outbound/                   # Adaptadores de saída (repositórios)
│       ├── client/                 # Clientes para APIs externas
│       ├── redis/                  # Adaptadores para Redis
│       │   └── RedisDistributedLockAdapter.kt
│       └── persistence/            # Adaptadores para persistência
│           ├── ParkingCustomQueryRepositoryAdapter.kt
│           ├── ParkingEventRepositoryAdapter.kt
│           ├── ParkingRepositoryAdapter.kt
│           ├── ParkingSpotRepositoryAdapter.kt
│           └── RevenueRepositoryAdapter.kt
│   │       ├── revenue/
│   │       ├── spot/
│   │       └── vehicle/
│   └── outbound/                   # Adaptadores de saída (repositórios)
│       ├── client/                 # Clientes para APIs externas
│       └── persistence/            # Adaptadores para persistência
│           ├── ParkingCustomQueryRepositoryAdapter.kt
│           ├── ParkingEventRepositoryAdapter.kt
│           ├── ParkingRepositoryAdapter.kt
│           ├── ParkingSpotRepositoryAdapter.kt
│           └── RevenueRepositoryAdapter.kt
├── application/                    # Camada de aplicação
│   └── usecases/                   # Casos de uso da aplicação
│       ├── parking/                # Casos de uso relacionados a estacionamento
│       ├── plate/                  # Casos de uso relacionados a placas de veículos
│       ├── revenue/                # Casos de uso relacionados a receitas
│       └── webhook/                # Casos de uso para processamento de webhooks
├── domain/                         # Camada de domínio
│   ├── enums/                      # Enumerações do domínio
│   │   ├── CurrencyType.kt
│   │   ├── EventType.kt
│   │   ├── ExceptionType.kt
│   │   ├── InternalCodeType.kt
│   │   └── OccupancyActionType.kt
│   ├── gateway/                    # Portas (interfaces) para adaptadores
│   │   ├── client/
│   │   ├── redis/                  # Interfaces para Redis
│   │   │   └── DistributedLockPort.kt
│   │   └── repository/
│   │       ├── parking/
│   │       └── revenue/
│   ├── model/                      # Modelos de domínio
│   │   ├── parking/
│   │   │   ├── Parking.kt
│   │   │   ├── ParkingCapacityAndOccupancy.kt
│   │   │   ├── ParkingEvent.kt
│   │   │   ├── ParkingSpot.kt
│   │   │   └── ParkingSpotStatus.kt
│   │   ├── revenue/
│   │   │   └── Revenue.kt
│   │   ├── vehicle/
│   │   │   └── PlateStatus.kt
│   │   └── webhook/
│   │       └── WebhookEvent.kt
│   └── rules/                      # Regras de negócio
│       ├── DateTimeRules.kt
│       └── OperationalRules.kt
├── infra/                          # Infraestrutura
│   ├── config/                     # Configurações
│   │   ├── GlobalExceptionHandler.kt
│   │   ├── I18nConfig.kt
│   │   └── TraceContext.kt
│   ├── exception/                  # Exceções personalizadas
│   │   ├── BusinessException.kt
│   │   ├── EntryEventNotFoundException.kt
│   │   └── ... (outras exceções)
│   └── persistence/                # Implementações de persistência
│       ├── parking/
│       │   ├── entity/             # Entidades JPA
│       │   └── repository/         # Repositórios Spring Data
│       └── revenue/
│           ├── entity/
│           └── repository/
└── ParkingManagementApplication.kt # Classe principal da aplicação
```

### 6.2 Principais Componentes

#### 6.2.1 Modelos de Domínio

- **Parking**: Representa um estacionamento com setor, preço base, capacidade e horários
- **ParkingSpot**: Representa uma vaga individual com coordenadas geográficas
- **ParkingEvent**: Representa eventos de entrada, estacionamento e saída de veículos
- **Revenue**: Representa a receita diária por setor de estacionamento
- **ParkingCapacityAndOccupancy**: Representa a capacidade e ocupação atual de um estacionamento
- **PlateStatus**: Representa o status atual de um veículo no estacionamento

#### 6.2.2 Regras de Negócio

- **OperationalRules**: Implementa regras como precificação dinâmica e cálculo de tarifas
  - Multiplicador de preço baseado na taxa de ocupação:
    - Ocupação < 25%: desconto de 10% (multiplicador 0.9)
    - Ocupação entre 25% e 50%: preço base (multiplicador 1.0)
    - Ocupação entre 50% e 75%: acréscimo de 10% (multiplicador 1.1)
    - Ocupação > 75%: acréscimo de 25% (multiplicador 1.25)
  - Validação de coordenadas geográficas
  - Cálculo de tarifas de estacionamento baseado em tempo e multiplicador

- **DateTimeRules**: Regras relacionadas a datas e horários
  - Formatação e parsing de datas e horas
  - Validação de períodos de operação

#### 6.2.3 Casos de Uso

- **Processamento de eventos de webhook**:
  - `EntryEventHandler`: Processa eventos de entrada de veículos
  - `ParkedEventHandler`: Processa eventos de estacionamento de veículos e implementa bloqueio distribuído
  - `ExitEventHandler`: Processa eventos de saída de veículos, calcula tarifas e libera chaves de idempotência

- **Consulta de status**:
  - `GetPlateStatusHandler`: Consulta o status atual de um veículo por placa
  - `GetSpotStatusHandler`: Consulta o status atual de uma vaga por coordenadas

- **Gestão de receitas**:
  - `GetDailyBillingByParkingSectorHandler`: Consulta receitas por setor e data
  - `UpdateRevenueHandler`: Atualiza receitas após saída de veículos

#### 6.2.4 Adaptadores de Repositório e Infraestrutura

- **ParkingRepositoryAdapter**: Persistência de estacionamentos
- **ParkingEventRepositoryAdapter**: Persistência de eventos de estacionamento
- **ParkingSpotRepositoryAdapter**: Persistência de vagas
- **RevenueRepositoryAdapter**: Persistência de receitas
- **ParkingCustomQueryRepositoryAdapter**: Consultas personalizadas para estacionamentos
- **RedisDistributedLockAdapter**: Implementação de bloqueio distribuído e idempotência usando Redis

#### 6.2.5 Controllers REST

- **VehicleRestController**: Endpoints para consulta de status de veículos
- **SpotRestController**: Endpoints para consulta de status de vagas
- **RevenueRestController**: Endpoints para consulta de receitas
- **WebhookEvent**: Endpoint para receber eventos de webhook

## 7. Fluxo do Sistema

### 7.1 Processamento de Eventos de Webhook

#### 7.1.1 Evento ENTRY (Entrada)

1. O sistema recebe um evento de entrada com placa do veículo e timestamp
2. Verifica se há alguma garagem em funcionamento no momento
3. Registra o evento ENTRY com multiplicador padrão 1.0

**Exemplo JSON recebido:**
```json
{
  "license_plate": "ZUL0001",
  "event_type": "ENTRY",
  "timestamp": "2023-10-01T10:00:00Z"
}
```

#### 7.1.2 Evento PARKED (Estacionado)

1. O sistema recebe um evento de estacionamento com placa e coordenadas
2. Verifica se há duplicidade do evento usando Redis (controle de idempotência)
3. Adquire um bloqueio distribuído para a vaga específica usando Redis
4. Verifica se o veículo está registrado como ENTRY
5. Calcula o multiplicador de preço baseado na ocupação atual:
   - Lotação < 25%: desconto de 10% (multiplicador 0.9)
   - Lotação ≤ 50%: preço base (multiplicador 1.0)
   - Lotação ≤ 75%: acréscimo de 10% (multiplicador 1.1)
   - Lotação ≤ 100%: acréscimo de 25% (multiplicador 1.25)
   - Lotação > 100%: não permite estacionar
6. Registra o evento PARKED com o multiplicador calculado
7. Cria um registro inicial de receita para o dia, se necessário
8. Libera o bloqueio distribuído após a conclusão da operação

**Exemplo JSON recebido:**
```json
{
  "license_plate": "ZUL0001",
  "lat": -23.561684,
  "lng": -46.655981,
  "event_type": "PARKED"
}
```

#### 7.1.3 Evento EXIT (Saída)

1. O sistema recebe um evento de saída com placa e horário
2. Verifica se o veículo está registrado como PARKED
3. Calcula o valor a ser cobrado com base no:
   - Multiplicador definido no evento PARKED
   - Tempo de permanência
   - Preço base do setor
4. Registra o evento EXIT com o valor calculado
5. Atualiza a receita do dia para o setor correspondente
6. Libera a chave de idempotência no Redis para permitir que a vaga seja utilizada novamente

**Exemplo JSON recebido:**
```json
{
  "license_plate": "ZUL0001",
  "exit_time": "2023-10-01T12:00:00.000Z",
  "event_type": "EXIT"
}
```

### 7.2 Consultas REST

#### 7.2.1 Consulta de Status de Veículo

**Endpoint:** `GET /plates/{licensePlate}/status`

**Request:**
```
GET /plates/ZUL0001/status
```

**Response:**
```json
{
  "license_plate": "ZUL0001",
  "price_until_now": 15.50,
  "entry_time": "2025-01-01T12:00:00.000Z",
  "time_parked": "2025-01-01T14:30:00.000Z",
  "lat": -23.561684,
  "lng": -46.655981
}
```

#### 7.2.2 Consulta de Status de Vaga

**Endpoint:** `GET /spots/status?lat={latitude}&lng={longitude}`

**Request:**
```
GET /spots/status?lat=-23.561684&lng=-46.655981
```

**Response:**
```json
{
  "ocupied": true,
  "license_plate": "ZUL0001",
  "price_until_now": 15.50,
  "entry_time": "2025-01-01T12:00:00.000Z",
  "time_parked": "2025-01-01T14:30:00.000Z"
}
```

#### 7.2.3 Consulta de Receitas

**Endpoint:** `GET /revenues/{sector}?date={date}`

**Request:**
```
GET /revenues/A?date=2025-01-01
```

**Response:**
```json
{
  "amount": 1250.75,
  "currency": "BRL",
  "timestamp": "2025-01-01T23:59:59.000Z"
}
```

### 7.3 Melhorias Implementadas na API

O sistema originalmente utilizava endpoints menos intuitivos, mas foi refatorado para seguir padrões RESTful mais descritivos:

| Endpoint Original | Endpoint Atual | Descrição |
|------------------|----------------|-----------|
| `POST /plate-status` | `GET /plates/{licensePlate}/status` | Consulta o status de um veículo por placa |
| `POST /spot-status` | `GET /spots/status?lat={lat}&lng={lng}` | Consulta o status de uma vaga por coordenadas |
| `GET /revenue` | `GET /revenues/{sector}?date={date}` | Consulta a receita por setor e data |

Esta melhoria torna a API mais intuitiva, autodocumentada e alinhada com as melhores práticas RESTful.

## 8. Modelo de Dados

### 8.1 Estrutura das Tabelas

#### 8.1.1 Tabela: Parking
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

#### 8.1.2 Tabela: ParkingSpot
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

#### 8.1.3 Tabela: ParkingEvent
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

#### 8.1.4 Tabela: Revenue
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

### 8.2 Relacionamentos

- Um `Parking` pode ter múltiplos `ParkingSpot`
- Um `ParkingSpot` pertence a um único `Parking`
- Um `ParkingEvent` está associado a uma placa de veículo
- Uma `Revenue` está associada a um `Parking` e uma data específica

## 9. Testes

### 9.1 Estratégia de Testes

O projeto implementa uma estratégia de testes abrangente, incluindo:

- **Testes Unitários**: Verificam o comportamento de componentes individuais
- **Testes de Carga**: Avaliam o desempenho do sistema sob diferentes condições

### 9.2 Testes Unitários

Os testes unitários são implementados usando JUnit 5, Kotest e MockK, focando em:

- **Adaptadores de Repositório**:
  - `ParkingRepositoryAdapterTest`
  - `ParkingEventRepositoryAdapterTest`
  - `ParkingSpotRepositoryAdapterTest`
  - `RevenueRepositoryAdapterTest`
  - `ParkingCustomQueryRepositoryAdapterTest`

- **Casos de Uso**:
  - Testes para handlers de eventos de webhook
  - Testes para consultas de status
  - Testes para gestão de receitas

- **Regras de Negócio**:
  - Testes para `OperationalRules`
  - Testes para `DateTimeRules`

- **Extensões e Conversões**:
  - Testes para conversões entre modelos de domínio e entidades

### 9.3 Testes de Carga

Os testes de carga são implementados usando k6, com a seguinte estrutura:

```
performance-tests/
├── config.js                  # Configurações gerais dos testes
├── load-test.js               # Teste de carga constante
├── stress-test.js             # Teste de estresse com aumento gradual
├── spike-test.js              # Teste de pico de carga
├── scenarios/                 # Cenários de teste
│   ├── parking-entry.js       # Cenário de entrada no estacionamento
│   ├── parking-exit.js        # Cenário de saída do estacionamento
│   ├── parking-parked.js      # Cenário de veículos estacionados
│   ├── get-spot-status.js     # Cenário de consulta de status de vagas
│   ├── get-plate-status.js    # Cenário de consulta de status por placa
│   └── get-revenue.js         # Cenário de consulta de receita
└── package.json               # Configuração do projeto
```

#### 9.3.1 Tipos de Testes de Carga

1. **Teste de Carga (Load Test)**: Simula um número constante de usuários acessando o sistema simultaneamente por um período prolongado.

2. **Teste de Estresse (Stress Test)**: Aumenta gradualmente o número de usuários para identificar o ponto de ruptura do sistema.

3. **Teste de Pico (Spike Test)**: Simula um aumento repentino e significativo no tráfego para avaliar como o sistema se comporta sob picos de carga.

### 9.4 Cobertura de Testes

O projeto utiliza JaCoCo para gerar relatórios de cobertura de código, com os seguintes resultados:

- **Instruções**: 95% de cobertura (4.254 de 4.438 instruções)
- **Branches**: 90% de cobertura (138 de 153 branches)
- **Complexidade**: 90% de cobertura (174 de 194)
- **Linhas**: 96% de cobertura (825 de 861 linhas)
- **Métodos**: 95% de cobertura (117 de 123 métodos)
- **Classes**: 100% de cobertura (59 de 59 classes)

## 10. Documentação da API

### 10.1 OpenAPI/Swagger

O projeto utiliza o plugin `org.springdoc.openapi-gradle-plugin` para gerar documentação OpenAPI, disponível em:

```
http://localhost:{SERVER_PORT}/swagger-ui.html
```

A documentação também está disponível em formato ReDoc, acessível através do endpoint:

```
https://martinsjavacode.github.io/parking-management/
```

### 10.2 Anotações OpenAPI

Todos os endpoints são documentados com anotações OpenAPI detalhadas, incluindo:

- Descrições de operações
- Parâmetros de requisição
- Esquemas de resposta
- Códigos de status HTTP
- Exemplos de uso

Exemplo de anotação para o `SpotRestController`:

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

## 11. Implantação

### 11.1 Requisitos

- JDK 17 ou superior
- Docker e Docker Compose
- PostgreSQL (ou container Docker)
- Redis (ou container Docker)

### 11.2 Variáveis de Ambiente

O sistema utiliza as seguintes variáveis de ambiente para configuração:

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `SERVER_PORT` | Porta em que a aplicação será executada | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo do Spring (dev, prod, test) | `default` |
| `DOCKER_COMPOSE_ENABLED` | Habilita o uso automático do Docker Compose | `false` |
| `GARAGE_API_URL` | URL da API do simulador de garagem | `http://localhost:3000` |
| `APP_VERSION` | Versão da aplicação para documentação | `1.0.0` |

#### Detalhamento das Variáveis

- **SERVER_PORT**: Define a porta HTTP em que a aplicação estará disponível. Importante para evitar conflitos com outros serviços.

- **SPRING_PROFILES_ACTIVE**: Controla qual conjunto de configurações será carregado. Útil para ter diferentes configurações em ambientes de desenvolvimento, teste e produção.

- **DOCKER_COMPOSE_ENABLED**: Quando definido como `true`, permite que o Spring Boot inicie automaticamente os containers Docker necessários (PostgreSQL e Redis) ao iniciar a aplicação.

- **GARAGE_API_URL**: Especifica o endpoint do simulador de garagem que envia eventos de webhook para o sistema.

- **APP_VERSION**: Utilizada na documentação OpenAPI/Swagger para indicar a versão atual da API.

### 11.3 Configuração Local

1. Clone o repositório:
   ```bash
   git clone https://github.com/martinsjavacode/parking-management.git
   cd parking-management
   ```

2. Configure as variáveis de ambiente necessárias:
   ```bash
   export SERVER_PORT=3003
   export SPRING_PROFILES_ACTIVE=dev
   export DOCKER_COMPOSE_ENABLED=true
   export GARAGE_API_URL=http://localhost:3000
   export APP_VERSION=1.0.0
   ```

3. Execute os serviços necessários:
   ```bash
   docker-compose up -d postgres redis
   ```
   
   Alternativamente, o projeto está configurado com Spring Docker Compose, que automaticamente inicia containers Docker com PostgreSQL e Redis ao iniciar a aplicação, eliminando a necessidade de iniciar os serviços manualmente.

4. Compile e inicie a aplicação:
   ```bash
   ./gradlew clean build bootRun --refresh-dependencies
   ```

### 11.3 Execução do Simulador

Execute o simulador de estacionamento para gerar eventos:

```bash
docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
```

### 11.4 Execução dos Testes de Carga

```bash
cd performance-tests
k6 run load-test.js  # Teste de carga
k6 run stress-test.js  # Teste de estresse
k6 run spike-test.js  # Teste de pico
```

## 12. Considerações Futuras

### 12.1 Melhorias Propostas

- Implementação de autenticação e autorização
- Suporte a múltiplas moedas para pagamentos
- Dashboard administrativo para monitoramento em tempo real
- Integração com sistemas de pagamento
- Otimizações de desempenho para alta concorrência
- Expansão dos testes de integração
- Expandir Payloads: Adicionar informações no evento ENTRY para incluir o setor e permitir uma lógica mais granular.

### 12.2 Limitações Identificadas

* Payload Insuficiente no Evento ENTRY:
    * Não contém informações detalhadas, como o setor, dificultando a identificação do estacionamento correto.

## 13. Escalabilidade

O sistema foi projetado considerando escalabilidade horizontal:

- Uso de coroutines para processamento assíncrono
- Banco de dados com índices otimizados
- Arquitetura modular que permite distribuição de componentes
- Testes de carga para validar limites de escalabilidade
- Uso de Redis para bloqueios distribuídos e controle de idempotência

## 14. Conclusão

O Sistema de Gestão de Estacionamento oferece uma solução completa e robusta para o gerenciamento de estacionamentos com precificação dinâmica. A arquitetura hexagonal adotada proporciona flexibilidade, testabilidade e manutenibilidade, enquanto as tecnologias escolhidas (Kotlin, Spring Boot, Redis, PostgreSQL) garantem desempenho e confiabilidade.

A extensa cobertura de testes e documentação detalhada facilitam a manutenção e evolução do sistema, tornando-o uma base sólida para futuras expansões e integrações.
