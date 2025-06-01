import http from 'k6/http';
import {sleep, check} from 'k6';
import {BASE_URL, getThinkTime} from '../config.js';

export default function () {
    // Dados de exemplo para entrada de veículo no estacionamento
    const license_plate = `ABC-${Math.floor(Math.random() * 9999)}`
    const entry = Date()
    const endpoint = `${BASE_URL}/webhook`

    const entryPayload = JSON.stringify({
        license_plate,
        entry_time: entry.setHours(entry.getHours() - 5).toISOString(),
        event_type: 'ENTRY'
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Registrar entrada de veículo
    const response = http.post(endpoint, entryPayload, params);

    check(response, {
        'status is 201': (r) => r.status === 201,
        'Response contains expected data': (r) => r.json().key === 'internalCode'
    });

    return { license_plate }
}
