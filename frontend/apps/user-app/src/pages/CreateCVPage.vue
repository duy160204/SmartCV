<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';

import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const auth = useAuthStore();
const templates = ref<any[]>([]);
const favorites = ref<any[]>([]);
const isLoading = ref(true);
const selectedTemplateId = ref<number | null>(null);
const title = ref('Untitled CV');
const showFavoritesOnly = ref(false);

onMounted(async () => {
    try {
        const [templatesRes, favoritesRes] = await Promise.all([
            api.get('/templates'),
            api.get('/cv/favorites')
        ]);
        templates.value = templatesRes.data;
        favorites.value = favoritesRes.data;
    } catch (e) {
        console.error(e);
    } finally {
        isLoading.value = false;
    }
});

const isFavorite = (templateId: number) => {
    return favorites.value.some(f => f.templateId === templateId);
};

const toggleFavorite = async (templateId: number, event: Event) => {
    event.stopPropagation();
    try {
        if (isFavorite(templateId)) {
            await api.delete(`/cv/template/${templateId}/favorite`);
            favorites.value = favorites.value.filter(f => f.templateId !== templateId);
        } else {
            await api.post(`/cv/template/${templateId}/favorite`);
            favorites.value.push({ templateId });
        }
    } catch (e: any) {
        alert('Failed to update favorite: ' + (e.response?.data?.message || e.message));
    }
};

const displayedTemplates = computed(() => {
    if (showFavoritesOnly.value) {
        const favIds = favorites.value.map(f => f.templateId);
        return templates.value.filter(t => favIds.includes(t.id));
    }
    return templates.value;
});

// Helper to check if template is locked for current user
const isLocked = (tmpl: any) => {
    const userPlan = auth.user?.plan || 'FREE';
    if (tmpl.planRequired === 'FREE') return false;
    if (userPlan === 'PREMIUM') return false;
    if (userPlan === 'PRO' && tmpl.planRequired === 'PRO') return false;
    return true; // e.g. Free user trying Pro template
};

const selectTemplate = (tmpl: any) => {
    if (isLocked(tmpl)) {
        // Optional: Trigger upgrade modal here if we had one
        return;
    }
    selectedTemplateId.value = tmpl.id;
};

const createCV = async () => {
    if (!selectedTemplateId.value) return;
    
    try {
        const res = await api.post('/cv', {
            title: title.value,
            templateId: selectedTemplateId.value,
            content: { profile: { name: "", summary: "" }, education: [], experience: [], skills: [] }
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

          <div class="flex justify-between items-center mb-4">
              <h2 class="text-xl font-bold">Select a Template</h2>
              <label class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="checkbox" v-model="showFavoritesOnly" class="form-checkbox" />
                  <span>Show Favorites Only</span>
              </label>
          </div>
          
          <div v-if="isLoading">Loading templates...</div>
          
          <div v-else class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
              <div 
                v-for="tmpl in displayedTemplates" 
                :key="tmpl.id"
                @click="selectTemplate(tmpl)"
                :class="[
                    'border-2 rounded p-4 transition relative', 
                    isLocked(tmpl) ? 'opacity-70 cursor-not-allowed bg-gray-50' : 'cursor-pointer',
                    selectedTemplateId === tmpl.id ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:border-gray-300 bg-white'
                ]"
              >
                  <!-- Favorite Heart Button -->
                  <button 
                    v-if="!isLocked(tmpl)"
                    @click="toggleFavorite(tmpl.id, $event)" 
                    class="absolute top-2 left-2 z-10 text-2xl transition"
                    :class="isFavorite(tmpl.id) ? 'text-red-500' : 'text-gray-300 hover:text-red-300'"
                  >
                      {{ isFavorite(tmpl.id) ? '❤️' : '🤍' }}
                  </button>
                  
                  <div class="aspect-[210/297] bg-gray-200 mb-4 rounded overflow-hidden relative">
                      <img v-if="tmpl.thumbnailUrl" :src="tmpl.thumbnailUrl" class="w-full h-full object-cover">
                      <div v-else class="flex items-center justify-center h-full text-gray-400">Preview</div>

                      <div v-if="tmpl.planRequired !== 'FREE'" class="absolute top-2 right-2 bg-yellow-400 text-yellow-900 text-xs font-bold px-2 py-1 rounded shadow-sm z-20">
                          {{ tmpl.planRequired }}
                          <span v-if="isLocked(tmpl)" class="ml-1">🔒</span>
                      </div>
                      
                      <!-- Lock Overlay -->
                      <div v-if="isLocked(tmpl)" class="absolute inset-0 bg-gray-900 bg-opacity-10 flex items-center justify-center z-10">
                          <div class="bg-white/90 px-3 py-1 rounded text-xs font-bold text-gray-700 shadow">
                              Upgrade to Unlock
                          </div>
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
