# Testes de Carga - Sistema de Gerenciamento de Estacionamento

Este diretório contém testes de carga para o sistema de gerenciamento de estacionamento utilizando o k6.

## Estrutura do Projeto

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

## Pré-requisitos

- k6 instalado (https://k6.io/docs/getting-started/installation/)
- API do sistema de estacionamento em execução (por padrão em http://localhost:8080)

## Configuração

Edite o arquivo `config.js` para ajustar:

- URL base da API (por padrão: http://localhost:8080)
- Número de usuários virtuais (VUs)
- Duração dos testes
- Tempos de espera entre requisições
- Dados de vagas de estacionamento

## Fluxo de Teste

Os testes simulam o ciclo completo de uso do estacionamento:

1. **Entrada do veículo** (parking-entry.js): Registra a entrada de um veículo com placa aleatória
2. **Estacionamento na vaga** (parking-parked.js): Registra o veículo estacionado em uma vaga específica
3. **Consulta por placa** (get-plate-status.js): Verifica o status do veículo por placa
4. **Saída do veículo** (parking-exit.js): Registra a saída do veículo
5. **Consulta de vaga** (get-spot-status.js): Verifica o status da vaga após a saída
6. **Consulta de receita** (get-revenue.js): Verifica a receita gerada pelo setor

## Executando os Testes

### Teste de Carga

```shell
k6 run load-test.js
```
Executa um teste com carga constante de 10 VUs por 60 segundos.

### Teste de Estresse

```bash
k6 run stress-test.js
```
Executa um teste com aumento gradual de carga até 30 VUs, mantendo por 3 minutos.

### Teste de Pico

```bash
k6 run spike-test.js
```
Executa um teste com pico rápido de 50 VUs, simulando um surto de tráfego.

### Usando os scripts do package.json

```bash
# Teste rápido com 1 VU por 30 segundos
npm run test:smoke
    
# Teste de carga com 10 VUs por 60 segundos
npm run test:load
    
# Teste de estresse com 30 VUs por 120 segundos
npm run test:stress
    
# Teste de pico com 50 VUs por 30 segundos
npm run test:spike
```

## Interpretando os Resultados

Após a execução dos testes, o k6 exibirá um resumo com métricas importantes:

- **http_req_duration**: Tempo de resposta das requisições
- **http_req_failed**: Taxa de falhas nas requisições
- **iterations**: Número total de iterações executadas
- **vus**: Número de usuários virtuais
- **data_received/data_sent**: Volume de dados trafegados

### Thresholds (Limites)

Os testes incluem thresholds para garantir a qualidade do serviço:

- **Teste de carga**: 95% das requisições devem completar em menos de 500ms
- **Teste de estresse**: 95% das requisições devem completar em menos de 1s
- **Teste de pico**: 95% das requisições devem completar em menos de 2s

## Personalizando os Testes

Para adicionar novos cenários:

1. Crie um novo arquivo na pasta `scenarios/`
2. Importe o cenário nos arquivos de teste
3. Adicione o cenário ao grupo de testes no arquivo de teste desejado
