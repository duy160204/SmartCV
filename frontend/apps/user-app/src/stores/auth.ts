import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../api/axios';
import axios from 'axios'; // Import standard axios for /auth endpoints if base api is /api

// Create separate instance for auth endpoints if strict baseURL=/api mapping conflicts
// Or just use relative paths "../auth" or absolute paths.
// Vite proxy maps /auth -> http://localhost:8080/auth
const authApi = axios.create({
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true
});

export interface User {
    id: number;
    email: string;
    name: string;
    avatarURL: string | null;
    role: string;
    isVerified: boolean;
    plan?: string; // e.g. FREE, PRO
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null);
    const isAuthenticated = ref(false);
    const isLoading = ref(true);

    async function checkAuth() {
        try {
            isLoading.value = true;
            // GET /api/users/me
            const res = await api.get('/users/me');
            user.value = res.data;
            isAuthenticated.value = true;
        } catch (error) {
            user.value = null;
            isAuthenticated.value = false;
        } finally {
            isLoading.value = false;
        }
    }

    async function login(payload: any) {
        try {
            // POST /api/auth/login
            const res = await authApi.post('/api/auth/login', payload);
            const data = res.data;

            // STORE REFRESH TOKEN for Logout
            if (data.refreshToken) {
                localStorage.setItem('refreshToken', data.refreshToken);
            }

            await checkAuth();
        } catch (error: any) {
            // Re-throw with user-friendly message for UI to handle
            const message = error.response?.data?.message || error.message || 'Login failed';
            throw new Error(message);
        }
    }

    async function register(payload: any) {
        try {
            // POST /api/auth/register
            const res = await authApi.post('/api/auth/register', payload);
            // After register, we might need to login automatically or show success
            // If backend returns token, we store it. If not, just user data.
            // Assuming simplified flow: register -> success -> auto-login or prompt login.
            // If the API returns same structure as login:
            const data = res.data;
            if (data.refreshToken) {
                localStorage.setItem('refreshToken', data.refreshToken);
            }
            await checkAuth();
        } catch (error: any) {
            const message = error.response?.data?.message || error.message || 'Registration failed';
            throw new Error(message);
        }
    }

    async function logout() {
        try {
            const token = localStorage.getItem('refreshToken');
            if (token) {
                await authApi.post('/api/auth/logout', { refreshToken: token });
                localStorage.removeItem('refreshToken');
            } else {
                console.warn("No refresh token found for logout");
            }
        } catch (e) {
            console.error("Logout failed", e);
        } finally {
            user.value = null;
            isAuthenticated.value = false;
            // Dynamic import to avoid circular dependency
            const { default: router } = await import('../router');
            router.push('/login');
        }
    }

    return {
        user,
        isAuthenticated,
        isLoading,
        checkAuth,
        login,
        register,
        logout
    };
});
