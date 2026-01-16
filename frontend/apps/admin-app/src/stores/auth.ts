import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../api/axios';
import { authApi } from '../api/auth.api';

export interface User {
    id: number;
    email: string;
    name: string;
    role: string;
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref<User | null>(null);
    const isAuthenticated = ref(false);
    const isLoading = ref(true);

    async function checkAuth() {
        try {
            isLoading.value = true;
            // GET /api/users/me - api already has baseURL: '/api'
            const res = await api.get('/users/me');
            user.value = res.data;
            isAuthenticated.value = true;
        } catch {
            user.value = null;
            isAuthenticated.value = false;
        } finally {
            isLoading.value = false;
        }
    }

    async function login(payload: any) {
        try {
            // POST /api/auth/login via authApi
            const res = await authApi.login(payload);
            // Store refresh token if returned
            if (res.data?.refreshToken) {
                localStorage.setItem('refreshToken', res.data.refreshToken);
            }
            await checkAuth();
        } catch (error: any) {
            const message = error.response?.data?.message || error.message || 'Login failed';
            throw new Error(message);
        }
    }

    async function logout() {
        console.log('[FE][LOGOUT] CLICKED');
        try {
            console.log('[FE][LOGOUT] CALLING API...');
            await authApi.logout();

            console.log('[FE][LOGOUT] CLEARING AUTH STATE');
            // Clear Pinia State
            user.value = null;
            isAuthenticated.value = false;

            // Clear Storage
            localStorage.clear();
            sessionStorage.clear();

            console.log('[FE][LOGOUT] REDIRECTING TO LOGIN');
            window.location.href = '/login'; // FORCE redirect
        } catch (e) {
            console.error('[FE][LOGOUT] FAILED', e);
            alert('Logout failed. Check console.');
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
