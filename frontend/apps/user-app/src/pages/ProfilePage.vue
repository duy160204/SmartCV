<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import api from '@/api/axios';
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserPlanStore } from '@/stores/user-plan.store';
import { cvApi } from '@/api/user.api';

const auth = useAuthStore();
const planStore = useUserPlanStore();
const router = useRouter();
const route = useRoute(); // Add useRoute
const activeTab = ref('cvs'); // 'cvs', 'account', 'subscription'

const userCVs = ref<any[]>([]);
const isLoadingCVs = ref(false);

const loadCVs = async () => {
    isLoadingCVs.value = true;
    try {
        const res = await cvApi.getAll();
        userCVs.value = res.data;
    } catch (e) {
        console.error("Failed to load CVs", e);
    } finally {
        isLoadingCVs.value = false;
    }
};

const deleteCV = async (id: number) => {
    if (!confirm("Are you sure you want to delete this CV?")) return;
    try {
        await cvApi.delete(id);
        userCVs.value = userCVs.value.filter(cv => cv.id !== id);
    } catch (e: any) {
        alert("Failed to delete: " + e.message);
    }
};

const openCV = (id: number) => {
    router.push(`/cv/editor/${id}`);
};

const formatDate = (dateStr: string) => {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString();
};

const formatCurrency = (val: number, currency: string) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: currency }).format(val);
};

onMounted(async () => {
    loadCVs();
    await planStore.init();
});
</script>

