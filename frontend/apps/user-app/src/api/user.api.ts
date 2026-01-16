// ===================================================
// user.api.ts - User App API Services
// Full Coverage: All user-facing endpoints
// ===================================================
import api from './axios';

// =========================
// AUTH ENDPOINTS
// =========================
export const authApi = {
    // POST /auth/register
    register: (data: { name: string; email: string; password: string }) =>
        api.post('/auth/register', data, { baseURL: '' }),

    // GET /auth/verify-email
    verifyEmail: (token: string) =>
        api.get('/auth/verify-email', { params: { token }, baseURL: '' }),

    // POST /auth/login
    login: (data: { email: string; password: string }) =>
        api.post('/auth/login', data, { baseURL: '' }),

    // POST /auth/refresh-token
    refreshToken: (refreshToken: string) =>
        api.post('/auth/refresh-token', { refreshToken }, { baseURL: '' }),

    // POST /auth/logout
    logout: (refreshToken: string) =>
        api.post('/auth/logout', { refreshToken }, { baseURL: '' }),

    // POST /auth/forgot-password
    forgotPassword: (email: string) =>
        api.post('/auth/forgot-password', { email }, { baseURL: '' }),
};

// =========================
// USER ENDPOINTS
// =========================
export const userApi = {
    // GET /api/users/me
    getMe: () => api.get('/users/me'),

    // PUT /api/users/profile
    updateProfile: (data: { name?: string; avatarURL?: string }) =>
        api.put('/users/profile', data),

    // PUT /api/users/password
    changePassword: (data: { currentPassword: string; newPassword: string }) =>
        api.put('/users/password', data),
};

// =========================
// CV ENDPOINTS
// =========================
export const cvApi = {
    // GET /api/cv
    getAll: () => api.get('/cv'),

    // GET /api/cv/{id}
    getById: (id: number) => api.get(`/cv/${id}`),

    // POST /api/cv
    create: (data: { title: string; templateId: number; content: object }) =>
        api.post('/cv', data),

    // PUT /api/cv/{id}
    update: (id: number, data: { title: string; content: object }) =>
        api.put(`/cv/${id}`, data),

    // PATCH /api/cv/{id}/autosave
    autosave: (id: number, content: string) =>
        api.patch(`/cv/${id}/autosave`, { content }),

    // POST /api/cv/{id}/publish
    publish: (id: number) => api.post(`/cv/${id}/publish`),

    // GET /api/cv/{id}/download
    download: (id: number) =>
        api.get(`/cv/${id}/download`, { responseType: 'blob' }),

    // DELETE /api/cv/{id}
    delete: (id: number) => api.delete(`/cv/${id}`),

    // POST /api/cv/template/{id}/favorite
    favoriteTemplate: (templateId: number) =>
        api.post(`/cv/template/${templateId}/favorite`),

    // DELETE /api/cv/template/{id}/favorite
    unfavoriteTemplate: (templateId: number) =>
        api.delete(`/cv/template/${templateId}/favorite`),

    // GET /api/cv/favorites
    getFavorites: () => api.get('/cv/favorites'),

    // GET /api/cv/export/{id}/pdf
    exportPdf: (id: number) =>
        api.get(`/cv/export/${id}/pdf`, { responseType: 'blob' }),
};

// =========================
// TEMPLATE ENDPOINTS
// =========================
export const templateApi = {
    // GET /api/templates
    getAll: () => api.get('/templates'),

    // GET /api/templates/{id}
    getById: (id: number) => api.get(`/templates/${id}`),
};

// =========================
// PAYMENT ENDPOINTS
// =========================
export const paymentApi = {
    // POST /api/payments
    create: (data: { plan: string; months: number; provider: string }) =>
        api.post('/payments', data),
};

// =========================
// SUBSCRIPTION ENDPOINTS
// =========================
export const subscriptionApi = {
    // POST /api/subscription/cv/{id}/public
    publicCV: (cvId: number) => api.post(`/subscription/cv/${cvId}/public`),

    // DELETE /api/subscription/cv/{id}/public
    revokePublicLink: (cvId: number) => api.delete(`/subscription/cv/${cvId}/public`),

    // GET /api/subscription/download/check
    checkDownload: () => api.get('/subscription/download/check'),

    // GET /api/subscription/me
    getMySubscription: () => api.get('/subscription/me'),
};

// =========================
// AI ENDPOINTS
// =========================
export const aiApi = {
    // POST /api/ai/cv/chat
    chat: (cvId: number, message: string) =>
        api.post('/ai/cv/chat', { cvId, message }),
};
