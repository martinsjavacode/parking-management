import { sleep } from 'k6';
import { group } from 'k6';
import { SPIKE_TEST_VUS, SPIKE_TEST_DURATION } from './config.js';

// Importar cenários
import parkingEntry from './scenarios/parking-entry.js';
import parkingExit from './scenarios/parking-exit.js';
import getParkingStatus from './scenarios/get-spot-status.js';

export const options = {
  stages: [
    { duration: '10s', target: 10 },    // Rampa de subida para 10 VUs
    { duration: '10s', target: SPIKE_TEST_VUS }, // Pico de carga
    { duration: '30s', target: SPIKE_TEST_VUS }, // Manter pico
    { duration: '10s', target: 10 },    // Rampa de descida para 10 VUs
    { duration: '10s', target: 0 },     // Rampa de descida para 0 VUs
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% das requisições devem completar abaixo de 2s
    http_req_failed: ['rate<0.1'],     // Menos de 10% das requisições podem falhar
  },
};

export default function() {
  group('Parking Entry', () => {
    parkingEntry();
  });

  sleep(0.5);

  group('Parking Exit', () => {
    parkingExit();
  });

  sleep(0.5);

  group('Parking Status', () => {
    getParkingStatus();
  });
}
