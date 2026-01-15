import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    withCredentials: true, // Crucial for HttpOnly cookies
    headers: {
        'Content-Type': 'application/json'
    }
});

// Response interceptor
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            // Check if we are already on login to avoid loops
            if (!window.location.pathname.startsWith('/login')) {
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
