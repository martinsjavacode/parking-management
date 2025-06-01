import http from 'k6/http';
import {sleep, check} from 'k6';
import {BASE_URL, checkRestResponse, getThinkTime} from '../config.js';

export default function (license_plate) {
    const payload = JSON.stringify({
        license_plate
    })
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    // Consultar status do estacionamento
    const response = http.post(`${BASE_URL}/plate-status`, payload, params);

    checkRestResponse(response, 'price_until_now')

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
