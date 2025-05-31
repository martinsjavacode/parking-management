import http from 'k6/http';
import { sleep, check } from 'k6';
import { BASE_URL, getThinkTime } from '../config.js';

export default function() {
  // Primeiro registramos uma entrada para obter um ID de transação
  const entryPayload = JSON.stringify({
    plate: `ABC-${Math.floor(Math.random() * 9999)}`,
    vehicleType: 'CAR',
    entryTime: new Date().toISOString()
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Registrar entrada
  const entryResponse = http.post(`${BASE_URL}/parking/entry`, entryPayload, params);
  
  check(entryResponse, {
    'entry status is 201': (r) => r.status === 201,
  });

  // Extrair ID da transação
  const transactionId = entryResponse.json('id');
  
  // Simular tempo de permanência no estacionamento
  sleep(2);
  
  // Registrar saída
  const exitPayload = JSON.stringify({
    exitTime: new Date().toISOString()
  });
  
  const exitResponse = http.put(`${BASE_URL}/parking/exit/${transactionId}`, exitPayload, params);
  
  check(exitResponse, {
    'exit status is 200': (r) => r.status === 200,
    'payment calculated': (r) => r.json('paymentValue') !== undefined,
  });

  // Tempo de espera entre requisições
  sleep(getThinkTime());
}