<template>
  <div class="min-h-screen bg-gray-50">
      <nav class="bg-white border-b px-8 py-4 flex justify-between items-center shadow-sm sticky top-0 z-20">
          <h1 class="font-bold text-xl"><router-link to="/">SmartCV</router-link></h1>
          <div class="flex items-center gap-4">
               <span class="font-medium text-gray-700">{{ auth.user?.name }}</span>
               <button @click="auth.logout()" class="text-sm text-red-600 hover:text-red-800">Logout</button>
          </div>
      </nav>

      <div class="max-w-6xl mx-auto mt-8 p-6">
          <h2 class="text-3xl font-bold mb-8 text-gray-800">My Workspace</h2>
          
          <div class="flex flex-col md:flex-row gap-8">
              <!-- Sidebar / Tabs -->
              <div class="md:w-64 flex-shrink-0">
                  <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                      <button 
                        @click="activeTab = 'cvs'"
                        :class="['w-full text-left px-6 py-4 font-medium transition flex items-center gap-3', activeTab === 'cvs' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-gray-600 hover:bg-gray-50']"
                      >
                          <span>📄</span> My CVs
                      </button>
                      <button 
                        @click="activeTab = 'account'"
                        :class="['w-full text-left px-6 py-4 font-medium transition flex items-center gap-3', activeTab === 'account' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-gray-600 hover:bg-gray-50']"
                      >
                          <span>👤</span> Account
                      </button>
                      <button 
                        @click="activeTab = 'subscription'"
                        :class="['w-full text-left px-6 py-4 font-medium transition flex items-center gap-3', activeTab === 'subscription' ? 'bg-blue-50 text-blue-700 border-l-4 border-blue-600' : 'text-gray-600 hover:bg-gray-50']"
                      >
                          <span>💎</span> Subscription
                      </button>
                  </div>
              </div>

              <!-- Main Content -->
              <div class="flex-1">
                  <!-- My CVs Tab -->
                  <div v-if="activeTab === 'cvs'" class="space-y-6">
                      <div class="flex justify-between items-center mb-4">
                          <h3 class="text-xl font-bold text-gray-800">My Resumes</h3>
                          <router-link to="/" class="bg-blue-600 text-white px-4 py-2 rounded-lg font-bold hover:bg-blue-700 transition shadow text-sm">
                              + Create New
                          </router-link>
                      </div>

                      <div v-if="isLoadingCVs" class="text-center py-12 text-gray-500">Loading CVs...</div>
                      
                      <div v-else-if="userCVs.length === 0" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
                          <div class="text-4xl mb-4">📝</div>
                          <h3 class="text-lg font-bold text-gray-800 mb-2">No CVs yet</h3>
                          <p class="text-gray-500 mb-6">Create your first professional resume in minutes.</p>
                          <router-link to="/" class="text-blue-600 font-bold hover:underline">Pick a Template</router-link>
                      </div>

                      <div v-else class="grid gap-4">
                          <div v-for="cv in userCVs" :key="cv.id" class="bg-white p-5 rounded-xl shadow-sm border border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 hover:shadow-md transition">
                              <div class="flex items-center gap-4">
                                  <div class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center text-xl">📄</div>
                                  <div>
                                      <h4 class="font-bold text-gray-800 text-lg">{{ cv.title || 'Untitled Resume' }}</h4>
                                      <p class="text-xs text-gray-500">Updated: {{ formatDate(cv.updatedAt) }}</p>
                                  </div>
                              </div>
                              
                              <div class="flex items-center gap-3 w-full sm:w-auto">
                                  <button @click="openCV(cv.id)" class="flex-1 sm:flex-none px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-medium hover:bg-gray-200 transition">
                                      Edit
                                  </button>
                                  <button @click="deleteCV(cv.id)" class="px-3 py-2 text-red-500 hover:bg-red-50 rounded-lg transition" title="Delete CV">
                                      🗑️
                                  </button>
                              </div>
                          </div>
                      </div>
                  </div>

                  <!-- Account Tab -->
                  <div v-if="activeTab === 'account'" class="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
                       <h3 class="text-xl font-bold text-gray-800 mb-6">Account Information</h3>
                       
                       <div class="space-y-6 max-w-lg">
                           <div>
                               <label class="block text-sm font-medium text-gray-500 mb-1">Full Name</label>
                               <div class="p-3 bg-gray-50 rounded-lg border border-gray-200 text-gray-800 font-medium">
                                   {{ auth.user?.name }}
                               </div>
                           </div>
                           
                           <div>
                               <label class="block text-sm font-medium text-gray-500 mb-1">Email Address</label>
                               <div class="p-3 bg-gray-50 rounded-lg border border-gray-200 text-gray-800 font-medium flex justify-between items-center">
                                   <span>{{ auth.user?.email }}</span>
                                   <span v-if="auth.user?.isVerified" class="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full font-bold">Verified</span>
                                   <span v-else class="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full font-bold">Unverified</span>
                               </div>
                               <!-- OAuth Badge -->
                               <div class="mt-2 text-xs text-gray-400">
                                   Logged in via {{ auth.user?.role === 'ADMIN' ? 'Admin Access' : 'Standard Access' }}
                                   <!-- Real OAuth provider info would come from user object if available -->
                               </div>
                           </div>
                       </div>
                  </div>

                  <!-- Subscription Tab -->
                  <div v-if="activeTab === 'subscription'" class="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
                       <h3 class="text-xl font-bold text-gray-800 mb-6">Subscription Plan</h3>
                       
                       <div v-if="planStore.isLoading">Loading details...</div>
                       <div v-else-if="planStore.currentSubscription">
                           <!-- Active Plan Card -->
                           <div class="bg-gradient-to-br from-indigo-600 to-blue-700 text-white p-6 rounded-2xl shadow-lg mb-8 relative overflow-hidden">
                               <div class="relative z-10">
                                   <div class="uppercase text-xs font-bold opacity-70 mb-1 tracking-wider">Current Plan</div>
                                   <div class="text-4xl font-extrabold mb-4">{{ planStore.currentSubscription.plan }}</div>
                                   
                                   <div class="flex gap-8">
                                       <div>
                                           <div class="text-2xl font-bold">{{ planStore.currentSubscription.cvCount }} / {{ planStore.currentSubscription.maxCVs === -1 ? '∞' : planStore.currentSubscription.maxCVs }}</div>
                                           <div class="text-xs opacity-70">CVs Created</div>
                                       </div>
                                       <div>
                                           <div class="text-2xl font-bold">{{ planStore.currentSubscription.status }}</div>
                                           <div class="text-xs opacity-70">Status</div>
                                       </div>
                                   </div>
                               </div>
                               <!-- Decorative circle -->
                               <div class="absolute -right-10 -bottom-10 w-48 h-48 bg-white opacity-10 rounded-full blur-2xl"></div>
                           </div>
                           
                           <!-- Available Plans (View Only) -->
                           <h4 class="font-bold text-gray-800 mb-4">Available Plans</h4>
                           <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                               <div 
                                    v-for="plan in planStore.plans" 
                                    :key="plan.code"
                                    class="p-4 border rounded-lg flex justify-between items-center opacity-75 grayscale hover:grayscale-0 hover:opacity-100 transition"
                                >
                                    <div>
                                        <div class="font-bold text-gray-800">{{ plan.name }}</div>
                                        <div class="text-sm text-gray-500">{{ formatCurrency(plan.price, plan.currency) }} / {{ plan.durationMonths }}mo</div>
                                    </div>
                                    <div class="text-xs bg-gray-100 px-2 py-1 rounded">View Only</div>
                                </div>
                           </div>
                           <p class="text-xs text-gray-400 mt-4 italic">* Upgrades are currently disabled in this view.</p>
                       </div>
                  </div>
              </div>
          </div>
      </div>
  </div>
</template>
