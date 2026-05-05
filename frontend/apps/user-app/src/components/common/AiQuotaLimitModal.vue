<script setup lang="ts">
import { useUserPlanStore } from '@/stores/user-plan.store';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const planStore = useUserPlanStore();
const router = useRouter();

const close = () => {
  planStore.isLimitModalOpen = false;
};

const upgrade = () => {
  close();
  router.push('/pricing');
};
</script>

<template>
  <div v-if="planStore.isLimitModalOpen" class="fixed inset-0 z-[1000] flex items-center justify-center p-4">
    <!-- Backdrop -->
    <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="close"></div>
    
    <!-- Modal -->
    <div class="relative bg-white rounded-3xl shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-300">
      
      <!-- Top Accent -->
      <div class="h-2 bg-gradient-to-r from-blue-600 to-indigo-600"></div>
      
      <div class="p-8 text-center">
        <!-- Icon -->
        <div class="w-20 h-20 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-6 text-4xl">
          🚀
        </div>
        
        <h2 class="text-2xl font-bold text-slate-800 mb-2">
          {{ t('ai.limit_reached_title') || 'Limit Reached' }}
        </h2>
        
        <p class="text-slate-600 mb-8">
          {{ t('ai.limit_reached_desc') || "You've used all your AI credits for today. Upgrade your plan to continue using our advanced AI features immediately." }}
        </p>
        
        <!-- Stats -->
        <div class="bg-slate-50 rounded-2xl p-4 mb-8 flex justify-between items-center text-sm">
          <div class="text-left">
            <span class="block text-slate-400 text-xs font-bold uppercase">{{ t('profile.current_plan') || 'Current Plan' }}</span>
            <span class="font-bold text-slate-700">{{ planStore.currentSubscription?.plan.name }}</span>
          </div>
          <div class="text-right">
            <span class="block text-slate-400 text-xs font-bold uppercase">{{ t('profile.daily_limit') || 'Daily Limit' }}</span>
            <span class="font-bold text-slate-700">{{ planStore.currentSubscription?.plan.maxAiRequestsPerDay }}</span>
          </div>
        </div>
        
        <!-- Actions -->
        <div class="flex flex-col gap-3">
          <button 
            @click="upgrade"
            class="w-full bg-blue-600 text-white py-4 rounded-2xl font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-500/20 active:scale-95"
          >
            {{ t('pricing.upgrade_now') }}
          </button>
          
          <button 
            @click="close"
            class="w-full py-4 text-slate-500 font-medium hover:text-slate-800 transition"
          >
            {{ t('common.maybe_later') || 'Maybe Later' }}
          </button>
        </div>
      </div>
      
      <!-- Footer -->
      <div class="bg-slate-50 p-4 text-center border-t border-slate-100">
        <p class="text-[10px] text-slate-400 font-medium italic">
          {{ t('ai.limit_reset_hint') || 'Quotas reset daily at 00:00 UTC' }}
        </p>
      </div>

    </div>
  </div>
</template>
