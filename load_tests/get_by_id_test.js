import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost/api';
const TOTAL_MARINES = 1000;

export const options = {
    vus: 50,
    duration: '20s',
};

export default function () {
    const id = Math.floor(Math.random() * TOTAL_MARINES) + 1;
    const url = `${BASE_URL}/space-marines/${id}?embed=all`;

    const res = http.get(url);

    check(res, {
        'status 200': (r) => r.status === 200,
        'has embedded chapter': (r) => r.json().chapter !== undefined,
        'has embedded coordinates': (r) => r.json().coordinates !== undefined,
    });

    sleep(Math.random() * 0.1);
}
