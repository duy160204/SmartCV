<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// UI State - MUST always show something
const state = ref<'loading' | 'success' | 'error'>('loading');
const errorMessage = ref('');

onMounted(async () => {
    // Read status from query params
    const status = route.query.status;
    const provider = route.params.provider || 'unknown';
    
    console.log('[OAuth Callback] Provider:', provider, 'Status:', status);
    
    // CASE 1: Status is NOT success -> show error
    if (status !== 'success') {
        state.value = 'error';
        errorMessage.value = (route.query.error as string) || 'OAuth authentication failed';
        return; // Stop here, do NOT redirect automatically
    }
    
    // CASE 2: Status is success -> verify auth and redirect
    try {
        state.value = 'loading';
        
        // Call checkAuth to verify the OAuth cookie was set correctly
        await authStore.checkAuth();
        
        // Check if authentication succeeded
        if (authStore.isAuthenticated && authStore.user) {
            console.log('[OAuth Callback] Auth verified, user:', authStore.user.email);
            state.value = 'success';
            
            // Small delay for user feedback then redirect
            setTimeout(() => {
                router.replace('/dashboard');
            }, 500);
        } else {
            // Auth check passed but no user -> something wrong
            throw new Error('Authentication verified but user data not available');
        }
    } catch (error: any) {
        console.error('[OAuth Callback] Error:', error);
        state.value = 'error';
        errorMessage.value = error.message || 'Failed to complete authentication';
    }
});

const goToLogin = () => {
    router.push('/login');
};

const retry = () => {
    window.location.reload();
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="bg-white p-8 rounded-xl shadow-lg max-w-md w-full text-center">
      
      <!-- LOADING STATE -->
      <div v-if="state === 'loading'">
        <div class="animate-spin w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
        <h2 class="text-xl font-bold text-gray-800 mb-2">Authenticating...</h2>
        <p class="text-gray-500">Please wait while we verify your login.</p>
      </div>
      
      <!-- SUCCESS STATE -->
      <div v-else-if="state === 'success'">
        <div class="text-5xl mb-4">✅</div>
        <h2 class="text-xl font-bold text-green-600 mb-2">Login Successful!</h2>
        <p class="text-gray-500">Redirecting to dashboard...</p>
      </div>
      
      <!-- ERROR STATE -->
      <div v-else-if="state === 'error'">
        <div class="text-5xl mb-4">❌</div>
        <h2 class="text-xl font-bold text-red-600 mb-2">Authentication Failed</h2>
        <p class="text-gray-600 mb-6">{{ errorMessage }}</p>
        <div class="flex gap-3 justify-center">
          <button @click="retry" class="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition">
            Try Again
          </button>
          <button @click="goToLogin" class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">
            Back to Login
          </button>
        </div>
      </div>
      
    </div>
  </div>
</template>

