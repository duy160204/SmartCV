<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const templates = ref<any[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

onMounted(async () => {
    try {
        isLoading.value = true;
        error.value = null;
        const res = await api.get('/admin/templates');
        // Backend returns ApiResponse<List<Template>>, so we need res.data.data
        templates.value = res.data.data || [];
    } catch (e: any) {
        console.error(e);
        error.value = e.response?.data?.message || e.message || 'Failed to load templates';
    } finally {
        isLoading.value = false;
    }
});

const enableTemplate = async (id: number) => {
    try {
        await api.put(`/admin/templates/${id}/enable`);
        const tmpl = templates.value.find(t => t.id === id);
        if (tmpl) tmpl.isEnabled = true;
        alert('Template enabled');
    } catch (e: any) {
        alert('Error: ' + (e.response?.data?.message || e.message));
    }
};

const disableTemplate = async (id: number) => {
    try {
        await api.put(`/admin/templates/${id}/disable`);
        const tmpl = templates.value.find(t => t.id === id);
        if (tmpl) tmpl.isEnabled = false;
        alert('Template disabled');
    } catch (e: any) {
        alert('Error: ' + (e.response?.data?.message || e.message));
    }
};

const deleteTemplate = async (id: number) => {
    if (!confirm('Delete this template permanently?')) return;
    try {
        await api.delete(`/admin/templates/${id}`);
        templates.value = templates.value.filter(t => t.id !== id);
        alert('Template deleted');
    } catch (e: any) {
        alert('Error: ' + (e.response?.data?.message || e.message));
    }
};
</script>

<template>
  <div class="p-8">
      <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold">Template Management</h1>
          <router-link to="/templates/create" class="bg-blue-600 text-white px-4 py-2 rounded">Create New</router-link>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-12">
          <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p class="text-gray-500">Loading templates...</p>
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 p-6 rounded text-center">
          <p class="font-bold mb-2">Failed to load templates</p>
          <p class="text-sm">{{ error }}</p>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="templates.length === 0" class="bg-gray-50 p-8 text-center rounded">
          <p class="text-gray-500 mb-4">No templates found</p>
          <router-link to="/templates/create" class="text-blue-600 hover:underline">Create your first template</router-link>
      </div>
      
      <!-- Template Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="tmpl in templates" :key="tmpl.id" class="bg-white p-4 border rounded shadow">
              <div class="h-40 bg-gray-100 flex items-center justify-center mb-4 overflow-hidden relative group">
                  <img v-if="tmpl.thumbnailUrl" :src="tmpl.thumbnailUrl" alt="Preview" class="w-full h-full object-cover" />
                  <span v-else class="text-gray-400">No Preview</span>
                  
                  <!-- Hover Overlay for Preview -->
                  <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all flex items-center justify-center">
                  </div>
              </div>
              <h3 class="font-bold font-lg">{{ tmpl.name || 'Untitled' }}</h3>
              <p class="text-sm text-gray-500 mb-2">Plan: {{ tmpl.planRequired || 'FREE' }}</p>
              <p class="text-xs mb-3" :class="tmpl.isEnabled !== false ? 'text-green-600' : 'text-red-600'">
                  {{ tmpl.isEnabled !== false ? 'Enabled' : 'Disabled' }}
              </p>
              
              <div class="flex gap-2 flex-wrap">
                  <router-link :to="`/templates/${tmpl.id}`" class="text-blue-600 hover:underline text-sm font-medium">Edit / Preview</router-link>
                  <button v-if="tmpl.isEnabled !== false" @click="disableTemplate(tmpl.id)" class="text-yellow-600 hover:underline text-sm">Disable</button>
                  <button v-else @click="enableTemplate(tmpl.id)" class="text-green-600 hover:underline text-sm">Enable</button>
                  <button @click="deleteTemplate(tmpl.id)" class="text-red-600 hover:underline text-sm">Delete</button>
              </div>
          </div>
      </div>
  </div>
</template>
