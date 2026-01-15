<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import api from '@/api/axios';
import { ref, onMounted } from 'vue';
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const auth = useAuthStore();
const stats = ref<any>(null);

const chartData = ref({
  labels: ['Free', 'Pro', 'Premium'],
  datasets: [
    { label: 'Subscriptions', data: [0, 0, 0], backgroundColor: '#f87979' }
  ]
})

onMounted(async () => {
    try {
        const res = await api.get('/admin/dashboard');
        stats.value = res.data; 
        // Assuming data structure: { totalUsers, totalCVs, revenue, subscriptions: { free: 10, pro: 5... } }
        
        if (stats.value.subscriptions) {
             chartData.value = {
                labels: ['Free', 'Pro', 'Premium'],
                datasets: [
                    { 
                        label: 'Subscriptions', 
                        data: [stats.value.subscriptions.free, stats.value.subscriptions.pro, stats.value.subscriptions.premium], 
                        backgroundColor: ['#4ade80', '#60a5fa', '#a78bfa'] 
                    }
                ]
             }
        }
    } catch(e) {
        console.error("Dashboard error", e);
    }
});
</script>

<template>
  <div class="flex min-h-screen bg-gray-100">
      <!-- Sidebar -->
      <aside class="w-64 bg-gray-900 text-white flex flex-col">
          <div class="p-6 font-bold text-2xl border-b border-gray-700">Admin Panel</div>
          <nav class="flex-1 p-4 space-y-2">
              <router-link to="/" class="block px-4 py-2 rounded bg-gray-800">Dashboard</router-link>
              <router-link to="/users" class="block px-4 py-2 rounded hover:bg-gray-800">Users</router-link>
              <router-link to="/cv" class="block px-4 py-2 rounded hover:bg-gray-800">CV Management</router-link>
              <router-link to="/templates" class="block px-4 py-2 rounded hover:bg-gray-800">Templates</router-link>
              <router-link to="/payments" class="block px-4 py-2 rounded hover:bg-gray-800">Payments</router-link>
          </nav>
          <div class="p-4 border-t border-gray-700">
              <button @click="auth.logout()" class="w-full text-left px-4 py-2 text-red-400 hover:text-red-300">Logout</button>
          </div>
      </aside>

      <!-- Main -->
      <main class="flex-1 p-8" v-if="stats">
          <h1 class="text-3xl font-bold mb-8">Dashboard Overview</h1>
          
          <div class="grid grid-cols-4 gap-6 mb-8">
              <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Total Users</div>
                  <div class="text-3xl font-bold">{{ stats.totalUsers }}</div>
              </div>
              <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Active CVs</div>
                  <div class="text-3xl font-bold">{{ stats.totalCVs }}</div>
              </div>
              <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Total Revenue</div>
                  <div class="text-3xl font-bold text-green-600">${{ stats.revenue }}</div>
              </div>
               <div class="bg-white p-6 rounded shadow">
                  <div class="text-gray-500 text-sm">Pending Requests</div>
                  <div class="text-3xl font-bold text-yellow-500">{{ stats.pendingRequests || 0 }}</div>
              </div>
          </div>

          <div class="bg-white p-6 rounded shadow h-96 w-1/2">
              <h3 class="font-bold mb-4">Subscription Distribution</h3>
              <Bar :data="chartData" :options="{ responsive: true, maintainAspectRatio: false }" />
          </div>
      </main>
      
      <div v-else class="flex-1 flex items-center justify-center">
          Loading Dashboard...
      </div>
  </div>
</template>
