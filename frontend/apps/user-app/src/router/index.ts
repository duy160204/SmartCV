import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// Pages (Lazy load)
const LandingPage = () => import('../pages/LandingPage.vue')
const Dashboard = () => import('../pages/DashboardPage.vue')
const Login = () => import('../pages/LoginPage.vue')
const Register = () => import('../pages/RegisterPage.vue')
const ForgotPassword = () => import('../pages/ForgotPasswordPage.vue')
const CreateCV = () => import('../pages/CreateCVPage.vue')
const CVEditor = () => import('../pages/CVEditorPage.vue')
const OAuthCallback = () => import('../pages/OAuthCallbackPage.vue')
const PaymentReturn = () => import('../pages/PaymentReturnPage.vue')
const Settings = () => import('../pages/SettingsPage.vue')

const router = createRouter({
    history: createWebHistory((import.meta as any).env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'landing',
            component: LandingPage,
            meta: { guestOnly: true }
        },
        {
            path: '/dashboard',
            name: 'dashboard',
            component: Dashboard,
            meta: { requiresAuth: true }
        },
        {
            path: '/login',
            name: 'login',
            component: Login,
            meta: { guestOnly: true }
        },
        {
            path: '/register',
            name: 'register',
            component: Register,
            meta: { guestOnly: true }
        },
        {
            path: '/forgot-password',
            name: 'forgot-password',
            component: ForgotPassword,
            meta: { guestOnly: true }
        },
        // OAuth callback routes - MUST bypass auth guard
        {
            path: '/oauth/callback/:provider',
            name: 'oauth-callback',
            component: OAuthCallback,
            meta: { bypassAuth: true }  // ← CRITICAL: Bypass auth check
        },
        {
            path: '/auth/callback/:provider',
            name: 'auth-callback-legacy',
            component: OAuthCallback,
            meta: { bypassAuth: true }  // ← CRITICAL: Bypass auth check
        },
        {
            path: '/cv/create',
            name: 'create-cv',
            component: CreateCV,
            meta: { requiresAuth: true }
        },
        {
            path: '/cv/editor/:id',
            name: 'cv-editor',
            component: CVEditor,
            meta: { requiresAuth: true }
        },
        {
            path: '/payment/return',
            name: 'payment-return',
            component: PaymentReturn,
            meta: { requiresAuth: true }
        },
        {
            path: '/settings',
            name: 'settings',
            component: Settings,
            meta: { requiresAuth: true }
        }
    ]
})

router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();

    // CRITICAL: Bypass auth check for OAuth callback routes
    // These routes handle auth themselves and must not be interrupted
    if (to.meta.bypassAuth) {
        return next();
    }

    // Check auth status if not already checked (e.g., initial load)
    if (authStore.isLoading) {
        try {
            await authStore.checkAuth();
        } catch (e) {
            // ignore error
        }
    }

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        return next('/login');
    }

    if (to.meta.guestOnly && authStore.isAuthenticated) {
        return next('/dashboard');
    }

    next();
});

export default router
