import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';
import { htmlReport } from 'https://raw.githubusercontent.com/masterkikoman/k6-reporter/main/dist/bundle.js';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

const echoData = new SharedArray('echoData', function () {
    return open('./echo.csv')
        .split('\n')
        .slice(1)
        .map(line => {
            let [user_id, name] = line.split(',');
            user_id = user_id ? user_id.trim() : null;
            name = name ? name.trim() : null;
            return { user_id, name };
        });
});

export const options = {
    scenarios: {
        echo: {
            executor: 'per-vu-iterations',
        }
    },
    thresholds: {
        http_req_failed: ['rate<0.0001'],
        http_req_duration: ['p(95)<500', 'p(99)<500'],
    },
    summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
}

export function setup() {
    console.log("Setup Invoked");
    return echoData.map( userData => {
        const token = "test_token";
        return {
            ...userData,
            token
        }
    });
}

export default function (userList) {
    const echoUrl = __ENV.ECHO_URL;
    const index = Math.floor(Math.random() * echoData.length);
    const userData = userList[index];

    const headers = {
        headers: {
            'Content-Type': 'application/json'
        }
    }

    const requestBody = JSON.stringify(userData);

    const response = http.post(echoUrl, requestBody, headers);

    const echo = check(response, {
        'is status 200': (r) => r.status === 200,
        'echo equals to the request': (r) => r.body === requestBody
    });

    if (!echo) {
        console.error(`Failed to echo: ${response.body}`);
    } else {
        console.log(`Echoed: ${response.body}`);
    }
}

export function handleSummary(data) {
    return {
        "userdetails-api-result.html": htmlReport(data),
        stdout: textSummary(data, { indent: " ", enableColors: true }),
    };
}
