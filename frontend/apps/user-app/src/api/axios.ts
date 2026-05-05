import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    withCredentials: true, // Crucial for HttpOnly cookies and CSRF
    xsrfCookieName: 'XSRF-TOKEN',
    xsrfHeaderName: 'X-XSRF-TOKEN',
    headers: {
        'Content-Type': 'application/json'
    }
});

// Pages that should NOT trigger 401 redirect (they handle auth themselves)
const AUTH_EXEMPT_PATHS = [
    '/login',
    '/register',
    '/forgot-password',
    '/oauth/callback',
    '/auth/callback',
    '/payment/return'
];

// Request interceptor
api.interceptors.request.use(
    (config) => {
        // DO NOT rely on localStorage token
        // DO NOT send Authorization header if using cookies

        // i18n: Send current locale to backend for localized responses
        const locale = localStorage.getItem('smartcv_locale') || 'en';
        config.headers['Accept-Language'] = locale;

        return config;
    },
    (error) => Promise.reject(error)
);

export const fetchUser = async (): Promise<any> => {
    const response = await api.get('/users/me');
    return response.data;
};

// Response interceptor
api.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            const currentPath = window.location.pathname;

            // Check if current page is exempt from 401 redirect
            const isExempt = AUTH_EXEMPT_PATHS.some(path => currentPath.startsWith(path));

            if (!isExempt) {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                import('../router').then((routerModule) => {
                    const router = routerModule.default;
                    router.replace('/login');
                });
            }
        }
        if (error.response?.status === 429 || error.response?.data?.message === 'AI_DAILY_LIMIT_EXCEEDED') {
            import('../stores/user-plan.store').then(m => {
                const planStore = m.useUserPlanStore();
                planStore.isLimitModalOpen = true;
            });
        }
        return Promise.reject(error);
    }
);

export default api;
