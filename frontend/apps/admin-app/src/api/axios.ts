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

api.interceptors.request.use(
    async (config) => {
        const isAuthRequest = config.url?.includes('/auth/') || config.url?.includes('/login') || config.url?.includes('/refresh') || config.url?.includes('/users/me');

        const { useAuthStore } = await import('../stores/auth');
        const authStore = useAuthStore();
        
        if (!authStore.authInitialized && !isAuthRequest) {
            await authStore.hydrateAuth();
        }

        const token = localStorage.getItem('accessToken');

        if (token) {
            config.headers = {
                ...config.headers,
                Authorization: `Bearer ${token}`
            } as any;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    res => res,
    err => {
        const status = err?.response?.status;

        if (status === 401) {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('token');
            window.location.href = '/login';
        }

        if (status === 403) {
            console.warn('Permission denied');
        }

        return Promise.reject(err);
    }
);

export default api;
