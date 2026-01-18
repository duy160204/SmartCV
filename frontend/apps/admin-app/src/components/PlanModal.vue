<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { usePlanStore, type Plan } from '@/stores/plan.store';

const props = defineProps<{
    isOpen: boolean;
    editMode: boolean;
    planToEdit?: Plan | null;
}>();

const emit = defineEmits(['close']);

const store = usePlanStore();

const formData = ref({
    code: '',
    name: '',
    price: 0,
    durationMonths: 1,
    planType: 'PRO', // Default
    maxSharePerMonth: 10,
    publicLinkExpireDays: 30,
    description: ''
});

// Reset form when modal opens
watch(
    () => props.isOpen,
    (val) => {
        if (val) {
            if (props.editMode && props.planToEdit) {
                formData.value = { ...props.planToEdit };
            } else {
                // Reset for create
                formData.value = {
                    code: '',
                    name: '',
                    price: 0,
                    durationMonths: 1,
                    planType: 'PRO',
                    maxSharePerMonth: 10,
                    publicLinkExpireDays: 30,
                    description: ''
                };
            }
        }
    }
);

const handleSubmit = async () => {
    console.log('[PlanModal] handleSubmit called');
    console.log('[PlanModal] FormData:', JSON.parse(JSON.stringify(formData.value)));
    try {
        if (props.editMode && props.planToEdit) {
            console.log('[PlanModal] Updating plan...', props.planToEdit.id);
            await store.updatePlan(props.planToEdit.id, formData.value);
        } else {
            console.log('[PlanModal] Creating plan...');
            await store.createPlan(formData.value);
        }
        console.log('[PlanModal] Action successful, emitting close');
        emit('close');
    } catch (e: any) {

        console.error('Plan creation/update failed', e);
        let msg = e.message;
        if (e.response && e.response.data) {
             const data = e.response.data;
             // Spring Boot standard validation error (GlobalExceptionHandler returns 'details' map)
             if (data.details && typeof data.details === 'object') {
                 msg = Object.entries(data.details)
                    .map(([field, message]) => `${field}: ${message}`)
                    .join('\n');
             } else if (data.message) {
                 msg = data.message;
             } else if (typeof data === 'string') {
                 msg = data;
             }
        }
        alert(`Error:\n${msg}`);
    }
};
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[9999]">
      <div class="bg-white rounded p-6 w-[600px] shadow-xl max-h-[90vh] overflow-y-auto">
          <h2 class="text-xl font-bold mb-4">{{ editMode ? 'Edit Plan' : 'Create New Plan' }}</h2>
          
          <form @submit.prevent="handleSubmit" class="space-y-4">
              
              <!-- Code (Immutable in Edit) -->
              <div>
                  <label class="block text-sm font-medium">Plan Code (Unique)</label>
                  <input v-model="formData.code" type="text" class="border p-2 w-full rounded" 
                         :disabled="editMode" required placeholder="e.g. PRO_MONTHLY" />
              </div>

              <!-- Name -->
               <div>
                  <label class="block text-sm font-medium">Display Name</label>
                  <input v-model="formData.name" type="text" class="border p-2 w-full rounded" required />
              </div>

              <div class="grid grid-cols-2 gap-4">
                  <!-- Tier (Immutable in Edit for simplicity, or complex logic needed) -->
                  <div>
                      <label class="block text-sm font-medium">Tier</label>
                      <select v-model="formData.planType" class="border p-2 w-full rounded" :disabled="editMode">
                          <option value="FREE">Free</option>
                          <option value="PRO">Pro</option>
                          <option value="PREMIUM">Premium</option>
                      </select>
                  </div>

                  <!-- Price -->
                   <div>
                      <label class="block text-sm font-medium">Price (VND)</label>
                      <input v-model.number="formData.price" type="number" class="border p-2 w-full rounded" required min="0" />
                  </div>
              </div>

              <div class="grid grid-cols-2 gap-4">
                  <!-- Duration -->
                   <div>
                      <label class="block text-sm font-medium">Duration (Months)</label>
                      <input v-model.number="formData.durationMonths" type="number" class="border p-2 w-full rounded" required min="1" />
                  </div>
                  <!-- Max Share -->
                   <div>
                      <label class="block text-sm font-medium">Max Share / Month</label>
                      <input v-model.number="formData.maxSharePerMonth" type="number" class="border p-2 w-full rounded" required min="1" />
                  </div>
              </div>
              
              <div>
                  <label class="block text-sm font-medium">Public Link Expiry (Days)</label>
                  <input v-model.number="formData.publicLinkExpireDays" type="number" class="border p-2 w-full rounded" required min="1" />
              </div>

              <!-- Description -->
              <div>
                  <label class="block text-sm font-medium">Description</label>
                  <textarea v-model="formData.description" class="border p-2 w-full rounded" rows="3"></textarea>
              </div>

              <div class="flex justify-end gap-2 mt-6">
                  <button type="button" @click="$emit('close')" class="px-4 py-2 border rounded hover:bg-gray-50">Cancel</button>
                  <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
                      {{ editMode ? 'Save Changes' : 'Create Plan' }}
                  </button>
              </div>

          </form>
      </div>
  </div>
</template>
