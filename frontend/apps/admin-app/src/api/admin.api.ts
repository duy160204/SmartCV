// ===================================================
// admin.api.ts - Admin App API Services
// Full Coverage: All admin endpoints (37 total)
// ===================================================
import api from './axios';

// =========================
// ADMIN DASHBOARD
// =========================
export const dashboardApi = {
    // GET /api/admin/dashboard
    getOverview: () => api.get('/admin/dashboard'),
};

// =========================
// ADMIN USER MANAGEMENT
// =========================
export const adminUserApi = {
    // GET /api/admin/users
    getAll: () => api.get('/admin/users'),

    // GET /api/admin/users/{id}
    getById: (id: number) => api.get(`/admin/users/${id}`),

    // PUT /api/admin/users/{id}/lock
    lock: (id: number) => api.put(`/admin/users/${id}/lock`),

    // PUT /api/admin/users/{id}/unlock
    unlock: (id: number) => api.put(`/admin/users/${id}/unlock`),
};

// =========================
// ADMIN CV MANAGEMENT
// =========================
export const adminCVApi = {
    // GET /api/admin/cv
    getAll: () => api.get('/admin/cv'),

    // GET /api/admin/cv/{id}
    getById: (id: number) => api.get(`/admin/cv/${id}`),

    // POST /api/admin/cv/{id}/lock
    lock: (id: number | string, reason: string) => {
        console.log('[FE][API] LOCK CALLED', id, reason);
        debugger;
        return api.post(`/admin/cv/${id}/lock`, { reason });
    },

    // POST /api/admin/cv/{id}/unlock
    unlock: (id: number | string, reason: string) => {
        console.log('[FE][API] UNLOCK CALLED', id, reason);
        debugger;
        return api.post(`/admin/cv/${id}/unlock`, { reason });
    },

    // DELETE /api/admin/cv/{id}
    delete: (id: number | string, reason: string) => {
        console.log('[FE][API] DELETE CALLED', id, reason);
        debugger;
        return api.delete(`/admin/cv/${id}`, { data: { reason } });
    },
};

// =========================
// ADMIN TEMPLATE MANAGEMENT
// =========================
export const adminTemplateApi = {
    // GET /api/admin/templates
    getAll: () => api.get('/admin/templates'),

    // GET /api/admin/templates/{id}
    getById: (id: number) => api.get(`/admin/templates/${id}`),

    // POST /api/admin/templates
    create: (data: FormData) => api.post('/admin/templates', data),

    // PUT /api/admin/templates/{id}
    update: (id: number, data: FormData) => api.put(`/admin/templates/${id}`, data),

    // PUT /api/admin/templates/{id}/disable
    disable: (id: number) => api.put(`/admin/templates/${id}/disable`),

    // PUT /api/admin/templates/{id}/enable
    enable: (id: number) => api.put(`/admin/templates/${id}/enable`),

    // DELETE /api/admin/templates/{id}
    delete: (id: number) => api.delete(`/admin/templates/${id}`),
};

// =========================
// ADMIN PAYMENT MANAGEMENT
// =========================
export const adminPaymentApi = {
    // GET /api/admin/payments
    getAll: () => api.get('/admin/payments'),

    // GET /api/admin/payments/{id}
    getById: (id: number) => api.get(`/admin/payments/${id}`),

    // GET /api/admin/payments/user/{userId}
    getByUser: (userId: number) => api.get(`/admin/payments/user/${userId}`),

    // GET /api/admin/payments/status/{status}
    getByStatus: (status: string) => api.get(`/admin/payments/status/${status}`),

    // GET /api/admin/payments/provider/{provider}
    getByProvider: (provider: string) => api.get(`/admin/payments/provider/${provider}`),

    // GET /api/admin/payments/date-range
    getByDateRange: (from: string, to: string) =>
        api.get('/admin/payments/date-range', { params: { from, to } }),

    // GET /api/admin/payments/search
    search: (userId: number, from: string, to: string) =>
        api.get('/admin/payments/search', { params: { userId, from, to } }),
};

// =========================
// ADMIN SUBSCRIPTION MANAGEMENT
// =========================
export const adminSubscriptionApi = {
    // POST /api/admin/subscriptions/preview
    preview: (data: { userId: number; newPlan: string }) =>
        api.post('/admin/subscriptions/preview', data),

    // POST /api/admin/subscriptions/confirm
    confirm: (data: { userId: number; newPlan: string; confirm: boolean }) =>
        api.post('/admin/subscriptions/confirm', data),
};

// =========================
// ADMIN SUBSCRIPTION REQUEST MANAGEMENT
// =========================
export const adminSubscriptionRequestApi = {
    // GET /api/admin/subscription-requests
    getAll: () => api.get('/admin/subscription-requests'),

    // GET /api/admin/subscription-requests/status/{status}
    getByStatus: (status: string) =>
        api.get(`/admin/subscription-requests/status/${status}`),

    // POST /api/admin/subscription-requests/{id}/preview
    preview: (id: number) => api.post(`/admin/subscription-requests/${id}/preview`),

    // POST /api/admin/subscription-requests/{id}/confirm
    confirm: (id: number) => api.post(`/admin/subscription-requests/${id}/confirm`),
};
