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
                },
                {
                    path: 'plans',
                    name: 'plan-list',
                    component: () => import('../pages/PlanListPage.vue')
                },
                {
                    path: 'plans/create',
                    name: 'plan-create',
                    component: () => import('../pages/PlanCreatePage.vue')
                }
            ]
        }
    ]
})

let navigating = false;

router.beforeEach((to, from, next) => {
    // 1. Prevent Double Navigation
    if (to.path === from.path && to.path !== '/') {
        return next(false);
    }

    const rawToken = localStorage.getItem('accessToken');

    // 2. Guest-only check (e.g. /login)
    if (to.meta.guestOnly) {
        if (rawToken) {
            return next({ name: 'dashboard' });
        }
        return next();
    }

    // 3. Protected route check
    if (to.meta.requiresAuth && !rawToken) {
        return next({ name: 'login' });
    }

    // 4. Default Allow
    next();
});

export default router
