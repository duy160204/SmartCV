import api from './axios';

export const authApi = {
    logout(): Promise<void> {
        console.log('[FE][API] LOGOUT CALLED');
        return api.post('/auth/logout');
    },

    login(payload: any): Promise<any> {
        return api.post('/auth/login', payload);
    }
};
