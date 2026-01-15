<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const cvs = ref<any[]>([]);
const isLoading = ref(true);

onMounted(async () => {
    try {
        const res = await api.get('/admin/cv');
        cvs.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
});

const deleteCV = async (id: number) => {
    if (!confirm("Are you sure?")) return;
    try {
        await api.delete(`/admin/cv/${id}`); // Assuming endpoint
        cvs.value = cvs.value.filter(c => c.id !== id);
    } catch (e) {
        alert("Error: " + e.message);
    }
};
</script>

<template>
  <div class="p-8">
      <h1 class="text-2xl font-bold mb-6">CV Management</h1>
      <div class="bg-white rounded shadow">
           <table class="w-full text-left border-collapse">
              <thead>
                  <tr class="border-b bg-gray-50">
                      <th class="p-4">ID</th>
                      <th class="p-4">Title</th>
                      <th class="p-4">User</th>
                      <th class="p-4">Template</th>
                      <th class="p-4">Created At</th>
                      <th class="p-4">Actions</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="cv in cvs" :key="cv.id" class="border-b hover:bg-gray-50">
                      <td class="p-4">{{ cv.id }}</td>
                      <td class="p-4 font-medium">{{ cv.title }}</td>
                      <td class="p-4">{{ cv.user?.name || cv.userId }}</td>
                      <td class="p-4">{{ cv.templateId }}</td>
                      <td class="p-4">{{ new Date(cv.createdAt).toLocaleDateString() }}</td>
                      <td class="p-4">
                          <button @click="deleteCV(cv.id)" class="text-red-500 hover:text-red-700">Delete</button>
                      </td>
                  </tr>
              </tbody>
          </table>
      </div>
  </div>
</template>
