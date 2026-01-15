<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const users = ref<any[]>([]);
const isLoading = ref(true);

onMounted(async () => {
    try {
        const res = await api.get('/admin/users');
        users.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
});

const toggleLock = async (user: any) => {
    try {
        const action = user.isEnabled ? 'disable' : 'enable';
        await api.put(`/admin/users/${user.id}/${action}`);
        user.isEnabled = !user.isEnabled;
    } catch (e: any) {
        alert("Action failed: " + e.message);
    }
};
</script>

<template>
  <div class="p-8">
      <h1 class="text-2xl font-bold mb-6">User Management</h1>
      <div class="bg-white rounded shadow list-none">
          <table class="w-full text-left border-collapse">
              <thead>
                  <tr class="border-b bg-gray-50">
                      <th class="p-4">ID</th>
                      <th class="p-4">Name</th>
                      <th class="p-4">Email</th>
                      <th class="p-4">Role</th>
                      <th class="p-4">Status</th>
                      <th class="p-4">Actions</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="user in users" :key="user.id" class="border-b hover:bg-gray-50">
                      <td class="p-4">{{ user.id }}</td>
                      <td class="p-4">{{ user.name }}</td>
                      <td class="p-4">{{ user.email }}</td>
                      <td class="p-4">
                          <span :class="user.role === 'ADMIN' ? 'text-red-600 font-bold' : 'text-gray-600'">{{ user.role }}</span>
                      </td>
                      <td class="p-4">
                          <span class="px-2 py-1 rounded text-xs" :class="user.isEnabled ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                              {{ user.isEnabled ? 'Active' : 'Locked' }}
                          </span>
                      </td>
                      <td class="p-4">
                          <button @click="toggleLock(user)" class="text-blue-600 hover:underline">
                              {{ user.isEnabled ? 'Lock' : 'Unlock' }}
                          </button>
                      </td>
                  </tr>
              </tbody>
          </table>
      </div>
  </div>
</template>
