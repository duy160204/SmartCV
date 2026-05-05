import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { publicPlanApi, subscriptionApi } from '@/api/user.api';

export interface PlanDefinition {
    id: number;
    code: string;
    name: string;
    price: number;
    currency: string;
    durationMonths: number;
    planType: string;
    description: string;
    maxSharePerMonth: number;
    publicLinkExpireDays: number;
    maxAiRequestsPerDay: number;
    features: string[];
}

export interface MySubscription {
    userId: number;
    role: string;
    plan: {
        code: string;
        name: string;
        maxAiRequestsPerDay: number;
    };
    subscription: {
        status: string;
        startDate: string;
        endDate: string;
    };
    usage: {
        usedToday: number;
        remaining: number;
        resetAt: string;
    };
    limits: {
        isUnlimited: boolean;
        rateLimitPerMinute: number;
    };
    // Flat fields for 4.3 API REQUIREMENT
    planName: string;
    maxAiRequestsPerDay: number;
    usedToday: number;
    remainingToday: number;
}

export const useUserPlanStore = defineStore('user-plan', () => {
    const plans = ref<PlanDefinition[]>([]);
    const currentSubscription = ref<MySubscription | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);
    const isLimitModalOpen = ref(false);

    const isUnlimited = computed(() => currentSubscription.value?.limits.isUnlimited || false);
    
    const usagePercentage = computed(() => {
        if (!currentSubscription.value || isUnlimited.value) return 0;
        const used = currentSubscription.value.usage.usedToday;
        const total = currentSubscription.value.plan.maxAiRequestsPerDay;
        if (total === 0) return 100;
        return Math.min(100, (used / total) * 100);
    });

    async function fetchPlans() {
        try {
            isLoading.value = true;
            const res = await publicPlanApi.getAll();
            plans.value = res.data;
        } catch (e: any) {
            console.error("Failed to fetch plans", e);
            error.value = "Failed to load plans";
        } finally {
            isLoading.value = false;
        }
    }

    async function fetchSubscription() {
        try {
            isLoading.value = true;
            const res = await subscriptionApi.getMySubscription();
            // Backend returns: { message: "...", data: { ...MySubscriptionDTO } }
            currentSubscription.value = res.data.data;
        } catch (e: any) {
            console.error("Failed to fetch subscription", e);
        } finally {
            isLoading.value = false;
        }
    }

    async function upgradePlan(planCode: string, provider: string = 'VNPAY') {
        try {
            isLoading.value = true;
            error.value = null;

            const res = await import('@/api/user.api').then(m => m.paymentApi.create({
                planCode: planCode,
                provider: provider
            }));

            const { paymentUrl, clientSecret, provider: resProvider } = res.data;
            
            if (resProvider === 'VNPAY' && paymentUrl) {
                window.location.href = paymentUrl;
            } else if (resProvider === 'STRIPE' && clientSecret) {
                return clientSecret;
            } else {
                throw new Error("Invalid payment gateway response");
            }

        } catch (e: any) {
            console.error("Payment creation failed", e);
            error.value = e.response?.data?.message || e.message || "Failed to initiate payment";
            throw e;
        } finally {
            isLoading.value = false;
        }
    }

    async function init() {
        await Promise.all([fetchPlans(), fetchSubscription()]);
    }

    return {
        plans,
        currentSubscription,
        isUnlimited,
        usagePercentage,
        isLoading,
        error,
        isLimitModalOpen,
        fetchPlans,
        fetchSubscription,
        upgradePlan,
        init
    };
});
