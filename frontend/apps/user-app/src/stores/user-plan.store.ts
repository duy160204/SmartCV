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
    features: string[];
}

export interface MySubscription {
    plan: string;
    status: string;
    startDate: string;
    endDate: string;
    cvCount: number;
    maxCVs: number;
}

export const useUserPlanStore = defineStore('user-plan', () => {
    const plans = ref<PlanDefinition[]>([]);
    const currentSubscription = ref<MySubscription | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    const currentPlanDefinition = computed(() => {
        if (!currentSubscription.value || !plans.value.length) return null;
        return plans.value.find(p => p.planType === currentSubscription.value?.plan);
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
            // Backend returns wrapped data? Checking previous view...
            // view_file of SubscriptionController shows: 
            // return ResponseEntity.ok(new SimpleResponseDTO("Subscription info", dto))
            // So data structure is res.data.data
            currentSubscription.value = res.data.data;
        } catch (e: any) {
            console.error("Failed to fetch subscription", e);
            // Non-blocking error, user might just be Guest or Free default
        } finally {
            isLoading.value = false;
        }
    }

    async function upgradePlan(planCode: string) {
        try {
            isLoading.value = true;
            error.value = null;

            // Assume VNPAY for now as per requirements
            // Ideally backend could default provider if not sent, 
            // but api requires it.
            const res = await import('@/api/user.api').then(m => m.paymentApi.create({
                planCode: planCode,
                provider: 'VNPAY'
            }));

            // Response: { paymentUrl: "...", transactionCode: "..." }
            const { paymentUrl } = res.data;
            if (paymentUrl) {
                window.location.href = paymentUrl;
            } else {
                throw new Error("No payment URL received");
            }

        } catch (e: any) {
            console.error("Payment creation failed", e);
            error.value = e.response?.data?.message || e.message || "Failed to initiate payment";
            throw e; // Re-throw for UI to handle
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
        currentPlanDefinition,
        isLoading,
        error,
        fetchPlans,
        fetchSubscription,
        upgradePlan,
        init
    };
});
