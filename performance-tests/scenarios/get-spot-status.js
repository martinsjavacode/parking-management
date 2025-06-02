import http from 'k6/http';
import {sleep} from 'k6';
import {BASE_URL, checkRestResponse, getThinkTime} from '../config.js';

export default function (spot) {
    const latitude = spot.lat
    const longitude = spot.lng

    // Consultar status do estacionamento
    const response = http.get(`${BASE_URL}/spots/status?lat=${latitude}&lng=${longitude}`);

    checkRestResponse(response, 'price_until_now')

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
