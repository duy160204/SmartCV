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
            // POST /auth/login
            const res = await authApi.post('/auth/login', payload);
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

    async function logout() {
        try {
            // POST /auth/logout requires refresh token?
            // User requested logout doesn't usually carry refresh token in body
            // Backend Controller: logout(@RequestParam String refreshToken)
            // Wait, if refreshToken is also HttpOnly cookie (if designed so), we might not have access.
            // But if current implementation expects param, we might fail if we don't have it in storage.
            // Let's check backend implementation..
            // Backend AuthController: logout(@RequestParam String refreshToken)
            // The login response returned `refreshToken` in body?
            // "AuthResponseDTO responseBody = ... getRefreshToken()..." YES.
            // So we should have stored refreshToken in localStorage or memory?
            // Security wise, refresh token in memory is safer.
            // For now, let's assume we might need to send a dummy or if we stored it.
            // Ideally logout should just clear cookies.
            // Ideally logout should just clear cookies.
            const token = localStorage.getItem('refreshToken');
            if (token) {
                await authApi.post('/auth/logout', { refreshToken: token });
                localStorage.removeItem('refreshToken');
            } else {
                // Force logout anyway locally if no token
                console.warn("No refresh token found for logout");
            }
        } catch (e) {
            console.error("Logout failed", e);
        } finally {
            user.value = null;
            isAuthenticated.value = false;
            window.location.href = '/login';
        }
    }

    return {
        user,
        isAuthenticated,
        isLoading,
        checkAuth,
        login,
        logout
    };
});
