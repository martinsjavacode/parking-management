# Garage Management Reactive System

## **Descrição do Projeto**

Este projeto implementa um sistema de gestão de estacionamentos utilizando programação reativa, baseado em Kotlin com coroutines e Spring WebFlux. O objetivo é gerenciar vagas, entradas e saídas de veículos, além de calcular o faturamento baseado em regras de preços dinâmicos. O sistema é modularizado seguindo a arquitetura hexagonal.

---

## **Arquitetura**

A aplicação utiliza a **arquitetura hexagonal (ports and adapters)** para separar claramente a lógica de negócio das dependências externas. A estrutura é dividida nas seguintes camadas:

### 1. **Domínio**

* Contém as entidades e lógica de negócio.
* Independente de frameworks e tecnologias externas.

**Classes principais:**

* `Garage.kt`: Representa uma garagem com múltiplos setores.
* `ParkingSpot.kt`: Representa uma vaga de estacionamento e sua lógica de preços dinâmicos.
* `Event.kt`: Representa os eventos de entrada, saída e ocupação de vagas.

### 2. **Aplicação**

* Contém os serviços que orquestram casos de uso e regras de negócio.
* Gerencia a comunicação entre a camada de domínio e infraestrutura.

**Principais componentes:**

* `GarageService.kt`: Lida com consultas de ocupação e gerenciamento de setores.
* `PricingService.kt`: Realiza o cálculo de preços dinâmicos.
* `EventHandler.kt`: Processa os eventos recebidos pelo simulador.

### 3. **Infraestrutura**

* Adapta a lógica de domínio para interagir com o mundo externo.

**Principais componentes:**

* **Persistência**: Repositórios e entidades para interagir com o banco de dados.

    * `GarageRepository.kt`, `ParkingSpotRepository.kt`.
    * `GarageEntity.kt`, `ParkingSpotEntity.kt`.
* **API Web**:

    * `WebhookController.kt`: Recebe eventos do simulador.
    * `GarageController.kt`: Fornece endpoints REST para consulta de ocupação e vagas.
    * `RevenueController.kt`: Consulta o faturamento por setor e data.
* **Configuração**:

    * `WebClientConfig.kt`: Configuração do WebClient para comunicação com o simulador.

---

## **Estrutura do Projeto**

```
src/
├── main/
│   ├── kotlin/
│   │   ├── com.example.garage/
│   │   │   ├── application/
│   │   │   │   ├── services/
│   │   │   │   │   ├── GarageService.kt
│   │   │   │   │   └── PricingService.kt
│   │   │   │   └── events/
│   │   │   │       └── EventHandler.kt
│   │   │   ├── domain/
│   │   │   │   ├── Garage.kt
│   │   │   │   ├── ParkingSpot.kt
│   │   │   │   └── Event.kt
│   │   │   ├── infrastructure/
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── GarageRepository.kt
│   │   │   │   │   ├── ParkingSpotRepository.kt
│   │   │   │   │   └── entities/
│   │   │   │   │       ├── GarageEntity.kt
│   │   │   │   │       └── ParkingSpotEntity.kt
│   │   │   │   ├── web/
│   │   │   │   │   ├── WebhookController.kt
│   │   │   │   │   ├── GarageController.kt
│   │   │   │   │   └── RevenueController.kt
│   │   │   │   └── config/
│   │   │   │       └── WebClientConfig.kt
│   │   │   └── GarageManagementApplication.kt
│   └── resources/
│       ├── application.yml
│       └── db/migration/
│           ├── V1__Create_garages_table.sql
│           └── V2__Create_parking_spots_table.sql
└── test/
    ├── kotlin/
    │   ├── com.example.garage/
    │   │   ├── application/
    │   │   │   └── GarageServiceTest.kt
    │   │   ├── domain/
    │   │   │   └── GarageDomainTest.kt
    │   │   └── infrastructure/
    │   │       └── persistence/
    │   │           └── GarageRepositoryTest.kt
    └── resources/
        └── application-test.yml
```

---

## **Requisitos do Projeto**

1. **Tecnologias**:

    * Linguagem: Kotlin
    * Framework: Spring WebFlux
    * Banco de Dados: MySQL ou PostgreSQL
    * Migrações: Flyway

2. **Funcionalidades**:

    * Receber eventos de entrada, saída e ocupação via Webhook.
    * Consultar status de vagas e ocupação geral.
    * Calcular preços dinâmicos com base na ocupação.
    * Registrar faturamento por setor e data.

3. **Regras de Negócio**:

    * Regras de preços dinâmicos:

        * Menor que 25% de ocupação: desconto de 10%.
        * Entre 25% e 50%: preço base.
        * Entre 50% e 75%: acréscimo de 10%.
        * Entre 75% e 100%: acréscimo de 25%.
    * Com 100% de lotação, o setor é fechado até liberar vagas.

4. **Teste**:

    * Testes unitários para camada de domínio e serviços.
    * Testes de integração para controladores e repositórios.

---

## **Como Executar**

1. Clone o repositório:

   ```bash
   git clone https://github.com/example/garage-management.git
   cd garage-management
   ```

2. Configure o banco de dados:

    * Altere o arquivo `application.yml` com suas credenciais.

3. Rode as migrações:

   ```bash
    ./gradlew flywayMigrate
   ```

4. Inicie a aplicação:

    ```bash
    ./gradlew bootRun
    ```

5. Execute o simulador:

    ```bash
    docker run -d --network="host" cfontes0estapar/garage-sim:1.0.0
    ```
