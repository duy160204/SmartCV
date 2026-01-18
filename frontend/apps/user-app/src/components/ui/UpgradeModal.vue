<script setup lang="ts">
import { onMounted } from 'vue';
import { usePlanStore } from '@/stores/plan.store';
import { useAuthStore } from '@/stores/auth';
import { paymentApi } from '@/api/user.api';

const props = defineProps<{
    isOpen: boolean;
}>();

const emit = defineEmits(['close']);

const store = usePlanStore();
const auth = useAuthStore();

onMounted(() => {
    store.fetchPlans();
});

const upgrade = async (planCode: string) => {
    try {
        const res = await paymentApi.create({
            planCode,
            provider: 'VNPAY'
        });
        
        if (res.data.paymentUrl) {
            window.location.href = res.data.paymentUrl;
        } else {
            alert("Failed to initiate payment");
        }
    } catch (e: any) {
        alert("Payment Error: " + (e.response?.data || e.message));
    }
};

const formatCurrency = (val: number, currency: string) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: currency }).format(val);
};
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded p-8 w-[600px] shadow-xl text-center max-h-[90vh] overflow-y-auto">
          <h2 class="text-2xl font-bold mb-2">Upgrade Your Plan</h2>
          <p class="text-gray-600 mb-8">Choose the best plan for your career goals.</p>
          
          <div v-if="store.loading" class="py-8">Loading plans...</div>
          <div v-else-if="store.error" class="text-red-500 py-4">{{ store.error }}</div>
          
          <div v-else class="space-y-4">
              <div 
                v-for="plan in store.plans" 
                :key="plan.code"
                class="border p-4 rounded transition-colors relative"
                :class="[
                    auth.user?.plan === plan.planType 
                        ? 'border-green-500 bg-green-50 cursor-default' 
                        : 'hover:border-blue-500 cursor-pointer bg-white',
                    plan.planType === 'PREMIUM' && auth.user?.plan !== 'PREMIUM' ? 'bg-blue-50 border-blue-200' : ''
                ]"
                @click="auth.user?.plan !== plan.planType && upgrade(plan.code)"
              >
                  <div class="flex justify-between items-center mb-2">
                       <h3 class="font-bold text-lg">{{ plan.name }}</h3>
                       <span v-if="auth.user?.plan === plan.planType" class="text-xs text-white bg-green-500 px-2 py-1 rounded font-bold">CURRENT PLAN</span>
                       <span v-else-if="plan.description" class="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded">{{ plan.description }}</span>
                  </div>
                  
                  <div class="text-left">
                       <p class="text-2xl font-bold text-blue-600" :class="{'text-green-700': auth.user?.plan === plan.planType}">
                           {{ formatCurrency(plan.price, plan.currency) }} 
                           <span class="text-sm font-normal" :class="auth.user?.plan === plan.planType ? 'text-green-600' : 'text-gray-500'">/ {{ plan.durationMonths }} mo</span>
                       </p>
                       <p class="text-sm text-gray-500 mt-1">Duration: {{ plan.durationMonths * 30 }} days</p>
                  </div>
              </div>
          </div>
          
          <button @click="$emit('close')" class="mt-8 text-gray-400 hover:text-gray-600 underline">No thanks</button>
      </div>
  </div>
</template>
