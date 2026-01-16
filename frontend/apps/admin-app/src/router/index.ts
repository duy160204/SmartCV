import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// Layout
const AdminLayout = () => import('../components/layouts/AdminLayout.vue')

// Pages (Lazy load)
const Dashboard = () => import('../pages/DashboardPage.vue')
const Login = () => import('../pages/LoginPage.vue')
const TemplateEditor = () => import('../pages/TemplateEditorPage.vue')
const TemplateList = () => import('../pages/TemplateListPage.vue')
const UserList = () => import('../pages/UserListPage.vue')
const CVList = () => import('../pages/CVListPage.vue')
const PaymentList = () => import('../pages/PaymentListPage.vue')
const SubscriptionManagement = () => import('../pages/SubscriptionManagementPage.vue')

const router = createRouter({
    history: createWebHistory((import.meta as any).env.BASE_URL),
    routes: [
        // Login page (no layout)
        {
            path: '/login',
            name: 'login',
            component: Login,
            meta: { guestOnly: true }
        },
        // Protected routes with AdminLayout
        {
            path: '/',
            component: AdminLayout,
            meta: { requiresAuth: true },
            children: [
                {
                    path: '',
                    name: 'dashboard',
                    component: Dashboard
                },
                {
                    path: 'users',
                    name: 'user-list',
                    component: UserList
                },
                {
                    path: 'cv',
                    name: 'cv-list',
                    component: CVList
                },
                {
                    path: 'templates',
                    name: 'template-list',
                    component: TemplateList
                },
                {
                    path: 'templates/:id',
                    name: 'template-editor',
                    component: TemplateEditor
                },
                {
                    path: 'payments',
                    name: 'payment-list',
                    component: PaymentList
                },
                {
                    path: 'subscriptions',
                    name: 'subscription-management',
                    component: SubscriptionManagement
                }
            ]
        }
    ]
})

router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();

    // ALWAYS check auth on initial load, BEFORE any routing decisions
    if (authStore.isLoading) {
        try {
            await authStore.checkAuth();
        } catch (e) {
            console.warn('[Admin Router] Auth check failed:', e);
            // Continue with isAuthenticated = false
        }
    }

    // Now handle routing based on auth state

    // Guest-only pages (login)
    if (to.meta.guestOnly) {
        if (authStore.isAuthenticated) {
            // Already logged in, redirect to dashboard
            return next({ name: 'dashboard' });
        }
        return next();
    }

    // Protected routes - check auth
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        return next({ name: 'login' });
    }

    // ADMIN Role Check (case-insensitive)
    if (authStore.isAuthenticated) {
        const userRole = authStore.user?.role?.toLowerCase();
        if (userRole !== 'admin') {
            console.warn('[Admin] Non-admin user attempted access:', userRole);
            await authStore.logout();
            return next({ name: 'login' });
        }
    }

    next();
});

export default router
