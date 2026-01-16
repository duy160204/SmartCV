<script setup lang="ts">
import api from '@/api/axios';
import { ref, onMounted } from 'vue';
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const stats = ref<any>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);

const chartData = ref({
  labels: ['Free', 'Pro', 'Premium'],
  datasets: [
    { label: 'Subscriptions', data: [0, 0, 0], backgroundColor: '#f87979' as any }
  ]
})

const loadDashboard = async () => {
    try {
        isLoading.value = true;
        error.value = null;
        const res = await api.get('/admin/dashboard');
        // Controller returns AdminDashboardResponse directly (not wrapped in ApiResponse)
        stats.value = res.data; 
        
        if (stats.value) {
             chartData.value = {
                labels: ['Free', 'Pro', 'Premium'],
                datasets: [
                    { 
                        label: 'Subscriptions', 
                        data: [
                            stats.value.freeUsers || 0, 
                            stats.value.proUsers || 0, 
                            stats.value.premiumUsers || 0
                        ], 
                        backgroundColor: ['#4ade80', '#60a5fa', '#a78bfa'] as any 
                    }
                ]
             }
        }
    } catch(e: any) {
        console.error("Dashboard error", e);
        error.value = e.response?.data?.message || e.message || 'Failed to load dashboard';
    } finally {
        isLoading.value = false;
    }
};

const reload = () => window.location.reload();

const formatCurrency = (val: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
};

onMounted(() => {
   loadDashboard();
});
</script>

<template>
  <div class="p-8 min-h-screen">
      <!-- Loading State -->
      <div v-if="isLoading" class="flex items-center justify-center min-h-[400px]">
          <div class="text-center">
              <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
              <p class="text-gray-500">Loading Dashboard...</p>
          </div>
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="flex items-center justify-center min-h-[400px]">
          <div class="text-center p-8 bg-white rounded shadow max-w-md">
              <div class="text-red-500 text-4xl mb-4">⚠️</div>
              <h2 class="text-xl font-bold text-red-600 mb-2">Failed to Load Dashboard</h2>
              <p class="text-gray-600 mb-4">{{ error }}</p>
              <button @click="reload" class="bg-blue-600 text-white px-4 py-2 rounded">Retry</button>
          </div>
      </div>
      
      <!-- Main Content -->
      <div v-else-if="stats">
          <h1 class="text-3xl font-bold mb-8">Dashboard Overview</h1>
          
          <div class="grid grid-cols-4 gap-6 mb-8">
              <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Total Users</div>
                  <div class="text-3xl font-bold">{{ stats.totalUsers || 0 }}</div>
                  <div class="text-xs text-gray-400 mt-1">Verified: {{ stats.verifiedUsers }} | Locked: {{ stats.lockedUsers }}</div>
              </div>
              <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Active CVs</div>
                  <div class="text-3xl font-bold">{{ stats.totalCVs || 0 }}</div>
                  <div class="text-xs text-gray-400 mt-1">Public: {{ stats.publicCVs }}</div>
              </div>
              <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Total Revenue</div>
                  <div class="text-3xl font-bold text-green-600">{{ formatCurrency(stats.totalRevenue || 0) }}</div>
                  <div class="text-xs text-gray-400 mt-1">From {{ stats.paidUsers }} paid users</div>
              </div>
               <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Successful Payments</div>
                  <div class="text-3xl font-bold text-blue-500">{{ stats.successPayments || 0 }}</div>
                  <div class="text-xs text-gray-400 mt-1">Total txns: {{ stats.totalPayments }}</div>
              </div>
          </div>

          <div class="bg-white p-6 rounded shadow h-96 w-1/2">
              <h3 class="font-bold mb-4">Subscription Distribution</h3>
              <Bar :data="chartData" :options="{ responsive: true, maintainAspectRatio: false }" />
          </div>
      </div>
  </div>
</template>
