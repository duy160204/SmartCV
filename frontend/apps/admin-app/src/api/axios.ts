import axios from 'axios';

const api = axios.create({
    baseURL: '/api',  // CRITICAL: Must point to API prefix
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Auth endpoints need different base (no /api prefix)
export const authApi = axios.create({
    baseURL: '',  // Auth endpoints are at /auth/*, not /api/auth/*
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

api.interceptors.response.use(
    response => response,
    error => {
        // Handle 401 - redirect to login (but NOT during login itself)
        if (error.response?.status === 401) {
            const currentPath = window.location.pathname;
            // Don't redirect if already on login or during initial auth check
            if (!currentPath.startsWith('/login')) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
