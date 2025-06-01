import http from 'k6/http';
import {sleep, check} from 'k6';
import {BASE_URL, getThinkTime, getSpot} from '../config.js';

export default function () {
    const spot = getSpot()

    const payload = JSON.stringify({
        lat: spot.lat,
        lng: spot.lng
    })
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Consultar status do estacionamento
    const response = http.post(`${BASE_URL}/spot-status`, payload, params);

    check(response, {
        'status is 200': (r) => r.status === 200,
        'has occupancy data': (r) => r.json('price_until_now') !== undefined,
        'Response contains expected data': (r) => r.json().key === 'internalCode'
    });

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
