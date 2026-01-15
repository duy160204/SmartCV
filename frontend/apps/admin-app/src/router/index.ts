import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const Dashboard = () => import('../pages/DashboardPage.vue')
const Login = () => import('../pages/LoginPage.vue')
const TemplateEditor = () => import('../pages/TemplateEditorPage.vue')
const TemplateList = () => import('../pages/TemplateListPage.vue')
const UserList = () => import('../pages/UserListPage.vue')
const CVList = () => import('../pages/CVListPage.vue')

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
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
            path: '/templates',
            name: 'template-list',
            component: TemplateList,
            meta: { requiresAuth: true }
        },
        {
            path: '/templates/:id',
            name: 'template-editor',
            component: TemplateEditor,
            meta: { requiresAuth: true }
        }
    ]
})

router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore();

    if (authStore.isLoading) {
        await authStore.checkAuth();
    }

    // Auth Check
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        return next({ name: 'login' });
    }

    // Guest Check
    if (to.meta.guestOnly && authStore.isAuthenticated) {
        return next({ name: 'dashboard' });
    }

    // Role Check for Admin App (Double check store logic too)
    if (authStore.isAuthenticated && authStore.user?.role !== 'ADMIN') {
        // Log them out or show error?
        // Let's redirect to login for now, store.checkAuth handles strict logic by throwing error ideally
        await authStore.logout();
        return next({ name: 'login' });
    }

    next();
});

export default router
