import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../api/axios';
import axios from 'axios';

const authApi = axios.create({
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true
});

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
            // Use /users/me to verify session and role
            const res = await api.get('/users/me');
            const userData = res.data;

            if (userData.role !== 'ADMIN') {
                throw new Error("Not authorized");
            }

            user.value = userData;
            isAuthenticated.value = true;
        } catch (error) {
            user.value = null;
            isAuthenticated.value = false;
        } finally {
            isLoading.value = false;
        }
    }

    async function login(payload: any) {
        const res = await authApi.post('/auth/login', payload);
        // Login successful, now verify admin access
        await checkAuth();
        if (!isAuthenticated.value) {
            // Logged in but not admin
            await authApi.post('/auth/logout', null, { params: { refreshToken: 'cookie' } });
            throw new Error("Access Denied: Admins only");
        }
    }

    async function logout() {
        try {
            await authApi.post('/auth/logout', null, { params: { refreshToken: 'cookie' } });
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
