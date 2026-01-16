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
