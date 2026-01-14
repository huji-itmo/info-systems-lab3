import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// Custom metrics for cache performance
const cacheHitRate = new Trend('cache_hit_rate', true);
const dbQueryTime = new Trend('db_query_time_ms', true);
const successRate = new Rate('import_success_rate');

// Configuration options
export let options = {
    scenarios: {
        cache_hits: {
            executor: 'constant-vus',
            vus: 20,
            duration: '1m',
            exec: 'testCacheHits',
            tags: { scenario: 'cache_hits' },
        }
    },
};

// Base URL - MAKE SURE THIS MATCHES YOUR ENVIRONMENT
const BASE_URL = __ENV.BASE_URL || 'http://localhost/api';
// Use this if running in Docker Compose: 'http://spring-service:8080/api'
// Use this for local testing: 'http://localhost:8080/api'

// ID ranges (1-1000 as specified)
const MIN_ID = 1;
const MAX_ID = 1000;

// Weapon types and categories
const WEAPON_TYPES = ['BOLTGUN', 'HEAVY_BOLTGUN', 'FLAMER', 'HEAVY_FLAMER', 'MULTI_MELTA'];
const CATEGORIES = ['AGGRESSOR', 'INCEPTOR', 'TACTICAL', 'CHAPLAIN', 'APOTHECARY'];

// Pre-defined IDs for cache hit testing
const hotChapterIds = [1, 42, 150, 287, 512, 733, 999];
const hotCoordinatesIds = [2, 87, 211, 345, 624, 888, 1000];

// Function to create a Space Marine object
function createSpaceMarine(useHotIds = false, index = 0) {
    let chapterId, coordinatesId;

    if (useHotIds) {
        chapterId = hotChapterIds[index % hotChapterIds.length];
        coordinatesId = hotCoordinatesIds[index % hotCoordinatesIds.length];
    } else {
        chapterId = randomIntBetween(MIN_ID, MAX_ID);
        coordinatesId = randomIntBetween(MIN_ID, MAX_ID);
    }

    return {
        name: `Marine_${Date.now()}_${__VU}_${index}`,
        chapterId: chapterId,
        coordinatesId: coordinatesId,
        health: randomIntBetween(50, 150),
        loyal: Math.random() > 0.5,
        category: CATEGORIES[randomIntBetween(0, CATEGORIES.length - 1)],
        weaponType: WEAPON_TYPES[randomIntBetween(0, WEAPON_TYPES.length - 1)]
    };
}

function performImport(fileContent, fileName, scenarioTag) {
    const payload = {
        file: http.file(fileContent, fileName, 'application/json'),
    };

    const params = {
        // DO NOT SET Content-Type - k6 auto-generates boundary
        tags: { scenario: scenarioTag },
    };

    return http.post(`${BASE_URL}/space-marines/import`, payload, params);
}

// Scenario 1: Test with frequently used IDs (cache hits)
export function testCacheHits() {
    group('Cache Hit Testing', function () {
        const marines = [];
        for (let i = 0; i < 15; i++) {
            marines.push(createSpaceMarine(true, i));
        }

        const fileContent = JSON.stringify(marines);
        const fileName = `cache_hit_${Date.now()}_${__VU}.json`;
        const response = performImport(fileContent, fileName, 'cache_hits');

        check(response, {
            'Status is 201': (r) => r.status === 201,
            'Has valid response': (r) => {
                if (r.status !== 201) return false;
                try {
                    const body = JSON.parse(r.body);
                    return body.total > 0 && body.successful >= 0;
                } catch (e) {
                    console.error(`Response parsing error: ${e.message}, Response body: ${r.body}`);
                    return false;
                }
            },
        });

        sleep(randomIntBetween(0.5, 1.5));
    });
}

// // Scenario 2: Test with random IDs (cache misses)
// export function testCacheMisses() {
//     group('Cache Miss Testing', function () {
//         const marines = [];
//         for (let i = 0; i < 15; i++) {
//             marines.push(createSpaceMarine(false, i));
//         }

//         const fileContent = JSON.stringify(marines);
//         const fileName = `cache_miss_${Date.now()}_${__VU}.json`;
//         const response = performImport(fileContent, fileName, 'cache_misses');

//         check(response, {
//             'Status is 201': (r) => r.status === 201,
//         });

//         sleep(randomIntBetween(0.5, 1.5));
//     });
// }

// // Scenario 3: Mixed pattern
// export function testMixedPattern() {
//     group('Mixed Pattern Testing', function () {
//         const marines = [];

//         // First half uses hot IDs
//         for (let i = 0; i < 7; i++) {
//             marines.push(createSpaceMarine(true, i));
//         }

//         // Second half uses random IDs
//         for (let i = 7; i < 15; i++) {
//             marines.push(createSpaceMarine(false, i));
//         }

//         const fileContent = JSON.stringify(marines);
//         const fileName = `mixed_${Date.now()}_${__VU}.json`;
//         const response = performImport(fileContent, fileName, 'mixed_workload');

//         check(response, {
//             'Status is 201': (r) => r.status === 201,
//         });

//         sleep(randomIntBetween(0.5, 1.5));
//     });
// }

// Setup function
export function setup() {
    console.log(`Testing against: ${BASE_URL}`);

    // Health check with retry logic
    let attempts = 0;
    const maxAttempts = 5;
    let response;

    while (attempts < maxAttempts) {
        response = http.get(`${BASE_URL}/space-marines?page=0&size=1`);
        if (response.status === 200) {
            console.log('API health check passed');
            return {};
        }
        attempts++;
        console.warn(`Health check attempt ${attempts} failed. Status: ${response.status}`);
        sleep(2);
    }

    console.error(`API health check failed after ${maxAttempts} attempts! Status: ${response.status}`);
    console.error(`Response: ${response.body}`);
    throw new Error('API endpoint not accessible');
}
