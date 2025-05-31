import http from 'k6/http';
import { sleep, check } from 'k6';
import { BASE_URL, getThinkTime } from '../config.js';

export default function() {
  // Consultar status do estacionamento
  const response = http.get(`${BASE_URL}/parking/status`);
  
  check(response, {
    'status is 200': (r) => r.status === 200,
    'has occupancy data': (r) => r.json('currentOccupancy') !== undefined,
  });

  // Tempo de espera entre requisições
  sleep(getThinkTime());
}
