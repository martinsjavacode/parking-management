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
- API do sistema de estacionamento em execução

## Configuração

Edite o arquivo `config.js` para ajustar:

- URL base da API
- Número de usuários virtuais (VUs)
- Duração dos testes
- Tempos de espera entre requisições

## Executando os Testes

### Teste de Carga

```shell
  k6 run load-test.js
```

### Teste de Estresse

```bash
  k6 run stress-test.js
```

### Teste de Pico

```bash
  k6 run spike-test.js
```

### Usando os scripts do package.json

```bash
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

## Personalizando os Testes

Para adicionar novos cenários:

1. Crie um novo arquivo na pasta `scenarios/`
2. Importe o cenário nos arquivos de teste
3. Adicione o cenário ao grupo de testes
