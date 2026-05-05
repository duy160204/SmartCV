<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import api from '@/api/axios';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserPlanStore } from '@/stores/user-plan.store';
import { useI18n } from 'vue-i18n';
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue';

const { t } = useI18n();
const auth = useAuthStore();
const router = useRouter();
const cvs = ref<any[]>([]);
const isLoading = ref(true);

const planStore = useUserPlanStore();

const resolveThumbnail = (cv: any) => {
    return cv.templateThumbnailUrl || '/images/cv-placeholder.png';
};

const handleImageError = (e: Event) => {
    const img = e.target as HTMLImageElement;
    img.onerror = null;
    img.src = '/images/cv-placeholder.png';
};

onMounted(async () => {
    try {
        await Promise.all([
            loadCVs(),
            planStore.init() // Ensure plan data is fresh (though strictly not needed for list)
        ]);
    } finally {
        isLoading.value = false;
    }
});

const loadCVs = async () => {
    try {
        const res = await api.get('/cv'); 
        cvs.value = res.data;
    } catch (e) {
        console.error(e);
    }
};

const createCV = () => {
    // We do NOT block in frontend anymore. We let the backend decide.
    router.push('/cv/create');
};

import { cvApi } from '@/api/user.api';

const deleteCV = async (id: number) => {
    if (!confirm(t('common.confirm_delete'))) return;
    try {
        await cvApi.delete(id);
        cvs.value = cvs.value.filter(cv => cv.id !== id);
    } catch (e: any) {
        alert(t('common.delete_failed') + ": " + e.message);
    }
};

const goSettings = () => router.push('/settings');
</script>

<template>
  <div class="flex-1 p-8 max-w-6xl mx-auto w-full">
          <!-- Header Actions -->
          <div class="flex justify-between items-center mb-10">
              <div>
                  <h2 class="text-3xl font-bold text-gray-800">{{ t('dashboard.title') }}</h2>
                  <p class="text-gray-500 mt-1">{{ t('dashboard.subtitle') }}</p>
                  <p class="text-xs text-blue-600 mt-1 font-bold" v-if="planStore.currentSubscription">
                      {{ t('dashboard.plan_label') }}: {{ planStore.currentSubscription.plan }}
                  </p>
              </div>
              <button 
                  @click="createCV" 
                  class="px-6 py-3 rounded-lg font-bold shadow transition flex items-center gap-2 bg-blue-600 text-white hover:bg-blue-700"
              >
                  <span>{{ t('dashboard.create_new') }}</span>
              </button>
          </div>

          <!-- Loading State -->
          <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-3 gap-6 animate-pulse">
              <div class="h-64 bg-gray-200 rounded-xl"></div>
              <div class="h-64 bg-gray-200 rounded-xl"></div>
              <div class="h-64 bg-gray-200 rounded-xl"></div>
          </div>
          
          <!-- Empty State -->
          <div v-else-if="cvs.length === 0" class="text-center py-24 bg-white rounded-2xl shadow-sm border border-gray-100">
              <div class="text-6xl mb-6">📄</div>
              <h3 class="text-2xl font-bold text-gray-800 mb-2">{{ t('dashboard.empty_title') }}</h3>
              <p class="text-gray-500 mb-8 max-w-md mx-auto">{{ t('dashboard.empty_desc') }}</p>
              <button @click="createCV" class="bg-blue-600 text-white px-8 py-3 rounded-lg font-bold hover:bg-blue-700 transition">{{ t('dashboard.start_now') }}</button>
          </div>
          
          <!-- Grid -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              <div v-for="cv in cvs" :key="cv.id" class="group bg-white rounded-xl shadow-sm border hover:shadow-xl transition-all duration-300 flex flex-col overflow-hidden">
                  <!-- Thumbnail Container -->
                  <div class="aspect-[210/297] bg-slate-50 relative overflow-hidden border-b border-slate-100 flex-shrink-0">
                      <img 
                          :src="resolveThumbnail(cv)" 
                          @error="handleImageError"
                          loading="lazy"
                          class="w-full h-full object-cover group-hover:scale-105 transition duration-500" 
                          alt="CV Preview"
                      />
                      <div class="absolute top-2 right-2 flex gap-1 actions opacity-0 group-hover:opacity-100 transition">
                          <!-- Overlay Actions could go here -->
                      </div>
                  </div>

                  <!-- Content -->
                  <div class="p-5 flex-1 flex flex-col">
                      <h3 class="font-bold text-lg text-gray-800 mb-1 truncate" :title="cv.title">{{ cv.title }}</h3>
                      <p class="text-xs text-gray-500 mb-4">{{ t('dashboard.last_updated') }}: {{ new Date(cv.updatedAt || Date.now()).toLocaleDateString() }}</p>
                      
                      <div class="mt-auto grid grid-cols-2 gap-3">
                           <router-link :to="`/cv/editor/${cv.id}`" class="col-span-2 text-center bg-blue-50 text-blue-600 font-bold py-2 rounded-lg hover:bg-blue-100 transition border border-blue-100">
                               {{ t('common.edit') }}
                           </router-link>
                           <button @click="deleteCV(cv.id)" class="col-span-2 text-gray-400 hover:text-red-500 text-sm py-2 hover:bg-red-50 rounded-lg transition">{{ t('common.delete') }}</button>
                      </div>
                  </div>
              </div>
          </div>
      </div>
  </div>
</template>
