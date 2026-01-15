<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const status = ref('processing'); // processing, success, failed

onMounted(async () => {
    // VNPay returns: vnp_ResponseCode=00 (Success)
    const code = route.query.vnp_ResponseCode;
    
    if (code === '00') {
        status.value = 'processing';
        // Poll for role update
        let retries = 5;
        const interval = setInterval(async () => {
            await auth.checkAuth();
            // Check if role/subscription updated? 
            // Ideally backend updates immediately on callback, but frontend might lag or cache
            // We just assume success if API call works
            retries--;
            if (retries <= 0) {
                 clearInterval(interval);
                 status.value = 'success';
                 setTimeout(() => router.push('/'), 2000);
            }
        }, 1000);
    } else {
        status.value = 'failed';
    }
});
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
      <div class="text-center p-8 bg-white rounded shadow-lg">
          <div v-if="status === 'processing'" class="animate-pulse">
              <h1 class="text-2xl font-bold mb-2">Processing Payment...</h1>
              <p>Please wait while we confirm your subscription.</p>
          </div>
          
          <div v-if="status === 'success'">
              <h1 class="text-2xl font-bold mb-2 text-green-600">Upgrade Successful!</h1>
              <p>Redirecting to dashboard...</p>
          </div>
          
          <div v-if="status === 'failed'">
              <h1 class="text-2xl font-bold mb-2 text-red-600">Payment Failed</h1>
              <p class="mb-4">Something went wrong. Please try again.</p>
              <router-link to="/" class="bg-gray-800 text-white px-4 py-2 rounded">Go Home</router-link>
          </div>
      </div>
  </div>
</template>
