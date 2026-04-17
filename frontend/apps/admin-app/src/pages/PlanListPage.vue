<script setup lang="ts">
import { onMounted, ref, computed, type Ref } from 'vue';
import { useRouter } from 'vue-router';
import { usePlanStore, type Plan } from '@/stores/plan.store';
import PlanModal from '@/components/PlanModal.vue';

const router = useRouter();
const store = usePlanStore();
const isModalOpen = ref(false);
const editMode = ref(false);

type NullablePlan = Plan | null;
const selectedPlan = ref(null) as Ref<NullablePlan>;

// Filter state
const currentFilter = ref('ALL'); // ALL, ACTIVE, INACTIVE

onMounted(() => {
    store.fetchPlans();
});

const filteredPlans = computed(() => {
    let result = [...store.plans];
    
    // 1. Filter
    if (currentFilter.value === 'ACTIVE') result = result.filter(p => p.active);
    if (currentFilter.value === 'INACTIVE') result = result.filter(p => !p.active);
    
    // 2. Sort (Active first, then by code)
    return result.sort((a, b) => {
        if (a.active === b.active) {
            return a.code.localeCompare(b.code);
        }
        return a.active ? -1 : 1;
    });
});

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

const handleActivate = async (plan: Plan) => {
    if (plan.active) return;
    
    if (confirm(`Activate plan "${plan.name}"?`)) {
        try {
            await store.activatePlan(plan.id);
        } catch (e: any) {
             alert("Error activating plan: " + (e.response?.data?.message || e.message));
        }
    }
};

const deletePlan = async (plan: Plan) => {
    if (!plan.active) return;
    
    if (confirm(`Deactivate plan "${plan.name}"? Users will not be able to purchase it anymore.`)) {
        try {
            await store.deletePlan(plan.id);
        } catch (e: any) {
             const msg = e.response?.data?.message || e.message;
             if (msg.includes("in use")) {
                 alert("Cannot deactivate plan currently used by active users.");
             } else {
                 alert("Error: " + msg);
             }
        }
    }
};

const formatCurrency = (val: number, currency: string) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: currency }).format(val);
};

// Dedicated create page
const navigateToCreate = () => router.push({ name: 'plan-create' });
</script>

