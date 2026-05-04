<script setup lang="ts">

import { ref, onMounted, computed } from 'vue';
import { adminCVApi } from '@/api/admin.api';
import CVRenderer from '@/components/core/CVRenderer.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const cvs = ref<any[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// Modal State
const showModal = ref(false);
const selectedCV = ref<any>(null);
const isModalLoading = ref(false);
const triggerLoading = ref(false);

const loadCVs = async () => {
    try {
        isLoading.value = true;
        error.value = null;
        const res = await adminCVApi.getAll();
        cvs.value = res.data.data || [];
    } catch (e: any) {
        console.error(e);
        error.value = e.response?.data?.message || e.message || 'Failed to load CVs';
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    loadCVs();
});

const lockCV = async (id: number) => {
    if (triggerLoading.value) return;
    triggerLoading.value = true;
    
    console.log('[FE][FORCE] LOCK CV CLICKED', id);
    console.log('[FE][FORCE] ABOUT TO CALL API');
    debugger;

    await adminCVApi.lock(id, 'ADMIN_FORCE_LOCK');

    console.log('[FE][FORCE] API CALL FINISHED');
    await loadCVs();
    triggerLoading.value = false;
};

const unlockCV = async (id: number) => {
    if (triggerLoading.value) return;
    triggerLoading.value = true;

    console.log('[FE][FORCE] UNLOCK CV CLICKED', id);
    console.log('[FE][FORCE] ABOUT TO CALL API');
    debugger;

    await adminCVApi.unlock(id, 'ADMIN_FORCE_UNLOCK');

    console.log('[FE][FORCE] API CALL FINISHED');
    await loadCVs();
    triggerLoading.value = false;
};

const deleteCV = async (id: number) => {
    if (triggerLoading.value) return;
    triggerLoading.value = true;

    console.log('[FE][FORCE] DELETE CV CLICKED', id);
    console.log('[FE][FORCE] ABOUT TO CALL API');
    debugger;

    await adminCVApi.delete(id, 'ADMIN_FORCE_DELETE');

    console.log('[FE][FORCE] API CALL FINISHED');
    // Force wait for DB propagation if needed, but loadCVs is usually enough
    await loadCVs();
    triggerLoading.value = false;
};

const viewDetail = async (id: number) => {
    showModal.value = true;
    selectedCV.value = null;
    isModalLoading.value = true;
    try {
        const res = await adminCVApi.getById(id);
        selectedCV.value = res.data.data;
    } catch (e: any) {
        alert('Failed to load detail: ' + (e.response?.data?.message || e.message));
        showModal.value = false;
    } finally {
        isModalLoading.value = false;
    }
};

const closeModal = () => {
    showModal.value = false;
    selectedCV.value = null;
};

const safeParse = (content: string | null) => {
    try {
        return content ? JSON.parse(content) : {};
    } catch (e) {
        return {};
    }
};
</script>

