<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { usePlanStore } from '@/stores/plan.store';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
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
            errorMessage.value = e.message || t('cv.error');
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
            <h1 class="text-2xl font-bold text-gray-800">{{ t('plans.create') }}</h1>
            <button @click="cancel" class="text-blue-600 hover:underline font-bold text-sm">
                &larr; {{ t('plans.form.back') }}
            </button>
        </div>

        <div class="bg-white rounded shadow-lg p-8 border border-gray-100">
            <div v-if="errorMessage" class="mb-6 p-4 bg-red-50 text-red-700 rounded whitespace-pre-line font-medium border border-red-100 text-sm">
                {{ errorMessage }}
            </div>

            <form @submit.prevent="handleSubmit" class="space-y-6">
                
                <!-- Code & Name -->
                <div class="grid grid-cols-2 gap-6">
                    <div>
                        <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.code') }} <span class="text-red-500">*</span></label>
                        <input v-model="formData.code" type="text" 
                               class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 font-mono text-sm" 
                               :placeholder="t('plans.form.placeholder_code')" required />
                        <p class="text-[10px] text-gray-400 mt-1 italic font-medium">{{ t('plans.form.unique_id') }}</p>
                    </div>
                    <div>
                        <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.name') }} <span class="text-red-500">*</span></label>
                        <input v-model="formData.name" type="text" 
                               class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 text-sm" 
                               :placeholder="t('plans.form.placeholder_name')" required />
                    </div>
                </div>

                <!-- Price & Tier -->
                <div class="grid grid-cols-2 gap-6">
                    <div>
                        <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.price') }} <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.price" type="number" min="0" 
                               class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 text-sm" required />
                    </div>
                    <div>
                        <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.tier') }} <span class="text-red-500">*</span></label>
                        <select v-model="formData.planType" 
                                class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 text-sm">
                            <option value="PRO">Pro</option>
                            <option value="PREMIUM">Premium</option>
                        </select>
                        <p class="text-[10px] text-blue-500 mt-1 font-bold italic">
                            {{ t('plans.form.free_managed') }}
                        </p>
                    </div>
                </div>

                <!-- Duration & Limits -->
                <div class="grid grid-cols-3 gap-6">
                    <div>
                        <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.duration') }} <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.durationMonths" type="number" min="1" 
                               class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 text-sm" required />
                    </div>
                    <div>
                        <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.max_share') }} <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.maxSharePerMonth" type="number" min="0" 
                               class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 text-sm" required />
                    </div>
                    <div>
                        <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.link_expiry') }} <span class="text-red-500">*</span></label>
                        <input v-model.number="formData.publicLinkExpireDays" type="number" min="1" 
                               class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 text-sm" required />
                    </div>
                </div>

                <!-- Description -->
                <div>
                    <label class="block text-xs font-bold mb-2 text-gray-500 uppercase tracking-wider">{{ t('plans.form.description') }}</label>
                    <textarea v-model="formData.description" rows="4" 
                              class="w-full border border-gray-200 p-3 rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 text-sm"></textarea>
                </div>

                <!-- Actions -->
                <div class="flex items-center justify-end gap-4 pt-6 border-t border-gray-50">
                    <button type="button" @click="cancel" class="px-6 py-2 border border-gray-200 rounded text-gray-600 hover:bg-gray-50 font-bold text-sm transition">
                        {{ t('payments.reset') }}
                    </button>
                    <button type="submit" :disabled="isSubmitting" 
                            class="px-8 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 font-bold shadow-md disabled:bg-blue-300 disabled:cursor-not-allowed transition uppercase tracking-widest text-xs">
                        {{ isSubmitting ? t('common.loading') : t('plans.create') }}
                    </button>
                </div>

            </form>
        </div>
    </div>
</template>
