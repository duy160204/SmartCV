import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// Pages (Lazy load)
const LandingPage = () => import('../pages/LandingPage.vue')
const Dashboard = () => import('../pages/DashboardPage.vue')
const Login = () => import('../pages/LoginPage.vue')
const CreateCV = () => import('../pages/CreateCVPage.vue')
const CVEditor = () => import('../pages/CVEditorPage.vue')
const OAuthCallback = () => import('../pages/OAuthCallbackPage.vue')
const PaymentReturn = () => import('../pages/PaymentReturnPage.vue')
const Settings = () => import('../pages/SettingsPage.vue')

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
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
            path: '/auth/callback/:provider',
            component: OAuthCallback,
            meta: { guestOnly: true }
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
