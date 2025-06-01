import http from 'k6/http';
import {sleep} from 'k6';
import {BASE_URL, checkRestResponse, getSpot, getThinkTime} from '../config.js';

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

    checkRestResponse(response, 'price_until_now')

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
