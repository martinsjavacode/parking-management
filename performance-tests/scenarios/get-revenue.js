import http from 'k6/http';
import {sleep} from 'k6';
import {BASE_URL, checkRestResponse, formatDate, getSpot, getThinkTime} from '../config.js';

export default function (spot) {
    const date = formatDate(new Date())
    const sector = spot.sector

    // Consultar status do estacionamento
    const response = http.get(`${BASE_URL}/revenues/${sector}?date=${date}`);

    checkRestResponse(response, 'amount');

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}

