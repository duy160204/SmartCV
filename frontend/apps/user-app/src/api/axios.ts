import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    withCredentials: true, // Crucial for HttpOnly cookies
    headers: {
        'Content-Type': 'application/json'
    }
});

// Pages that should NOT trigger 401 redirect (they handle auth themselves)
const AUTH_EXEMPT_PATHS = [
    '/login',
    '/register',
    '/forgot-password',
    '/oauth/callback',
    '/auth/callback',
    '/payment/return'
];

// Request interceptor
api.interceptors.request.use(
    (config) => {
        // DO NOT rely on localStorage token
        // DO NOT send Authorization header if using cookies
        return config;
    },
    (error) => Promise.reject(error)
);

export const waitForAuth = async (maxRetries = 10, delayMs = 300): Promise<any> => {
    for (let i = 0; i < maxRetries; i++) {
        try {
            const response = await api.get('/users/me');
            return response.data;
        } catch (error) {
            if (i === maxRetries - 1) throw error;
            await new Promise(resolve => setTimeout(resolve, delayMs));
        }
    }
};

export const fetchUser = async (): Promise<any> => {
    return await waitForAuth();
};

// Response interceptor
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            const currentPath = window.location.pathname;

            // Check if current page is exempt from 401 redirect
            const isExempt = AUTH_EXEMPT_PATHS.some(path => currentPath.startsWith(path));

            if (!isExempt) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
