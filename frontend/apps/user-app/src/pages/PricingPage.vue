<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useUserPlanStore } from '@/stores/user-plan.store';
import { onMounted } from 'vue';

const auth = useAuthStore();
const planStore = useUserPlanStore();

onMounted(() => {
    planStore.fetchPlans();
});

const handlePlanAction = (plan: any) => {
    if (!auth.isAuthenticated) {
        // Prompt login if guest
        // Ideally emit an event or use a global bus to open AuthModal
        // For now, redirect to register logic or use specific query param to trigger modal on Landing
        // But since we have a modal, maybe we can inject 'openAuth' if provided or just alert/redirect
        // Let's rely on a helper or redirect to login page if modal isn't globally available here easily without props props. 
        // Actually, Navbar handles modal. PricingPage is a route.
        // We can just redirect to /register
        window.location.href = '/register'; 
        return;
    }

    // User is logged in -> Upgrade Flow
    // Since payment integration is "User -> Upgrade plan", likely involves selecting plan
    alert(`Upgrade to ${plan.name} coming soon!`);
    // Real implementation would be: router.push(`/checkout/${plan.code}`) or similar
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
                             <li class="flex items-center gap-3 text-sm text-gray-700">
                                <span class="text-green-500 font-bold">✓</span>
                                <span>{{ plan.maxSharePerMonth === -1 ? 'Unlimited' : plan.maxSharePerMonth }} Public Shares</span>
                            </li>
                             <li class="flex items-center gap-3 text-sm text-gray-700">
                                <span class="text-green-500 font-bold">✓</span>
                                <span>{{ plan.publicLinkExpireDays }} Days Link Validity</span>
                            </li>
                            <li class="flex items-center gap-3 text-sm text-gray-700">
                                <span class="text-green-500 font-bold">✓</span>
                                <span>Professional Templates</span> // Hardcoded feature example
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
