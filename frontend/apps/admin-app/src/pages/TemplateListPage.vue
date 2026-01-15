<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const templates = ref<any[]>([]);
const isLoading = ref(true);

onMounted(async () => {
    try {
        const res = await api.get('/admin/templates');
        templates.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
});
</script>

<template>
  <div class="p-8">
      <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold">Template Management</h1>
          <router-link to="/templates/create" class="bg-blue-600 text-white px-4 py-2 rounded">Create New</router-link>
      </div>

      <div v-if="isLoading">Loading...</div>
      
      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="tmpl in templates" :key="tmpl.id" class="bg-white p-4 border rounded shadow">
              <div class="h-40 bg-gray-100 flex items-center justify-center mb-4">Preview</div>
              <h3 class="font-bold font-lg">{{ tmpl.name }}</h3>
              <p class="text-sm text-gray-500 mb-2">Plan: {{ tmpl.planRequired || 'FREE' }}</p>
              
              <router-link :to="`/templates/${tmpl.id}`" class="text-blue-600 hover:underline">Edit</router-link>
          </div>
      </div>
  </div>
</template>
