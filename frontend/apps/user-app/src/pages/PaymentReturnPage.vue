<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const status = ref('processing'); // processing, success, failed

let pollInterval: ReturnType<typeof setInterval> | null = null;

onMounted(async () => {
    // VNPay returns: vnp_ResponseCode=00 (Success)
    const code = route.query.vnp_ResponseCode;
    
    if (code === '00') {
        status.value = 'processing';
        
        let attempts = 0;
        const maxAttempts = 10;
        
        pollInterval = setInterval(async () => {
            attempts++;
            try {
                // Force refresh profile from backend
                await auth.checkAuth();
                
                // Check if user has non-FREE plan or specific target plan
                // For now, simpler check: if plan is NOT FREE, success.
                // Or we can trust checkAuth() returning at all means success if backend handled it.
                // Better: trust that if verifyAuth succeeds, the new data is loaded.
                
                if (auth.user?.plan && auth.user.plan !== 'FREE') {
                     status.value = 'success';
                     if (pollInterval) clearInterval(pollInterval);
                     // Show success message briefly then redirect
                     setTimeout(() => router.push('/'), 3000);
                } else {
                    // Still FREE? keep polling
                    console.log('Still FREE, polling...', attempts);
                }

            } catch (e) {
                console.error('Auth check failed:', e);
            }
            
            if (attempts >= maxAttempts && status.value !== 'success') {
                // If timeout, maybe it worked but just slow? Or actually failed?
                // Just redirect to dashboard to let user see.
                 if (pollInterval) clearInterval(pollInterval);
                 status.value = 'success'; // Assume success and let dashboard show truth
                 setTimeout(() => router.push('/'), 2000);
            }
        }, 1000); // Poll every 1s
    } else {
        status.value = 'failed';
    }
});

onUnmounted(() => {
    if (pollInterval) {
        clearInterval(pollInterval);
        pollInterval = null;
    }
});
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
      <div class="text-center p-8 bg-white rounded shadow-lg">
          <div v-if="status === 'processing'" class="animate-pulse">
              <h1 class="text-2xl font-bold mb-2">{{ t('payment.processing_title') }}</h1>
              <p>{{ t('payment.processing_desc') }}</p>
          </div>
          
          <div v-if="status === 'success'">
              <h1 class="text-2xl font-bold mb-2 text-green-600">{{ t('payment.success_title') }}</h1>
              <p>{{ t('payment.success_redirect') }}</p>
          </div>
          
          <div v-if="status === 'failed'">
              <h1 class="text-2xl font-bold mb-2 text-red-600">{{ t('payment.failed_title') }}</h1>
              <p class="mb-4 text-gray-600">{{ t('payment.failed_desc') }}</p>
              <div class="flex justify-center gap-4">
                  <router-link to="/" class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300">{{ t('common.goHome') }}</router-link>
                  <router-link to="/pricing" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">{{ t('payment.try_again') }}</router-link>
              </div>
          </div>
      </div>
  </div>
</template>
