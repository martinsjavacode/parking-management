import { sleep } from 'k6';
import { group } from 'k6';
import {SPIKE_TEST_VUS, SPIKE_TEST_DURATION, getThinkTime} from './config.js';

// Importar cenários
import parkingEntry from './scenarios/parking-entry.js';
import parkingExit from './scenarios/parking-exit.js';
import parkingParked from "./scenarios/parking-parked.js";
import getPlateStatus from './scenarios/get-plate-status.js';
import getSpotStatus from "./scenarios/get-spot-status.js";
import getRevenue from "./scenarios/get-revenue.js";

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

export default function () {
    let vehicle = null
    let spot = null
    group('Parking Entry', () => {
        vehicle = parkingEntry();
    });

    sleep(.5);

    group('Parking Parked', () => {
        spot = parkingParked(vehicle.license_plate);
    });

    sleep(.5);

    group('Plate Status', () => {
        getPlateStatus(vehicle.license_plate);
    });

    group('Parking Exit', () => {
        parkingExit(vehicle.license_plate);
    });

    sleep(.5);

    group('Spot Status', () => {
        getSpotStatus(spot);
    });

    sleep(.5);

    group('Revenue', () => {
        getRevenue(spot)
    });
}
