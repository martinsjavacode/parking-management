import { sleep } from 'k6';
import { group } from 'k6';
import { STRESS_TEST_VUS, STRESS_TEST_DURATION } from './config.js';

// Importar cenários
import parkingEntry from './scenarios/parking-entry.js';
import parkingExit from './scenarios/parking-exit.js';
import getParkingStatus from './scenarios/get-parking-status.js';

export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Rampa de subida para 10 VUs
    { duration: '1m', target: 20 },    // Rampa de subida para 20 VUs
    { duration: '2m', target: STRESS_TEST_VUS }, // Rampa de subida para o máximo de VUs
    { duration: '3m', target: STRESS_TEST_VUS }, // Manter carga máxima
    { duration: '1m', target: 0 },     // Rampa de descida para 0 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95% das requisições devem completar abaixo de 1s
    http_req_failed: ['rate<0.05'],    // Menos de 5% das requisições podem falhar
  },
};

export default function() {
  group('Parking Entry', () => {
    parkingEntry();
  });
  
  sleep(1);
  
  group('Parking Exit', () => {
    parkingExit();
  });
  
  sleep(1);
  
  group('Parking Status', () => {
    getParkingStatus();
  });
}
