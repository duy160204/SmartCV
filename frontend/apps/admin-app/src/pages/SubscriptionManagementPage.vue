<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

interface SubscriptionRequest {
    id: number;
    userId: number;
    requestedPlan: string;
    status: string;
    createdAt: string;
}

const requests = ref<SubscriptionRequest[]>([]);
const isLoading = ref(true);
const activeTab = ref('all');

// Direct subscription preview
const previewUserId = ref<number | null>(null);
const previewPlan = ref('PRO');
const previewResult = ref<any>(null);

onMounted(async () => {
    await loadRequests();
});

const loadRequests = async () => {
    try {
        isLoading.value = true;
        const res = await api.get('/admin/subscription-requests');
        requests.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const loadByStatus = async (status: string) => {
    try {
        isLoading.value = true;
        activeTab.value = status;
        const res = await api.get(`/admin/subscription-requests/status/${status}`);
        requests.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
};

const previewRequest = async (id: number) => {
    try {
        const res = await api.post(`/admin/subscription-requests/${id}/preview`);
        previewResult.value = res.data;
        alert('Preview loaded! Check console for details.\n' + JSON.stringify(res.data, null, 2));
    } catch (e: any) {
        alert('Preview failed: ' + (e.response?.data?.message || e.message));
    }
};

const confirmRequest = async (id: number) => {
    if (!confirm('Confirm this subscription request?')) return;
    try {
        await api.post(`/admin/subscription-requests/${id}/confirm`);
        alert('Subscription confirmed!');
        await loadRequests();
    } catch (e: any) {
        alert('Confirm failed: ' + (e.response?.data?.message || e.message));
    }
};

// Direct subscription management
const directPreview = async () => {
    if (!previewUserId.value) return;
    try {
        const res = await api.post('/admin/subscriptions/preview', {
            userId: previewUserId.value,
            newPlan: previewPlan.value
        });
        previewResult.value = res.data;
        alert('Preview:\n' + JSON.stringify(res.data, null, 2));
    } catch (e: any) {
        alert('Preview failed: ' + (e.response?.data?.message || e.message));
    }
};

const directConfirm = async () => {
    if (!previewUserId.value) return;
    if (!confirm(`Confirm subscription change for user ${previewUserId.value} to ${previewPlan.value}?`)) return;
    try {
        await api.post('/admin/subscriptions/confirm', {
            userId: previewUserId.value,
            newPlan: previewPlan.value,
            confirm: true
        });
        alert('Subscription updated!');
    } catch (e: any) {
        alert('Confirm failed: ' + (e.response?.data?.message || e.message));
    }
};

const getStatusClass = (status: string) => {
    switch (status) {
        case 'PENDING': return 'bg-yellow-100 text-yellow-800';
        case 'APPROVED': return 'bg-green-100 text-green-800';
        case 'REJECTED': return 'bg-red-100 text-red-800';
        default: return 'bg-gray-100 text-gray-800';
    }
};
</script>

<template>
  <div class="p-8">
      <h1 class="text-2xl font-bold mb-6">Subscription Management</h1>
      
      <!-- Direct Subscription Update -->
      <div class="bg-white p-6 rounded-lg shadow mb-8">
          <h2 class="font-bold text-lg mb-4">Direct Subscription Update</h2>
          <div class="flex gap-4 items-end">
              <div>
                  <label class="block text-sm font-medium mb-1">User ID</label>
                  <input v-model.number="previewUserId" type="number" class="border p-2 rounded w-32" placeholder="User ID" />
              </div>
              <div>
                  <label class="block text-sm font-medium mb-1">Plan</label>
                  <select v-model="previewPlan" class="border p-2 rounded">
                      <option value="FREE">FREE</option>
                      <option value="PRO">PRO</option>
                      <option value="PREMIUM">PREMIUM</option>
                  </select>
              </div>
              <button @click="directPreview" class="bg-blue-600 text-white px-4 py-2 rounded">Preview</button>
              <button @click="directConfirm" class="bg-green-600 text-white px-4 py-2 rounded">Confirm</button>
          </div>
      </div>
      
      <!-- Tabs -->
      <div class="flex gap-2 mb-4">
          <button @click="loadRequests(); activeTab = 'all'" 
                  :class="['px-4 py-2 rounded', activeTab === 'all' ? 'bg-blue-600 text-white' : 'bg-gray-200']">
              All
          </button>
          <button @click="loadByStatus('PENDING')" 
                  :class="['px-4 py-2 rounded', activeTab === 'PENDING' ? 'bg-yellow-500 text-white' : 'bg-gray-200']">
              Pending
          </button>
          <button @click="loadByStatus('APPROVED')" 
                  :class="['px-4 py-2 rounded', activeTab === 'APPROVED' ? 'bg-green-600 text-white' : 'bg-gray-200']">
              Approved
          </button>
          <button @click="loadByStatus('REJECTED')" 
                  :class="['px-4 py-2 rounded', activeTab === 'REJECTED' ? 'bg-red-600 text-white' : 'bg-gray-200']">
              Rejected
          </button>
      </div>
      
      <!-- Table -->
      <div class="bg-white rounded shadow">
          <div v-if="isLoading" class="p-8 text-center text-gray-500">Loading...</div>
          <table v-else class="w-full text-left border-collapse">
              <thead>
                  <tr class="border-b bg-gray-50">
                      <th class="p-4">ID</th>
                      <th class="p-4">User ID</th>
                      <th class="p-4">Requested Plan</th>
                      <th class="p-4">Status</th>
                      <th class="p-4">Created</th>
                      <th class="p-4">Actions</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="req in requests" :key="req.id" class="border-b hover:bg-gray-50">
                      <td class="p-4">{{ req.id }}</td>
                      <td class="p-4">{{ req.userId }}</td>
                      <td class="p-4 font-medium">{{ req.requestedPlan }}</td>
                      <td class="p-4">
                          <span class="px-2 py-1 rounded text-xs font-medium" :class="getStatusClass(req.status)">
                              {{ req.status }}
                          </span>
                      </td>
                      <td class="p-4 text-sm">{{ new Date(req.createdAt).toLocaleString() }}</td>
                      <td class="p-4 space-x-2">
                          <button @click="previewRequest(req.id)" class="text-blue-600 hover:underline">Preview</button>
                          <button v-if="req.status === 'PENDING'" @click="confirmRequest(req.id)" class="text-green-600 hover:underline">Confirm</button>
                      </td>
                  </tr>
                  <tr v-if="requests.length === 0">
                      <td colspan="6" class="p-8 text-center text-gray-500">No requests found</td>
                  </tr>
              </tbody>
          </table>
      </div>
  </div>
</template>
