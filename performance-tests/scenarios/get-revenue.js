import http from 'k6/http';
import {sleep, check} from 'k6';
import {BASE_URL, getSpot, getThinkTime} from '../config.js';

export default function (license_plate) {
    const spot = getSpot()
    const payload = JSON.stringify({
        date: formatDate(Date()),
        sector: spot.sector
    })
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    // Consultar status do estacionamento
    const response = http.get(`${BASE_URL}/revenue`, payload, params);

    check(response, {
        'status is 200': (r) => r.status === 200,
        'has occupancy data': (r) => r.json('price_until_now') !== undefined,
        'Response contains expected data': (r) => r.json().key === 'internalCode'
    });

    // Tempo de espera entre requisições
    sleep(getThinkTime());
}

function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Mês começa em 0
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
}
