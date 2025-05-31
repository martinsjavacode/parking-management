import { sleep } from 'k6';
import { group } from 'k6';
import { LOAD_TEST_VUS, LOAD_TEST_DURATION } from './config.js';

// Importar cenários
import parkingEntry from './scenarios/parking-entry.js';
import parkingExit from './scenarios/parking-exit.js';
import getParkingStatus from './scenarios/get-parking-status.js';

export const options = {
  vus: LOAD_TEST_VUS,
  duration: LOAD_TEST_DURATION,
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% das requisições devem completar abaixo de 500ms
    http_req_failed: ['rate<0.01'],   // Menos de 1% das requisições podem falhar
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
