<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

onMounted(async () => {
    // URL: /auth/callback/:provider?status=success&token=... (if token in param, but we use cookie)
    // Backend OAuth2LoginSuccessHandler redirects to:
    // frontendUrl + "/auth/callback/" + provider + "?status=success" (Cookies set)
    
    // Check status
    const status = route.query.status;
    
    if (status === 'success') {
        try {
            // Verify session by fetching profile
            await authStore.checkAuth();
            if (authStore.isAuthenticated) {
                router.push('/dashboard');
            } else {
                throw new Error("Authentication failed check");
            }
        } catch (e) {
            console.error(e);
            router.push('/login?error=oauth_failed');
        }
    } else {
         router.push('/login?error=' + (route.query.error || 'oauth_failed'));
    }
});
</script>

<template>
  <div class="h-screen flex items-center justify-center">
    <div class="text-center">
        <h2 class="text-xl font-bold mb-2">Authenticating...</h2>
        <p class="text-gray-500">Please wait while we log you in.</p>
    </div>
  </div>
</template>
