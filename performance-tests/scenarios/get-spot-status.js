import http from 'k6/http';
import {sleep, check} from 'k6';
import {BASE_URL, getThinkTime} from '../config.js';

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

    check(response, {
        'status is 200': (r) => r.status === 200,
        'has occupancy data': (r) => r.json('price_until_now') !== undefined,
        'Response contains expected data': (r) => r.json().key === 'internalCode'
    });

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
