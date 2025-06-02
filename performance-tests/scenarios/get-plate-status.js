import http from 'k6/http';
import {sleep} from 'k6';
import {BASE_URL, checkRestResponse, getSpot, getThinkTime} from '../config.js';

export default function (license_plate) {
    // Consultar status do estacionamento
    const response = http.get(`${BASE_URL}/plates/${license_plate}/status`);

    checkRestResponse(response, 'price_until_now')

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
