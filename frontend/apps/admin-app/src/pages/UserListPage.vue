<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const users = ref<any[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const loadUsers = async () => {
    try {
        isLoading.value = true;
        error.value = null;
        const res = await api.get('/admin/users');
        users.value = res.data || [];
    } catch (e: any) {
        console.error(e);
        error.value = e.response?.data?.message || e.message || 'Failed to load users';
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    loadUsers();
});

const toggleLock = async (user: any) => {
    try {
        // Backend uses locked: boolean
        const action = user.locked ? 'unlock' : 'lock';
        await api.put(`/admin/users/${user.id}/${action}`);
        user.locked = !user.locked;
        // alert(`User ${action}ed`);
    } catch (e: any) {
        alert("Action failed: " + (e.response?.data?.message || e.message));
    }
};

const viewDetail = async (id: number) => {
    try {
        const res = await api.get(`/admin/users/${id}`);
        // Backend returns AdminUserDetailResponse raw object
        alert('User Detail:\n' + JSON.stringify(res.data, null, 2));
    } catch(e: any) {
        alert("Failed to load detail: " + e.message);
    }
};

const deleteUser = () => {
    alert("Delete operation is not supported by the backend.");
};
</script>

<template>
  <div class="p-8">
      <h1 class="text-2xl font-bold mb-6">User Management</h1>
      
      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-12">
          <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p class="text-gray-500">Loading users...</p>
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 p-6 rounded text-center">
          <p class="font-bold mb-2">Failed to load users</p>
          <p class="text-sm">{{ error }}</p>
          <button @click="loadUsers" class="mt-4 text-blue-600 underline">Retry</button>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="users.length === 0" class="bg-gray-50 p-8 text-center rounded">
          <p class="text-gray-500">No users found</p>
      </div>
      
      <!-- Data Table -->
      <div v-else class="bg-white rounded shadow text-sm">
          <table class="w-full text-left border-collapse">
              <thead>
                  <tr class="border-b bg-gray-50">
                      <th class="p-4">ID</th>
                      <th class="p-4">Email</th>
                      <th class="p-4">Plan</th>
                      <th class="p-4">Verified</th>
                      <th class="p-4">Status</th>
                      <th class="p-4">Created At</th>
                      <th class="p-4">Actions</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="user in users" :key="user.id" class="border-b hover:bg-gray-50">
                      <td class="p-4">{{ user.id }}</td>
                      <td class="p-4 font-medium">{{ user.email }}</td>
                      <td class="p-4">
                          <span class="px-2 py-1 bg-blue-50 text-blue-700 rounded text-xs font-bold">{{ user.plan || 'FREE' }}</span>
                      </td>
                      <td class="p-4">
                          <span :class="user.verified ? 'text-green-600' : 'text-gray-400'">
                              {{ user.verified ? 'Yes' : 'No' }}
                          </span>
                      </td>
                      <td class="p-4">
                          <span class="px-2 py-1 rounded text-xs" :class="user.locked ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'">
                              {{ user.locked ? 'Locked' : 'Active' }}
                          </span>
                      </td>
                      <td class="p-4">{{ user.createdAt || '-' }}</td>
                      <td class="p-4 flex gap-2">
                          <button v-if="user.locked" @click="toggleLock(user)" class="text-green-600 hover:underline">Unlock</button>
                          <button v-else @click="toggleLock(user)" class="text-yellow-600 hover:underline">Lock</button>
                      </td>
                  </tr>
              </tbody>
          </table>
      </div>
  </div>
</template>
