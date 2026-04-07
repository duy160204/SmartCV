<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserPlanStore } from '@/stores/user-plan.store';

const route = useRoute();
const router = useRouter();
const planStore = useUserPlanStore();

const planId = ref(route.query.planId as string);
const selectedMethod = ref<'VNPAY' | 'STRIPE' | null>(null);
const isLoading = ref(false);
const error = ref<string | null>(null);

onMounted(() => {
    if (!planId.value) {
        router.push('/pricing');
    }
});

const handlePayment = async () => {
    if (!selectedMethod.value || !planId.value) return;

    isLoading.value = true;
    error.value = null;

    try {
        const clientSecret = await planStore.upgradePlan(planId.value, selectedMethod.value);
        
        if (selectedMethod.value === 'STRIPE' && clientSecret) {
            // Render Stripe Elements via simple redirect URL hook logic or redirect
            // Since this app has no complex Stripe Element form pre-rendered, 
            // the safest bet when clientSecret acts as paymentIntent ID
            // Or redirect user to success checking UI since they will embed Stripe 
            alert('Stripe Initialized! PaymentIntent ClientSecret: ' + clientSecret);
            // Example simulated redirect
            router.push('/payment/return?paymentId=simulated-' + Date.now());
        }
    } catch (e: any) {
       error.value = e.message || 'Payment initiation failed';
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
    <div class="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div class="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg border border-gray-100">
            <div>
                <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
                    Select Payment Method
                </h2>
                <p class="mt-2 text-center text-sm text-gray-600">
                    Plan Code: <span class="font-bold text-blue-600">{{ planId }}</span>
                </p>
            </div>

            <div class="space-y-4">
                <button 
                    @click="selectedMethod = 'VNPAY'"
                    :class="[selectedMethod === 'VNPAY' ? 'border-blue-600 ring-2 ring-blue-600 bg-blue-50' : 'border-gray-200 hover:border-blue-400']"
                    class="w-full flex items-center p-4 border rounded-xl transition duration-200 cursor-pointer text-left"
                >
                    <div class="flex-1">
                        <div class="font-semibold text-gray-900">VNPay (Domestic)</div>
                        <div class="text-sm text-gray-500">Thanh toán qua ví điện tử VNPay hoặc QR.</div>
                    </div>
                </button>

                <button 
                    @click="selectedMethod = 'STRIPE'"
                    :class="[selectedMethod === 'STRIPE' ? 'border-blue-600 ring-2 ring-blue-600 bg-blue-50' : 'border-gray-200 hover:border-blue-400']"
                    class="w-full flex items-center p-4 border rounded-xl transition duration-200 cursor-pointer text-left"
                >
                    <div class="flex-1">
                        <div class="font-semibold text-gray-900">Card (Visa/Mastercard)</div>
                        <div class="text-sm text-gray-500">International payments powered by Stripe.</div>
                    </div>
                </button>
            </div>
            
            <div v-if="error" class="text-red-600 text-sm p-3 bg-red-50 rounded-lg border border-red-100 text-center">
                {{ error }}
            </div>

            <button 
                :disabled="!selectedMethod || isLoading"
                @click="handlePayment"
                class="w-full flex justify-center py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition transform active:scale-95"
            >
                <div v-if="isLoading" class="flex items-center gap-2">
                    <span class="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></span>
                    Processing...
                </div>
                <span v-else>Continue to Payment</span>
            </button>
        </div>
    </div>
</template>
