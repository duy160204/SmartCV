<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import api from '@/api/axios';

interface Payment {
    id: number;
    userId: number;
    amount: number;
    status: string;
    provider: string;
    transactionId: string;
    createdAt: string;
}

const payments = ref<Payment[]>([]);
const isLoading = ref(true);
const filter = ref({
    status: '',
    provider: '',
    userId: '',
    fromDate: '',
    toDate: ''
});

onMounted(async () => {
    await loadPayments();
});

const loadPayments = async () => {
    try {
        isLoading.value = true;
        const res = await api.get('/admin/payments');
        payments.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const filterByStatus = async (status: string) => {
    try {
        isLoading.value = true;
        const res = await api.get(`/admin/payments/status/${status}`);
        payments.value = res.data;
    } catch (e) {
        console.error(e);
        alert('Filter failed');
    } finally {
        isLoading.value = false;
    }
};

const filterByProvider = async (provider: string) => {
    try {
        isLoading.value = true;
        const res = await api.get(`/admin/payments/provider/${provider}`);
        payments.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const searchPayments = async () => {
    try {
        isLoading.value = true;
        const params: any = {};
        if (filter.value.userId) params.userId = filter.value.userId;
        if (filter.value.fromDate) params.from = filter.value.fromDate;
        if (filter.value.toDate) params.to = filter.value.toDate;
        
        const res = await api.get('/admin/payments/search', { params });
        payments.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const resetFilters = async () => {
    filter.value = { status: '', provider: '', userId: '', fromDate: '', toDate: '' };
    await loadPayments();
};

const totalRevenue = computed(() => {
    return payments.value
        .filter(p => p.status === 'SUCCESS')
        .reduce((sum, p) => sum + p.amount, 0);
});

const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleString();
};

const getStatusClass = (status: string) => {
    switch (status) {
        case 'SUCCESS': return 'bg-green-100 text-green-800';
        case 'PENDING': return 'bg-yellow-100 text-yellow-800';
        case 'FAILED': return 'bg-red-100 text-red-800';
        default: return 'bg-gray-100 text-gray-800';
    }
};
</script>

<template>
  <div class="p-8">
      <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold">Payment Management</h1>
          <div class="bg-green-100 text-green-800 px-4 py-2 rounded-lg font-bold">
              Total Revenue: {{ formatCurrency(totalRevenue) }}
          </div>
      </div>
      
      <!-- Filters -->
      <div class="bg-white p-4 rounded-lg shadow mb-6">
          <div class="grid grid-cols-6 gap-4">
              <select v-model="filter.status" @change="filter.status ? filterByStatus(filter.status) : resetFilters()" class="border p-2 rounded">
                  <option value="">All Status</option>
                  <option value="SUCCESS">Success</option>
                  <option value="PENDING">Pending</option>
                  <option value="FAILED">Failed</option>
              </select>
              
              <select v-model="filter.provider" @change="filter.provider ? filterByProvider(filter.provider) : resetFilters()" class="border p-2 rounded">
                  <option value="">All Providers</option>
                  <option value="VNPAY">VNPAY</option>
                  <option value="MOMO">MOMO</option>
              </select>
              
              <input v-model="filter.userId" type="number" placeholder="User ID" class="border p-2 rounded" />
              <input v-model="filter.fromDate" type="date" class="border p-2 rounded" />
              <input v-model="filter.toDate" type="date" class="border p-2 rounded" />
              
              <div class="flex gap-2">
                  <button @click="searchPayments" class="bg-blue-600 text-white px-4 py-2 rounded">Search</button>
                  <button @click="resetFilters" class="bg-gray-200 px-4 py-2 rounded">Reset</button>
              </div>
          </div>
      </div>
      
      <!-- Table -->
      <div class="bg-white rounded shadow overflow-hidden">
          <div v-if="isLoading" class="p-8 text-center text-gray-500">Loading...</div>
          <table v-else class="w-full text-left border-collapse">
              <thead>
                  <tr class="border-b bg-gray-50">
                      <th class="p-4">ID</th>
                      <th class="p-4">User ID</th>
                      <th class="p-4">Amount</th>
                      <th class="p-4">Provider</th>
                      <th class="p-4">Status</th>
                      <th class="p-4">Transaction ID</th>
                      <th class="p-4">Date</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="payment in payments" :key="payment.id" class="border-b hover:bg-gray-50">
                      <td class="p-4">{{ payment.id }}</td>
                      <td class="p-4">{{ payment.userId }}</td>
                      <td class="p-4 font-medium">{{ formatCurrency(payment.amount) }}</td>
                      <td class="p-4">{{ payment.provider }}</td>
                      <td class="p-4">
                          <span class="px-2 py-1 rounded text-xs font-medium" :class="getStatusClass(payment.status)">
                              {{ payment.status }}
                          </span>
                      </td>
                      <td class="p-4 text-xs text-gray-500 font-mono">{{ payment.transactionId || '-' }}</td>
                      <td class="p-4 text-sm">{{ formatDate(payment.createdAt) }}</td>
                  </tr>
                  <tr v-if="payments.length === 0">
                      <td colspan="7" class="p-8 text-center text-gray-500">No payments found</td>
                  </tr>
              </tbody>
          </table>
      </div>
  </div>
</template>
