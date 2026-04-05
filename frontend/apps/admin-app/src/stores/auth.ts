import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
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
    const token = ref<string | null>(localStorage.getItem('accessToken'));
    const authInitialized = ref(false);
    const logoutExecuted = ref(false);
    const isLoading = ref(false);

    const isAuthenticated = computed(() => {
        if (!token.value) return false;
        try {
            const payload = JSON.parse(atob(token.value.split('.')[1]));
            return payload.exp * 1000 > Date.now();
        } catch {
            return false;
        }
    });

    function decodeJwtFallback(jwt: string) {
        try {
            const payload = JSON.parse(atob(jwt.split('.')[1]));
            user.value = {
                id: payload.id,
                email: payload.sub || payload.email,
                name: payload.name,
                role: payload.role
            };
            return true;
        } catch (error) {
            console.error('[AuthStore] JWT decode failed', error);
            return false;
        }
    }

    function clearAuth() {
        console.log('[AuthStore] Atomic Reset Executed');
        user.value = null;
        token.value = null;
        authInitialized.value = false;
        logoutExecuted.value = false;
        
        localStorage.clear();
        sessionStorage.clear();
    }

    /**
     * READ FROM STORAGE ONLY. No APIs.
     * Must run exactly once during app bootstrap.
     */
    function hydrateAuth() {
        if (authInitialized.value) return; 
        
        const storedToken = localStorage.getItem('accessToken');
        token.value = storedToken || null;
        
        authInitialized.value = true;
        console.log('[AuthStore] Hydrated from storage');
    }

    /**
     * Guarded Cache Layer for /users/me
     * Called only after login or first load if token exists.
     */
    async function fetchUser() {
        if (!token.value || user.value) return; // Cache check
        
        try {
            isLoading.value = true;
            const res = await api.get('/users/me');
            user.value = res.data;
        } catch (e) {
            console.error('[AuthStore] fetchUser failed', e);
            throw e;
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
            if (res.data?.accessToken) {
                localStorage.setItem('accessToken', res.data.accessToken);
                token.value = res.data.accessToken;
            }
            // NON-BLOCKING fetchUser
            try {
                await fetchUser();
            } catch (e) {
                console.warn('[AuthStore] fetchUser failed, attempting JWT local decode fallback');
                const success = decodeJwtFallback(res.data.accessToken);
                if (!success) {
                    throw new Error('Invalid JWT provided, login aborted.');
                }
            }
            
            if (!user.value) {
                clearAuth();
                window.location.replace('/login');
                return;
            }
        } catch (error: any) {
            clearAuth();
            window.location.replace('/login');
            throw error;
        }
    }

    async function logout() {
        if (logoutExecuted.value) return;
        logoutExecuted.value = true;
        
        console.log('[FE][LOGOUT] ATOMIC TRIGGER');
        try {
            await authApi.logout();
        } catch (e) {
            console.warn('[FE][LOGOUT] Backend call failed or already logged out', e);
        } finally {
            clearAuth();
            window.location.replace('/login');
        }
    }

    return {
        user,
        token,
        isAuthenticated,
        authInitialized,
        logoutExecuted,
        isLoading,
        clearAuth,
        hydrateAuth,
        fetchUser,
        login,
        logout
    };
});
