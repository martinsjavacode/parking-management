import http from 'k6/http';
import {sleep, check} from 'k6';
import {BASE_URL} from '../config.js';

export default function (license_plate) {
    const exit = Date()
    const endpoint = `${BASE_URL}/webhook`
    const exitPayload = JSON.stringify({
        event_type: 'EXIT',
        license_plate,
        entry_time: exit.toISOString()
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Registrar saída de veículo
    const responseExit = http.post(endpoint, exitPayload, params);

    check(responseExit, {
        'status is 201': (r) => r.status === 201,
        'Response contains expected data': (r) => r.json().key === 'internalCode'
    })

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