<template>
  <div class="p-6">
      <!-- Header -->
      <div class="flex justify-between items-center mb-6">
          <div>
              <h1 class="text-2xl font-bold text-gray-800">Subscription Plans</h1>
              <p class="text-sm text-gray-500">Manage functional tiers and purchase options</p>
          </div>
          <button @click="navigateToCreate" class="bg-blue-600 text-white px-5 py-2.5 rounded-lg hover:bg-blue-700 shadow-md transition-all font-bold">
              + Create New Plan
          </button>
      </div>

      <!-- Filters -->
      <div class="flex gap-2 mb-4 bg-gray-50 p-2 rounded-lg w-fit">
          <button v-for="f in ['ALL', 'ACTIVE', 'INACTIVE']" :key="f"
                  @click="currentFilter = f"
                  :class="currentFilter === f ? 'bg-white shadow text-blue-600' : 'text-gray-500 hover:text-gray-700'"
                  class="px-4 py-1.5 rounded-md text-sm font-bold transition-all uppercase">
              {{ f }}
          </button>
      </div>

      <div v-if="store.loading && !store.plans.length" class="text-center py-12">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p class="text-gray-500">Synchronizing plans...</p>
      </div>
      
      <div v-else class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <table class="w-full text-left border-collapse">
              <thead>
                  <tr class="bg-gray-50 border-b text-gray-600">
                      <th class="p-4 font-bold text-xs uppercase tracking-wider">Plan Code</th>
                      <th class="p-4 font-bold text-xs uppercase tracking-wider">Plan Name</th>
                      <th class="p-4 font-bold text-xs uppercase tracking-wider">Tier</th>
                      <th class="p-4 font-bold text-xs uppercase tracking-wider">Price (VND)</th>
                      <th class="p-4 font-bold text-xs uppercase tracking-wider text-center">Duration</th>
                      <th class="p-4 font-bold text-xs uppercase tracking-wider">Status</th>
                      <th class="p-4 font-bold text-xs uppercase tracking-wider text-right">Actions</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="plan in filteredPlans" :key="plan.id" 
                      :class="{'opacity-60 bg-gray-50': !plan.active}"
                      class="border-b transition-colors hover:bg-blue-50/30">
                      <td class="p-4 font-mono text-sm text-gray-600">{{ plan.code }}</td>
                      <td class="p-4 font-bold text-gray-800">
                          {{ plan.name }}
                          <span v-if="plan.planType === 'FREE'" class="ml-2 text-[10px] text-blue-400 font-medium px-1.5 py-0.5 border border-blue-200 rounded uppercase">System Core</span>
                          <span v-else-if="!plan.active" class="ml-2 text-[10px] text-red-400 font-medium px-1.5 py-0.5 border border-red-200 rounded uppercase">Retired</span>
                      </td>
                      <td class="p-4">
                          <span :class="{
                              'bg-gray-200 text-gray-800': plan.planType === 'FREE',
                              'bg-blue-100 text-blue-800': plan.planType === 'PRO',
                              'bg-purple-100 text-purple-800': plan.planType === 'PREMIUM'
                          }" class="px-2.5 py-1 rounded text-[10px] font-black uppercase">
                              {{ plan.planType }}
                          </span>
                      </td>
                      <td class="p-4 font-bold text-gray-700">{{ formatCurrency(plan.price, plan.currency) }}</td>
                      <td class="p-4 text-center font-medium">{{ plan.durationMonths }} Mo</td>
                      <td class="p-4">
                          <div class="flex items-center gap-2">
                             <div :class="plan.active ? 'bg-green-500' : 'bg-red-400'" class="w-2 h-2 rounded-full"></div>
                             <span :class="plan.active ? 'text-green-700' : 'text-red-500'" class="text-xs font-black uppercase">
                                 {{ plan.active ? 'Active' : 'Inactive' }}
                             </span>
                          </div>
                      </td>
                      <td class="p-4 text-right">
                          <div class="flex justify-end gap-3 items-center">
                              <!-- [LEGACY] FREE Plans are Read-only -->
                              <template v-if="plan.planType === 'FREE'">
                                  <span class="text-[10px] text-gray-400 font-bold uppercase italic pr-2">System Core</span>
                              </template>

                              <!-- [ACTIVE] Non-FREE Plans -->
                              <template v-else-if="plan.active">
                                  <button @click="openEditModal(plan)" 
                                          class="text-blue-600 hover:text-blue-800 text-xs font-bold uppercase">
                                      Edit
                                  </button>
                                  
                                  <button @click="deletePlan(plan)" 
                                          class="text-red-600 hover:text-red-800 text-xs font-bold border border-red-100 px-2 py-1 rounded hover:bg-red-50 uppercase shadow-sm">
                                      Deactivate
                                  </button>
                              </template>

                              <!-- [INACTIVE] Non-FREE Plans -->
                              <template v-else>
                                  <button @click="handleActivate(plan)" 
                                          class="text-green-600 hover:text-green-800 text-xs font-bold border border-green-100 px-3 py-1 rounded hover:bg-green-50 uppercase shadow-sm">
                                      Enable
                                  </button>
                              </template>
                          </div>
                      </td>
                  </tr>
              </tbody>
          </table>
          
          <div v-if="!filteredPlans.length" class="p-12 text-center text-gray-500 italic">
              No matching plans found.
          </div>
      </div>

      <!-- Logic for modal -->
      <PlanModal 
        v-if="isModalOpen" 
        :isOpen="isModalOpen" 
        :editMode="editMode" 
        :planToEdit="selectedPlan" 
        @close="isModalOpen = false; store.fetchPlans()" 
       />
  </div>
</template>
