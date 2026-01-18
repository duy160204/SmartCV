<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { usePlanStore } from '@/stores/plan.store';

const router = useRouter();
const store = usePlanStore();

const formData = ref({
    code: '',
    name: '',
    price: 0,
    durationMonths: 1,
    planType: 'PRO',
    maxSharePerMonth: 10,
    publicLinkExpireDays: 30,
    description: ''
});

const isSubmitting = ref(false);
const errorMessage = ref('');

const handleSubmit = async () => {
    isSubmitting.value = true;
    errorMessage.value = '';
    try {
        await store.createPlan(formData.value);
        router.push({ name: 'plan-list' });
    } catch (e: any) {
        console.error('[PlanCreatePage] Error:', e);
        if (e.response && e.response.data) {
             const data = e.response.data;
             if (data.details && typeof data.details === 'object') {
                 errorMessage.value = Object.entries(data.details)
                    .map(([field, message]) => `${field}: ${message}`)
                    .join('\n');
             } else if (data.message) {
                 errorMessage.value = data.message;
             } else {
                 errorMessage.value = JSON.stringify(data);
             }
        } else {
            errorMessage.value = e.message || 'Unknown error occurred';
        }
    } finally {
        isSubmitting.value = false;
    }
};

const cancel = () => {
    router.push({ name: 'plan-list' });
};
</script>

<template>
    <div class="p-6 max-w-4xl mx-auto">
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-2xl font-bold">Create New Subscription Plan</h1>
            <button @click="cancel" class="text-gray-600 hover:underline">
                &larr; Back to List
            </button>
        </div>

        <div class="bg-white rounded shadow-lg p-8 border border-gray-200">
            <div v-if="errorMessage" class="mb-4 p-4 bg-red-100 text-red-700 rounded whitespace-pre-line font-medium border border-red-200">
                {{ errorMessage }}
            </div>

            <form @submit.prevent="handleSubmit" class="space-y-6">
                
                <!-- Code & Name -->
                <div class="grid grid-cols-2 gap-6">
                    <div>
                        <label class="block text-sm font-bold mb-2 text-gray-700">Plan Code <span class="text-red-500">*</span></label>
                        <input v-model="formData.code" type="text" 
                               class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                               placeholder="e.g. PRO_MONTHLY" required />
                        <p class="text-xs text-gray-500 mt-1">Unique identifier (internal use)</p>
                    </div>
                    <div>
                        <label class="block text-sm font-bold mb-2 text-gray-700">Display Name <span class="text-red-500">*</span></label>
                        <input v-model="formData.name" type="text" 
                               class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
                               placeholder="e.g. Pro Monthly Plan" required />
                    </div>
                </div>

                <!-- Price & Tier -->
                <div class="grid grid-cols-2 gap-6">
                    <div>
                        <label class="block text-sm font-bold mb-2 text-gray-700">Price (VND) <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.price" type="number" min="0" 
                               class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" required />
                    </div>
                    <div>
                        <label class="block text-sm font-bold mb-2 text-gray-700">Tier / Type <span class="text-red-500">*</span></label>
                        <select v-model="formData.planType" 
                                class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500">
                            <option value="FREE">Free</option>
                            <option value="PRO">Pro</option>
                            <option value="PREMIUM">Premium</option>
                        </select>
                    </div>
                </div>

                <!-- Duration & Limits -->
                <div class="grid grid-cols-3 gap-6">
                    <div>
                        <label class="block text-sm font-bold mb-2 text-gray-700">Duration (Months) <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.durationMonths" type="number" min="1" 
                               class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" required />
                    </div>
                    <div>
                        <label class="block text-sm font-bold mb-2 text-gray-700">Max Share / Month <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.maxSharePerMonth" type="number" min="0" 
                               class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" required />
                    </div>
                    <div>
                        <label class="block text-sm font-bold mb-2 text-gray-700">Link Expiry (Days) <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.publicLinkExpireDays" type="number" min="1" 
                               class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" required />
                    </div>
                </div>

                <!-- Description -->
                <div>
                    <label class="block text-sm font-bold mb-2 text-gray-700">Description</label>
                    <textarea v-model="formData.description" rows="4" 
                              class="w-full border border-gray-300 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500"></textarea>
                </div>

                <!-- Actions -->
                <div class="flex items-center justify-end gap-4 pt-4 border-t border-gray-100">
                    <button type="button" @click="cancel" class="px-6 py-2 border border-gray-300 rounded text-gray-700 hover:bg-gray-50 font-medium">
                        Cancel
                    </button>
                    <button type="submit" :disabled="isSubmitting" 
                            class="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 font-bold shadow-md disabled:bg-blue-300 disabled:cursor-not-allowed">
                        {{ isSubmitting ? 'Creating...' : 'Create Plan' }}
                    </button>
                </div>

            </form>
        </div>
    </div>
</template>
