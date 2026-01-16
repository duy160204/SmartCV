<script setup lang="ts">

import { ref, onMounted, computed } from 'vue';
import { adminCVApi } from '@/api/admin.api';
import CVRenderer from '@/components/core/CVRenderer.vue';

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
      <h1 class="text-2xl font-bold mb-6">CV Management</h1>
      
      <!-- Loading State -->
      <div v-if="isLoading" class="text-center py-12">
          <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto mb-4"></div>
          <p class="text-gray-500">Loading CVs...</p>
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 text-red-700 p-6 rounded text-center">
          <p class="font-bold mb-2">Failed to load CVs</p>
          <p class="text-sm">{{ error }}</p>
          <button @click="loadCVs" class="mt-4 text-blue-600 underline">Retry</button>
      </div>
      
      <!-- Empty State -->
      <div v-else-if="cvs.length === 0" class="bg-gray-50 p-8 text-center rounded">
          <p class="text-gray-500">No CVs found</p>
      </div>
      
      <!-- Data Table -->
      <div v-else class="bg-white rounded shadow text-sm">
           <table class="w-full text-left border-collapse">
              <thead>
                  <tr class="border-b bg-gray-50">
                      <th class="p-4">ID</th>
                      <th class="p-4">Title</th>
                      <th class="p-4">Owner Email</th>
                      <th class="p-4">Status</th>
                      <th class="p-4">Created At</th>
                      <th class="p-4">Actions</th>
                  </tr>
              </thead>
              <tbody>
                  <tr v-for="cv in cvs" :key="cv.id" class="border-b hover:bg-gray-50">
                      <td class="p-4">{{ cv.id }}</td>
                      <td class="p-4 font-medium">{{ cv.title || 'Untitled' }}</td>
                      <td class="p-4 text-gray-600">{{ cv.ownerEmail || cv.userId }}</td>
                      <td class="p-4">
                          <span class="px-2 py-1 rounded text-xs" 
                                :class="cv.isLocked ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'">
                              {{ cv.isLocked ? 'LOCKED' : 'ACTIVE' }}
                          </span>
                      </td>
                      <td class="p-4">{{ cv.createdAt ? new Date(cv.createdAt).toLocaleDateString() : '-' }}</td>
                      <td class="p-4 space-x-2">
                          <button type="button" @click="viewDetail(cv.id)" class="text-blue-600 hover:underline">View</button>
                          
                          <button type="button" v-if="!cv.isLocked" @click="lockCV(cv.id)" :disabled="triggerLoading" class="text-yellow-600 hover:underline disabled:opacity-50">Lock</button>
                          <button type="button" v-else @click="unlockCV(cv.id)" :disabled="triggerLoading" class="text-green-600 hover:underline disabled:opacity-50">Unlock</button>
                          
                          <button type="button" @click="deleteCV(cv.id)" :disabled="triggerLoading" class="text-red-500 hover:underline disabled:opacity-50">Delete</button>
                      </td>
                  </tr>
              </tbody>
          </table>
      </div>
      
      <!-- Detail Modal -->
      <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center p-4 z-50">
          <div class="bg-white rounded shadow-lg w-full max-w-5xl h-[90vh] flex flex-col">
              <div class="p-4 border-b flex justify-between items-center">
                  <h2 class="text-xl font-bold">CV Detail</h2>
                  <button @click="closeModal" class="text-gray-500 hover:text-gray-700">&times;</button>
              </div>
              
              <div class="p-6 overflow-y-auto flex-1">
                  <div v-if="isModalLoading" class="text-center py-8">
                      <div class="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full mx-auto"></div>
                  </div>
                  
                  <div v-else-if="selectedCV">
                      <div class="grid grid-cols-2 gap-4 mb-6">
                          <div>
                              <label class="block text-xs text-gray-500">Title</label>
                              <div class="font-medium">{{ selectedCV.title || 'Untitled' }}</div>
                          </div>
                          <div>
                              <label class="block text-xs text-gray-500">ID / Template</label>
                              <div>{{ selectedCV.id }} / {{ selectedCV.templateId }}</div>
                          </div>
                          <div>
                              <label class="block text-xs text-gray-500">User ID</label>
                              <div>{{ selectedCV.userId }}</div>
                          </div>
                          <div>
                              <label class="block text-xs text-gray-500">Status</label>
                              <span class="px-2 py-1 rounded text-xs" 
                                    :class="selectedCV.isLocked ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'">
                                  {{ selectedCV.isLocked ? 'LOCKED' : 'ACTIVE' }}
                              </span>
                          </div>
                      </div>
                      
                      <!-- Renderer or JSON Fallback -->
                      <div class="flex-1 overflow-auto border rounded bg-gray-50 relative">
                          <CVRenderer 
                             v-if="selectedCV.templateHtml"
                             :html="selectedCV.templateHtml"
                             :css="selectedCV.templateCss || ''"
                             :data="safeParse(selectedCV.content)"
                             class="h-full w-full"
                          />
                          <div v-else class="p-4">
                              <label class="block text-xs text-gray-500 mb-2">Raw JSON Content (Template Missing)</label>
                              <pre class="bg-white p-4 rounded text-xs overflow-auto h-full border">{{ JSON.stringify(safeParse(selectedCV.content), null, 2) }}</pre>
                          </div>
                      </div>
                  </div>
              </div>
              
              <div class="p-4 border-t text-right">
                  <button @click="closeModal" class="bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300">Close</button>
              </div>
          </div>
      </div>
  </div>
</template>
