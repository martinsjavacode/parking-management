import {sleep} from 'k6';
import {group} from 'k6';
import {LOAD_TEST_VUS, LOAD_TEST_DURATION, getThinkTime} from './config.js';

// Importar cenários
import parkingEntry from './scenarios/parking-entry.js';
import parkingParked from './scenarios/parking-parked.js';
import parkingExit from './scenarios/parking-exit.js';
import getParkingStatus from './scenarios/get-spot-status.js';
import getSpotStatus from './scenarios/get-spot-status.js';
import getRevenue from "./scenarios/get-revenue.js";

export const options = {
    vus: LOAD_TEST_VUS,
    duration: LOAD_TEST_DURATION,
    thresholds: {
        http_req_duration: ['p(95)<500'], // 95% das requisições devem completar abaixo de 500ms
        http_req_failed: ['rate<0.01'],   // Menos de 1% das requisições podem falhar
    },
};

export default function () {
    let vehicle = null
    group('Parking Entry', () => {
        vehicle = parkingEntry();
    });

    sleep(getThinkTime() + Math.floor(Math.random() * 30) + 1);

    group('Parking Parked', () => {
        parkingParked(vehicle.license_plate);
    });

    sleep(Math.floor(Math.random() * 30) + 1);

    group('Parking Status', () => {
        getParkingStatus();
    });

    group('Parking Exit', () => {
        parkingExit(vehicle.license_plate);
    });

    sleep(2);

    group('Spot Status', () => {
        getSpotStatus(vehicle.license_plate);
    });

    sleep(2);

    group('Revenue', () => {
        getRevenue()
    });
}
