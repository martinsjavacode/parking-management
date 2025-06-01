import http from 'k6/http';
import {sleep} from 'k6';
import {BASE_URL, checkEventResponse, getThinkTime} from '../config.js';

export default function (license_plate) {
    const endpoint = `${BASE_URL}/webhook`
    const exitPayload = JSON.stringify({
        event_type: 'EXIT',
        license_plate,
        exit_time: new Date().toISOString(),
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Registrar saída de veículo
    const responseExit = http.post(endpoint, exitPayload, params);

    checkEventResponse(responseExit);

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}
