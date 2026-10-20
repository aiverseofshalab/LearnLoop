// LearnLoop REST API Client
export function getApiBase() {
    if (typeof window !== 'undefined') {
        if (window.LEARNLOOP_API_BASE && window.LEARNLOOP_API_BASE.trim() !== '') {
            return window.LEARNLOOP_API_BASE.replace(/\/+$/, '');
        }
        const customBase = localStorage.getItem('learnloop_api_base');
        if (customBase && customBase.trim() !== '') {
            return customBase.replace(/\/+$/, '');
        }
    }
    return '';
}

export class ApiClient {
    static getToken() {
        return localStorage.getItem('learnloop_token');
    }

    static setToken(token) {
        if (token) {
            localStorage.setItem('learnloop_token', token);
        } else {
            localStorage.removeItem('learnloop_token');
        }
    }

    static generateCorrelationId() {
        return 'req-' + Math.random().toString(36).substring(2, 11) + '-' + Date.now();
    }

    static async request(endpoint, options = {}) {
        const base = getApiBase();
        const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
        const url = !base 
            ? `/api/v1${cleanEndpoint}`
            : (base.endsWith('/api/v1') ? `${base}${cleanEndpoint}` : `${base}/api/v1${cleanEndpoint}`);
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'X-Correlation-ID': ApiClient.generateCorrelationId(),
            ...(options.headers || {})
        };

        const token = ApiClient.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        if (config.body && typeof config.body === 'object') {
            config.body = JSON.stringify(config.body);
        }

        try {
            const response = await fetch(url, config);

            // Handle 401 Unauthorized
            if (response.status === 401 && !endpoint.startsWith('/auth/login') && !endpoint.startsWith('/auth/register')) {
                ApiClient.setToken(null);
                window.dispatchEvent(new CustomEvent('learnloop:unauthorized'));
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (data && data.message) || `Request failed with status ${response.status}`;
                const error = new Error(message);
                error.status = response.status;
                error.data = data;
                throw error;
            }

            return data;
        } catch (error) {
            console.error(`API Error on [${options.method || 'GET'} ${endpoint}]:`, error);
            throw error;
        }
    }

    static get(endpoint, params = {}) {
        const query = new URLSearchParams(params).toString();
        const fullEndpoint = query ? `${endpoint}?${query}` : endpoint;
        return ApiClient.request(fullEndpoint, { method: 'GET' });
    }

    static post(endpoint, body = {}) {
        return ApiClient.request(endpoint, { method: 'POST', body });
    }

    static patch(endpoint, body = {}) {
        return ApiClient.request(endpoint, { method: 'PATCH', body });
    }

    static delete(endpoint) {
        return ApiClient.request(endpoint, { method: 'DELETE' });
    }
}
