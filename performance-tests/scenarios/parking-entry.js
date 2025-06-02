import http from 'k6/http';
import {BASE_URL, checkEventResponse} from '../config.js';

export default function () {
    // Dados de exemplo para entrada de veículo no estacionamento
    const license_plate = `ABC${Math.floor(Math.random() * 9999)}`
    const now = new Date()
    const entry_time = now // new Date(now.setHours(now.getHours() - 5))

    const endpoint = `${BASE_URL}/webhook`
    const entryPayload = JSON.stringify({
        license_plate,
        entry_time,
        event_type: 'ENTRY'
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Registrar entrada de veículo
    const response = http.post(endpoint, entryPayload, params);

    checkEventResponse(response);

    return {license_plate}
}
