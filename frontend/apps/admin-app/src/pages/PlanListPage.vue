<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { usePlanStore, type Plan } from '@/stores/plan.store';
import PlanModal from '@/components/PlanModal.vue';

const router = useRouter();
const store = usePlanStore();
const isModalOpen = ref(false);
const editMode = ref(false);
const selectedPlan = ref<Plan | null>(null);

onMounted(() => {
    store.fetchPlans();
});

// Navigate to dedicated create page
const navigateToCreateCheck = () => {
    router.push({ name: 'plan-create' });
};

const openCreateModal = () => {
    editMode.value = false;
    selectedPlan.value = null;
    isModalOpen.value = true;
};

const openEditModal = (plan: Plan) => {
    editMode.value = true;
    selectedPlan.value = plan;
    isModalOpen.value = true;
};

const toggleStatus = async (plan: Plan) => {
    if (confirm(`Are you sure you want to ${plan.active ? 'disable' : 'enable'} this plan?`)) {
        await store.toggleStatus(plan.id);
    }
};

const formatCurrency = (val: number, currency: string) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: currency }).format(val);
};
</script>

<template>
  <div class="p-6">
      <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold">Subscription Plans</h1>
          <button @click="navigateToCreateCheck" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 shadow-lg transition-colors">
              + Create New Plan
          </button>
      </div>

      <div v-if="store.loading && !store.plans.length" class="text-center py-8">Loading...</div>
      
      <div v-else class="bg-white rounded shadow overflow-hidden">
          <table class="w-full text-left border-collapse">
              <thead>
                  <tr class="bg-gray-100 border-b">
                      <th class="p-4 font-semibold">Code</th>
                      <th class="p-4 font-semibold">Name</th>
                      <th class="p-4 font-semibold">Tier</th>
                      <th class="p-4 font-semibold">Price</th>
                      <th class="p-4 font-semibold">Duration</th>
                      <th class="p-4 font-semibold">Status</th>
                      <th class="p-4 font-semibold text-right">Actions</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="plan in store.plans" :key="plan.id" class="border-b hover:bg-gray-50">
                      <td class="p-4 font-mono text-sm">{{ plan.code }}</td>
                      <td class="p-4 font-medium">{{ plan.name }}</td>
                      <td class="p-4">
                          <span :class="{
                              'bg-gray-200 text-gray-800': plan.planType === 'FREE',
                              'bg-blue-100 text-blue-800': plan.planType === 'PRO',
                              'bg-purple-100 text-purple-800': plan.planType === 'PREMIUM'
                          }" class="px-2 py-1 rounded text-xs font-bold">
                              {{ plan.planType }}
                          </span>
                      </td>
                      <td class="p-4">{{ formatCurrency(plan.price, plan.currency) }}</td>
                      <td class="p-4">{{ plan.durationMonths }} Mo</td>
                      <td class="p-4">
                          <span :class="plan.active ? 'text-green-600' : 'text-red-500'" class="font-bold">
                              {{ plan.active ? 'Active' : 'Disabled' }}
                          </span>
                      </td>
                      <td class="p-4 text-right space-x-2">
                          <button @click="openEditModal(plan)" class="text-blue-600 hover:underline">Edit</button>
                          <button @click="toggleStatus(plan)" class="text-sm font-medium" 
                                  :class="plan.active ? 'text-red-600 hover:text-red-800' : 'text-green-600 hover:text-green-800'">
                              {{ plan.active ? 'Disable' : 'Enable' }}
                          </button>
                      </td>
                  </tr>
              </tbody>
          </table>
          
          <div v-if="!store.plans.length" class="p-8 text-center text-gray-500">
              No plans found. Create one above.
          </div>
      </div>

      <PlanModal 
        v-if="isModalOpen" 
        :isOpen="isModalOpen" 
        :editMode="editMode" 
        :planToEdit="selectedPlan" 
        @close="isModalOpen = false; store.fetchPlans()" 
       />
  </div>
</template>
