import http from 'k6/http';
import { sleep, check } from 'k6';
import { BASE_URL, getThinkTime } from '../config.js';

export default function() {
  // Dados de exemplo para entrada de veículo no estacionamento
  const payload = JSON.stringify({
    plate: `ABC-${Math.floor(Math.random() * 9999)}`,
    vehicleType: 'CAR',
    entryTime: new Date().toISOString()
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // Registrar entrada de veículo
  const response = http.post(`${BASE_URL}/parking/entry`, payload, params);
  
  check(response, {
    'status is 201': (r) => r.status === 201,
    'transaction created': (r) => r.json('id') !== undefined,
  });

  // Tempo de espera entre requisições
  sleep(getThinkTime());
}
