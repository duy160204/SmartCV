import { defineStore } from 'pinia';
import { ref } from 'vue';
import { adminPlanApi } from '@/api/admin.api';

export interface Plan {
    id: number;
    code: string;
    name: string;
    price: number;
    currency: string;
    durationMonths: number;
    planType: string;
    maxSharePerMonth: number;
    publicLinkExpireDays: number;
    description: string;
    active: boolean; // Note: DTO uses 'active' or 'isActive'? Check backend. Backend DTO has no field for active status in the list view? checked PlanDefinitionDTO -> it does not have isActive field. Wait, PlanService.toDTO does NOT map isActive. Let me check the backend code again.
}

export const usePlanStore = defineStore('plan', () => {
    const plans = ref<Plan[]>([]);
    const loading = ref(false);
    const error = ref<string | null>(null);

    const fetchPlans = async () => {
        loading.value = true;
        error.value = null;
        try {
            const res = await adminPlanApi.getAll();
            plans.value = res.data;
        } catch (err: any) {
            console.error('Failed to fetch plans', err);
            error.value = err.message || 'Failed to fetch plans';
        } finally {
            loading.value = false;
        }
    };

    const createPlan = async (data: any) => {
        console.log('[PlanStore] createPlan called with:', data);
        loading.value = true;
        try {
            console.log('[PlanStore] Calling API...');
            await adminPlanApi.create(data);
            console.log('[PlanStore] API call success, fetching plans...');
            await fetchPlans();
        } catch (err: any) {
            console.error('[PlanStore] createPlan error:', err);
            throw err;
        } finally {
            loading.value = false;
        }
    };

    const updatePlan = async (id: number, data: any) => {
        loading.value = true;
        try {
            await adminPlanApi.update(id, data);
            await fetchPlans();
        } catch (err: any) {
            throw err;
        } finally {
            loading.value = false;
        }
    };

    const toggleStatus = async (id: number) => {
        loading.value = true;
        try {
            await adminPlanApi.toggleStatus(id);
            await fetchPlans();
        } catch (err: any) {
            throw err;
        } finally {
            loading.value = false;
        }
    }

    return {
        plans,
        loading,
        error,
        fetchPlans,
        createPlan,
        updatePlan,
        toggleStatus
    };
});
