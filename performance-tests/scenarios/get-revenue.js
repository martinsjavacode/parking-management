import http from 'k6/http';
import {sleep} from 'k6';
import {BASE_URL, checkRestResponse, formatDate, getSpot, getThinkTime} from '../config.js';

export default function () {
    const spot = getSpot()
    const payload = JSON.stringify({
        date: formatDate(new Date()),
        sector: spot.sector
    })
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    // Consultar status do estacionamento
    const response = http.get(`${BASE_URL}/revenue`, payload, params);

    checkRestResponse(response, 'amount');

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}

