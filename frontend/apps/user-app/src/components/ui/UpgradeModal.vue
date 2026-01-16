<script setup lang="ts">
import api from '@/api/axios';

const props = defineProps<{
    isOpen: boolean;
}>();

const emit = defineEmits(['close']);

const upgrade = async (plan: string, amount: number) => {
    try {
        const res = await api.post('/payments', {
            plan,
            amount
        });
        if (res.data.paymentUrl) {
            window.location.href = res.data.paymentUrl;
        } else {
            alert("Failed to initiate payment");
        }
    } catch (e: any) {
        alert("Payment Error: " + e.message);
    }
};
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded p-8 w-[500px] shadow-xl text-center">
          <h2 class="text-2xl font-bold mb-4">Upgrade to Pro</h2>
          <p class="text-gray-600 mb-8">Unlock premium templates, AI features, and unlimited downloads.</p>
          
          <div class="space-y-4">
              <div class="border p-4 rounded hover:border-blue-500 cursor-pointer" @click="upgrade('PRO_MONTHLY', 50000)">
                  <h3 class="font-bold">Monthly Plan</h3>
                  <p class="text-blue-600 font-bold">50,000 VND / mo</p>
              </div>
               <div class="border p-4 rounded hover:border-blue-500 cursor-pointer bg-blue-50 border-blue-200" @click="upgrade('PRO_YEARLY', 500000)">
                  <h3 class="font-bold">Yearly Plan <span class="text-xs bg-yellow-300 text-yellow-800 px-2 py-[2px] rounded-full">Best Value</span></h3>
                  <p class="text-blue-600 font-bold">500,000 VND / yr</p>
              </div>
          </div>
          
          <button @click="$emit('close')" class="mt-8 text-gray-400 hover:text-gray-600 underline">No thanks</button>
      </div>
  </div>
</template>
