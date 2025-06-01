export const BASE_URL = 'http://localhost:8080'; // Ajuste para a URL da sua API

export const THINK_TIME_MIN = 1;
export const THINK_TIME_MAX = 5;

export function getThinkTime() {
    return Math.floor(Math.random() * (THINK_TIME_MAX - THINK_TIME_MIN + 1) + THINK_TIME_MIN);
}

export const LOAD_TEST_VUS = 10;
export const LOAD_TEST_DURATION = '60s';

export const STRESS_TEST_VUS = 30;
export const STRESS_TEST_DURATION = '120s';

export const SPIKE_TEST_VUS = 50;
export const SPIKE_TEST_DURATION = '30s';

// Ajuste para as vagas cadastradas na sua API
export function getSpot() {
    const spots = [
        {
            "id": 1,
            "sector": "A",
            "lat": -23.561684,
            "lng": -46.655981
        },
        {
            "id": 2,
            "sector": "A",
            "lat": -23.561664,
            "lng": -46.655961
        },
        {
            "id": 3,
            "sector": "A",
            "lat": -23.561644,
            "lng": -46.655941
        },
        {
            "id": 4,
            "sector": "A",
            "lat": -23.561624,
            "lng": -46.655921
        },
        {
            "id": 5,
            "sector": "A",
            "lat": -23.561604,
            "lng": -46.655901
        },
        {
            "id": 6,
            "sector": "A",
            "lat": -23.561584,
            "lng": -46.655881
        },
        {
            "id": 7,
            "sector": "A",
            "lat": -23.561564,
            "lng": -46.655861
        },
        {
            "id": 8,
            "sector": "A",
            "lat": -23.561544,
            "lng": -46.655841
        },
        {
            "id": 9,
            "sector": "A",
            "lat": -23.561524,
            "lng": -46.655821
        },
        {
            "id": 10,
            "sector": "A",
            "lat": -23.561504,
            "lng": -46.655801
        },
        {
            "id": 11,
            "sector": "B",
            "lat": -23.561484,
            "lng": -46.655781
        },
        {
            "id": 12,
            "sector": "B",
            "lat": -23.561464,
            "lng": -46.655761
        },
        {
            "id": 13,
            "sector": "B",
            "lat": -23.561444,
            "lng": -46.655741
        },
        {
            "id": 14,
            "sector": "B",
            "lat": -23.561424,
            "lng": -46.655721
        },
        {
            "id": 15,
            "sector": "B",
            "lat": -23.561404,
            "lng": -46.655701
        },
        {
            "id": 16,
            "sector": "B",
            "lat": -23.561384,
            "lng": -46.655681
        },
        {
            "id": 17,
            "sector": "B",
            "lat": -23.561364,
            "lng": -46.655661
        },
        {
            "id": 18,
            "sector": "B",
            "lat": -23.561344,
            "lng": -46.655641
        },
        {
            "id": 19,
            "sector": "B",
            "lat": -23.561324,
            "lng": -46.655621
        },
        {
            "id": 20,
            "sector": "B",
            "lat": -23.561304,
            "lng": -46.655601
        },
        {
            "id": 21,
            "sector": "B",
            "lat": -23.561284,
            "lng": -46.655581
        },
        {
            "id": 22,
            "sector": "B",
            "lat": -23.561264,
            "lng": -46.655561
        },
        {
            "id": 23,
            "sector": "B",
            "lat": -23.561244,
            "lng": -46.655541
        },
        {
            "id": 24,
            "sector": "B",
            "lat": -23.561224,
            "lng": -46.655521
        },
        {
            "id": 25,
            "sector": "B",
            "lat": -23.561204,
            "lng": -46.655501
        },
        {
            "id": 26,
            "sector": "B",
            "lat": -23.561184,
            "lng": -46.655481
        },
        {
            "id": 27,
            "sector": "B",
            "lat": -23.561164,
            "lng": -46.655461
        },
        {
            "id": 28,
            "sector": "B",
            "lat": -23.561144,
            "lng": -46.655441
        },
        {
            "id": 29,
            "sector": "B",
            "lat": -23.561124,
            "lng": -46.655421
        },
        {
            "id": 30,
            "sector": "B",
            "lat": -23.561104,
            "lng": -46.655401
        }
    ]

    return spots[Math.floor(Math.random() * spots.length)]
}
