<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';

const router = useRouter();
const templates = ref<any[]>([]);
const isLoading = ref(true);
const selectedTemplateId = ref<number | null>(null);
const title = ref('Untitled CV');

onMounted(async () => {
    try {
        const res = await api.get('/templates');
        templates.value = res.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
});

const createCV = async () => {
    if (!selectedTemplateId.value) return;
    
    try {
        const res = await api.post('/cv', {
            title: title.value,
            templateId: selectedTemplateId.value,
            content: { profile: { name: "", summary: "" }, education: [], experience: [], skills: [] } // Init empty structure
        });
        router.push(`/cv/editor/${res.data.id}`);
    } catch (e: any) {
        alert("Failed to create: " + (e.response?.data?.message || e.message));
    }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 p-8">
      <div class="max-w-6xl mx-auto">
          <div class="flex items-center mb-8">
              <router-link to="/" class="text-gray-500 mr-4">← Back</router-link>
              <h1 class="text-3xl font-bold">Create New CV</h1>
          </div>

          <div class="bg-white p-6 rounded shadow mb-8">
              <label class="block font-bold mb-2">CV Title</label>
              <input v-model="title" class="w-full border p-2 rounded text-lg" placeholder="e.g. Software Engineer 2024" />
          </div>

          <h2 class="text-xl font-bold mb-4">Select a Template</h2>
          
          <div v-if="isLoading">Loading templates...</div>
          
          <div v-else class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
              <div 
                v-for="tmpl in templates" 
                :key="tmpl.id"
                @click="selectedTemplateId = tmpl.id"
                :class="['border-2 rounded p-4 cursor-pointer transition relative', selectedTemplateId === tmpl.id ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-gray-300 bg-white']"
              >
                  <div class="aspect-[210/297] bg-gray-200 mb-4 rounded overflow-hidden relative">
                      <img v-if="tmpl.thumbnailUrl" :src="tmpl.thumbnailUrl" class="w-full h-full object-cover">
                      <div v-else class="flex items-center justify-center h-full text-gray-400">Preview</div>

                      <div v-if="tmpl.planRequired !== 'FREE'" class="absolute top-2 right-2 bg-yellow-400 text-yellow-900 text-xs font-bold px-2 py-1 rounded">
                          {{ tmpl.planRequired }}
                      </div>
                  </div>
                  <h3 class="font-bold text-center">{{ tmpl.name }}</h3>
              </div>
          </div>

          <div class="fixed bottom-0 left-0 right-0 p-4 bg-white border-t flex justify-between items-center px-10 shadow-lg">
             <div class="text-gray-500">
                 Selected: <span v-if="selectedTemplateId" class="font-bold text-blue-600">Template #{{ selectedTemplateId }}</span><span v-else>None</span>
             </div>
             <button 
                @click="createCV" 
                :disabled="!selectedTemplateId"
                :class="['px-8 py-3 rounded font-bold text-white', selectedTemplateId ? 'bg-blue-600 hover:bg-blue-700' : 'bg-gray-300 cursor-not-allowed']"
             >
                 Create CV
             </button>
          </div>
      </div>
  </div>
</template>
