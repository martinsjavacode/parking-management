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
