<script setup lang="ts">
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const templates = ref<any[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const getImageUrl = (url: string | null) => {
    if (!url) return '';
    if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url;
    const backendBaseUrl = (import.meta as any).env.VITE_BACKEND_URL || '';
    return backendBaseUrl + (url.startsWith('/') ? url : '/' + url);
};

onMounted(async () => {
    try {
        isLoading.value = true;
        error.value = null;
        const res = await api.get('/admin/templates');
        templates.value = res.data.data || [];
    } catch (e: any) {
        console.error(e);
        error.value = e.response?.data?.message || e.message || t('cv.error');
    } finally {
        isLoading.value = false;
    }
});

const enableTemplate = async (id: number) => {
    try {
        await api.put(`/admin/templates/${id}/enable`);
        const tmpl = templates.value.find(t => t.id === id);
        if (tmpl) tmpl.isEnabled = true;
        alert(t('plans.enable') + ' ' + t('status.SUCCESS').toLowerCase());
    } catch (e: any) {
        alert('Error: ' + (e.response?.data?.message || e.message));
    }
};

const disableTemplate = async (id: number) => {
    try {
        await api.put(`/admin/templates/${id}/disable`);
        const tmpl = templates.value.find(t => t.id === id);
        if (tmpl) tmpl.isEnabled = false;
        alert(t('plans.deactivate') + ' ' + t('status.SUCCESS').toLowerCase());
    } catch (e: any) {
        alert('Error: ' + (e.response?.data?.message || e.message));
    }
};

const deleteTemplate = async (id: number) => {
    if (!confirm(t('common.refresh') + '?')) return; // Just using an existing confirm-like key for now or add new one
    try {
        await api.delete(`/admin/templates/${id}`);
        templates.value = templates.value.filter(t => t.id !== id);
        alert(t('table.col_actions') + ' ' + t('status.SUCCESS').toLowerCase());
    } catch (e: any) {
        alert('Error: ' + (e.response?.data?.message || e.message));
    }
};
</script>

<template>
  <div class="p-8 text-sm">
      <div class="flex justify-between items-center mb-6">
          <h1 class="text-2xl font-bold text-gray-800">{{ t('template.management') }}</h1>
          <router-link to="/templates/create" class="bg-blue-600 text-white px-4 py-2 rounded font-bold shadow hover:bg-blue-700 transition">
              + {{ t('plans.create') }}
          </router-link>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-12">
          <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p class="text-gray-500">{{ t('common.loading') }}</p>
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 p-6 rounded text-center">
          <p class="font-bold mb-2">{{ t('cv.error') }}</p>
          <p class="text-sm">{{ error }}</p>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="templates.length === 0" class="bg-gray-50 p-8 text-center rounded">
          <p class="text-gray-500 mb-4">{{ t('cv.no_cvs') }}</p>
          <router-link to="/templates/create" class="text-blue-600 hover:underline font-bold">{{ t('plans.create') }}</router-link>
      </div>
      
      <!-- Template Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div v-for="tmpl in templates" :key="tmpl.id" class="bg-white p-4 border rounded shadow hover:shadow-md transition border-gray-100">
              <div class="h-44 bg-gray-50 flex items-center justify-center mb-4 overflow-hidden relative group rounded border border-gray-100">
                  <img v-if="tmpl.thumbnailUrl" :src="getImageUrl(tmpl.thumbnailUrl)" alt="Preview" class="w-full h-full object-cover" />
                  <span v-else class="text-gray-400 font-medium italic">{{ t('common.noData') }}</span>
                  
                  <!-- Hover Overlay for Preview -->
                  <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-5 transition-all flex items-center justify-center">
                  </div>
              </div>
              <h3 class="font-bold text-lg text-gray-800 mb-1">{{ tmpl.name || t('cv.untitled') }}</h3>
              <p class="text-xs text-gray-500 mb-2 uppercase font-bold">{{ t('table.col_plan') }}: {{ tmpl.planRequired || 'FREE' }}</p>
              <p class="text-[10px] mb-4 font-black uppercase tracking-wider" :class="tmpl.isEnabled !== false ? 'text-green-600' : 'text-red-600'">
                  {{ tmpl.isEnabled !== false ? t('status.ACTIVE') : t('status.LOCKED') }}
              </p>
              
              <div class="flex gap-2 flex-wrap pt-3 border-t border-gray-50">
                  <router-link :to="`/templates/${tmpl.id}`" class="text-blue-600 hover:underline text-xs font-bold uppercase">{{ t('subscriptions.preview') }}</router-link>
                  <button v-if="tmpl.isEnabled !== false" @click="disableTemplate(tmpl.id)" class="text-yellow-600 hover:underline text-xs font-bold uppercase">{{ t('plans.deactivate') }}</button>
                  <button v-else @click="enableTemplate(tmpl.id)" class="text-green-600 hover:underline text-xs font-bold uppercase">{{ t('plans.enable') }}</button>
                  <button @click="deleteTemplate(tmpl.id)" class="text-red-600 hover:underline text-xs font-bold uppercase ml-auto">Delete</button>
              </div>
          </div>
      </div>
  </div>
</template>