<template>
  <div class="p-8">
      <h1 class="text-2xl font-bold mb-6">{{ t('cv.title') }}</h1>
      
      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-12">
          <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p class="text-gray-500">{{ t('cv.loading') }}</p>
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 p-6 rounded text-center">
          <p class="font-bold mb-2">{{ t('cv.error') }}</p>
          <p class="text-sm">{{ error }}</p>
          <button @click="loadCVs" class="mt-4 text-blue-600 underline">{{ t('common.retry') }}</button>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="cvs.length === 0" class="bg-gray-50 p-8 text-center rounded">
          <p class="text-gray-500">{{ t('cv.no_cvs') }}</p>
      </div>
      
      <!-- Data Table -->
      <div v-else class="bg-white rounded shadow text-sm">
           <table class="w-full text-left border-collapse">
              <thead>
                  <tr class="border-b bg-gray-50 text-xs text-gray-500 uppercase">
                      <th class="p-4">{{ t('table.col_id') }}</th>
                      <th class="p-4">{{ t('table.col_title') }}</th>
                      <th class="p-4">{{ t('table.col_owner') }}</th>
                      <th class="p-4">{{ t('table.col_status') }}</th>
                      <th class="p-4">{{ t('table.col_date') }}</th>
                      <th class="p-4">{{ t('table.col_actions') }}</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="cv in cvs" :key="cv.id" class="border-b hover:bg-gray-50">
                      <td class="p-4">{{ cv.id }}</td>
                      <td class="p-4 font-medium">{{ cv.title || t('cv.untitled') }}</td>
                      <td class="p-4 text-gray-600">{{ cv.ownerEmail || cv.userId }}</td>
                      <td class="p-4">
                          <span class="px-2 py-1 rounded text-[10px] font-bold" 
                                :class="cv.isLocked ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'">
                              {{ cv.isLocked ? t('status.LOCKED') : t('status.ACTIVE') }}
                          </span>
                      </td>
                      <td class="p-4 text-gray-500">{{ cv.createdAt ? new Date(cv.createdAt).toLocaleDateString() : '-' }}</td>
                      <td class="p-4 space-x-2">
                          <button type="button" @click="viewDetail(cv.id)" class="text-blue-600 hover:underline">{{ t('subscriptions.preview') }}</button>
                          
                          <button type="button" v-if="!cv.isLocked" @click="lockCV(cv.id)" :disabled="triggerLoading" class="text-yellow-600 hover:underline disabled:opacity-50">{{ t('users.lock') }}</button>
                          <button type="button" v-else @click="unlockCV(cv.id)" :disabled="triggerLoading" class="text-green-600 hover:underline disabled:opacity-50">{{ t('users.unlock') }}</button>
                          
                          <button type="button" @click="deleteCV(cv.id)" :disabled="triggerLoading" class="text-red-500 hover:underline disabled:opacity-50">Delete</button>
                      </td>
                  </tr>
              </tbody>
          </table>
      </div>
      
      <!-- Detail Modal -->
      <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center p-4 z-50">
          <div class="bg-white rounded shadow-lg w-full max-w-5xl h-[90vh] flex flex-col overflow-hidden">
              <div class="p-4 border-b flex justify-between items-center bg-gray-50">
                  <h2 class="text-xl font-bold text-gray-800">{{ t('cv.detail_title') }}</h2>
                  <button @click="closeModal" class="text-gray-500 hover:text-gray-700 text-2xl">&times;</button>
              </div>
              
              <div class="p-6 overflow-y-auto flex-1 bg-white">
                  <div v-if="isModalLoading" class="text-center py-20">
                      <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto"></div>
                  </div>
                  
                  <div v-else-if="selectedCV" class="h-full flex flex-col">
                      <div class="grid grid-cols-4 gap-6 mb-8 border-b pb-6">
                          <div>
                              <label class="block text-[10px] uppercase font-bold text-gray-400 mb-1">{{ t('table.col_title') }}</label>
                              <div class="font-bold text-gray-800">{{ selectedCV.title || t('cv.untitled') }}</div>
                          </div>
                          <div>
                              <label class="block text-[10px] uppercase font-bold text-gray-400 mb-1">{{ t('table.col_id_template') }}</label>
                              <div class="font-mono text-xs text-gray-600">{{ selectedCV.id }} / {{ selectedCV.templateId }}</div>
                          </div>
                          <div>
                              <label class="block text-[10px] uppercase font-bold text-gray-400 mb-1">{{ t('table.col_user_id') }}</label>
                              <div class="font-mono text-xs text-gray-600">#{{ selectedCV.userId }}</div>
                          </div>
                          <div>
                              <label class="block text-[10px] uppercase font-bold text-gray-400 mb-1">{{ t('table.col_status') }}</label>
                              <span class="px-2 py-1 rounded text-[10px] font-bold" 
                                    :class="selectedCV.isLocked ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'">
                                  {{ selectedCV.isLocked ? t('status.LOCKED') : t('status.ACTIVE') }}
                              </span>
                          </div>
                      </div>
                      
                      <!-- Renderer or JSON Fallback -->
                      <div class="flex-1 overflow-hidden border rounded-lg bg-gray-50 relative shadow-inner">
                          <CVRenderer 
                             v-if="selectedCV.templateHtml"
                             :html="selectedCV.templateHtml"
                             :css="selectedCV.templateCss || ''"
                             :data="safeParse(selectedCV.content)"
                             class="h-full w-full"
                          />
                          <div v-else class="p-8 h-full flex flex-col">
                              <label class="block text-xs text-gray-500 mb-4 font-bold uppercase italic">Raw JSON Content (Template Missing)</label>
                              <pre class="bg-gray-900 text-green-400 p-6 rounded-lg text-xs overflow-auto flex-1 font-mono shadow-xl border border-gray-800">{{ JSON.stringify(safeParse(selectedCV.content), null, 2) }}</pre>
                          </div>
                      </div>
                  </div>
              </div>
              
              <div class="p-4 border-t text-right bg-gray-50">
                  <button @click="closeModal" class="bg-white border border-gray-300 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-100 font-bold shadow-sm transition-all">
                      {{ t('common.noData') === 'Không có dữ liệu' ? 'Đóng' : 'Close' }}
                  </button>
              </div>
          </div>
      </div>
  </div>
</template>
