import http from 'k6/http';
import {sleep, check} from 'k6';
import {BASE_URL, getThinkTime, getSpot} from '../config.js';

export default function(license_plate) {
    const spot = getSpot()
    const parkedPayload = JSON.stringify({
        license_plate,
        lat: spot.lat,
        lng: spot.lng,
        event_type: "PARKED"
    })

    const endpoint = `${BASE_URL}/webhook`
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    // Registrar entrada de veículo na vaga
    const responseParked = http.post(endpoint, parkedPayload, params);

    check(responseParked, {
        'status is 201': (r) => r.status === 201,
        'Response contains expected data': (r) => r.json().key === 'internalCode'
    })

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
