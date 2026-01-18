import { defineStore } from 'pinia';
import { ref } from 'vue';
import { publicPlanApi } from '@/api/user.api';

export interface PublicPlan {
    id: number;
    code: string;
    name: string;
    price: number;
    currency: string;
    durationMonths: number;
    planType: string; // FREE, PRO, PREMIUM
    maxSharePerMonth: number;
    publicLinkExpireDays: number;
    description: string;
}

export const usePlanStore = defineStore('plan', () => {
    const plans = ref<PublicPlan[]>([]);
    const loading = ref(false);
    const error = ref<string | null>(null);

    const fetchPlans = async () => {
        // Simple cache: if plans exist, don't refetch automatically
        if (plans.value.length > 0) return;

        loading.value = true;
        error.value = null;
        try {
            const res = await publicPlanApi.getAll();
            plans.value = res.data;
        } catch (err: any) {
            console.error('Failed to fetch public plans', err);
            error.value = err.message || 'Failed to load subscription plans';
        } finally {
            loading.value = false;
        }
    };

    return {
        plans,
        loading,
        error,
        fetchPlans
    };
});
