<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useUserPlanStore } from '@/stores/user-plan.store';
import { onMounted } from 'vue';

import { useRouter } from 'vue-router';

// Make sure to add this inside setup
const auth = useAuthStore();
const planStore = useUserPlanStore();
const router = useRouter();

onMounted(() => {
    planStore.fetchPlans();
});

const handlePlanAction = async (plan: any) => {
    if (!auth.isAuthenticated) {
        window.location.href = '/register';
        return;
    }

    try {
        if (plan.price === 0) {
            // Free plan doesn't need payment selection
            await planStore.upgradePlan(plan.code, 'VNPAY');
        } else {
            router.push(`/payment-method?planId=${plan.code}`);
        }
    } catch (e: any) {
        console.error("Upgrade routing failed", e);
    }
};
</script>

<template>
    <div class="min-h-screen bg-gray-50 font-sans">
        <!-- Hero -->
        <div class="bg-blue-600 text-white py-20 px-6 text-center">
            <h1 class="text-4xl md:text-5xl font-bold mb-6">Simple, Transparent Pricing</h1>
            <p class="text-xl text-blue-100 max-w-2xl mx-auto">
                Invest in your career with professional templates and AI tools.
            </p>
        </div>

        <!-- Plans Grid -->
        <div class="max-w-7xl mx-auto px-6 -mt-10 pb-20">
            <div v-if="planStore.isLoading" class="text-center py-20">
                <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>

            <div v-else class="grid md:grid-cols-3 gap-8">
                <div 
                    v-for="plan in planStore.plans" 
                    :key="plan.code"
                    class="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100 hover:shadow-2xl transition-all duration-300 flex flex-col"
                >
                    <div class="p-8 border-b border-gray-100">
                        <h3 class="text-xl font-bold text-gray-800 mb-2">{{ plan.name }}</h3>
                        <div class="flex items-baseline gap-1">
                            <span class="text-4xl font-extrabold text-blue-600">
                                {{ new Intl.NumberFormat('vi-VN', { style: 'currency', currency: plan.currency }).format(plan.price) }}
                            </span>
                            <span class="text-gray-500">/ {{ plan.durationMonths }} mo</span>
                        </div>
                        <p class="text-gray-500 mt-4 text-sm">{{ plan.description }}</p>
                    </div>

                    <div class="p-8 bg-gray-50 flex-1">
                        <ul class="space-y-4 mb-8">
                            <li 
                                v-for="(feature, index) in plan.features" 
                                :key="index"
                                class="flex items-center gap-3 text-sm text-gray-700"
                            >
                                <span class="text-green-500 font-bold">✓</span>
                                <span>{{ feature }}</span>
                            </li>
                        </ul>
                        
                        <button 
                            @click="handlePlanAction(plan)"
                            class="w-full py-3 rounded-xl font-bold transition transform active:scale-95 shadow-lg"
                            :class="plan.price === 0 ? 'bg-gray-200 text-gray-800 hover:bg-gray-300' : 'bg-blue-600 text-white hover:bg-blue-700'"
                        >
                            {{ auth.isAuthenticated ? 'Upgrade Now' : (plan.price === 0 ? 'Get Started Free' : 'Choose ' + plan.name) }}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
