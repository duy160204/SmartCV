<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import api from '@/api/axios';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserPlanStore } from '@/stores/user-plan.store';

const auth = useAuthStore();
const router = useRouter();
const cvs = ref<any[]>([]);
const isLoading = ref(true);

const planStore = useUserPlanStore();

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
    if (!confirm("Are you sure you want to delete this CV?")) return;
    try {
        await cvApi.delete(id);
        cvs.value = cvs.value.filter(cv => cv.id !== id);
    } catch (e: any) {
        alert("Failed to delete: " + e.message);
    }
};

const goSettings = () => router.push('/settings');
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col font-sans">
      <!-- Navbar -->
      <nav class="bg-white border-b px-8 py-4 flex justify-between items-center sticky top-0 z-10 shadow-sm">
          <h1 class="font-bold text-2xl bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent cursor-pointer" @click="router.push('/dashboard')">SmartCV</h1>
          <div class="flex items-center gap-4">
               <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-100 p-2 rounded transition" @click="goSettings">
                   <img v-if="auth.user?.avatarURL" :src="auth.user.avatarURL" class="w-8 h-8 rounded-full border">
                   <div v-else class="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
                       {{ auth.user?.name?.charAt(0) || 'U' }}
                   </div>
                   <span class="font-medium text-gray-700">{{ auth.user?.name }}</span>
               </div>
               <button @click="auth.logout()" class="text-sm text-gray-500 hover:text-red-500 transition border-l pl-4">Logout</button>
          </div>
      </nav>

      <main class="flex-1 p-8 max-w-6xl mx-auto w-full">
          <!-- Header Actions -->
          <div class="flex justify-between items-center mb-10">
              <div>
                  <h2 class="text-3xl font-bold text-gray-800">My CVs</h2>
                  <p class="text-gray-500 mt-1">Manage and edit your professional resumes</p>
                  <p class="text-xs text-blue-600 mt-1 font-bold" v-if="planStore.currentSubscription">
                      Plan: {{ planStore.currentSubscription.plan }}
                  </p>
              </div>
              <button 
                  @click="createCV" 
                  class="px-6 py-3 rounded-lg font-bold shadow transition flex items-center gap-2 bg-blue-600 text-white hover:bg-blue-700"
              >
                  <span>+ Create New CV</span>
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
              <h3 class="text-2xl font-bold text-gray-800 mb-2">You haven't created any CVs yet.</h3>
              <p class="text-gray-500 mb-8 max-w-md mx-auto">Start by choosing a professional template and let our AI help you write your perfect resume.</p>
              <button @click="createCV" class="bg-blue-600 text-white px-8 py-3 rounded-lg font-bold hover:bg-blue-700 transition">Start Now</button>
          </div>
          
          <!-- Grid -->
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              <div v-for="cv in cvs" :key="cv.id" class="group bg-white rounded-xl shadow-sm border hover:shadow-xl transition-all duration-300 flex flex-col overflow-hidden">
                  <!-- Thumbnail Placeholder -->
                  <div class="h-48 bg-gray-100 relative overflow-hidden group-hover:scale-105 transition duration-500">
                      <div class="absolute inset-0 flex items-center justify-center text-gray-300">
                           <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path></svg>
                      </div>
                      <div class="absolute top-2 right-2 flex gap-1 actions opacity-0 group-hover:opacity-100 transition">
                          <!-- Overlay Actions could go here -->
                      </div>
                  </div>

                  <!-- Content -->
                  <div class="p-5 flex-1 flex flex-col">
                      <h3 class="font-bold text-lg text-gray-800 mb-1 truncate" :title="cv.title">{{ cv.title }}</h3>
                      <p class="text-xs text-gray-500 mb-4">Last updated: {{ new Date(cv.updatedAt || Date.now()).toLocaleDateString() }}</p>
                      
                      <div class="mt-auto grid grid-cols-2 gap-3">
                           <router-link :to="`/cv/editor/${cv.id}`" class="col-span-2 text-center bg-blue-50 text-blue-600 font-bold py-2 rounded-lg hover:bg-blue-100 transition border border-blue-100">
                               Edit
                           </router-link>
                           <!-- Secondary Actions -->
                           <!-- We could add Share/Preview here later -->
                           <button @click="deleteCV(cv.id)" class="text-gray-400 hover:text-red-500 text-sm py-2">Delete</button>
                      </div>
                  </div>
              </div>
          </div>
      </main>
  </div>
</template>
