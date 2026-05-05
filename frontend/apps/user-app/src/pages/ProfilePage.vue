<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserPlanStore } from '@/stores/user-plan.store';
import { cvApi } from '@/api/user.api';
import { useI18n } from 'vue-i18n';
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue';

const { t } = useI18n();
const auth = useAuthStore();
const planStore = useUserPlanStore();
const router = useRouter();

const activeTab = ref('cvs');

const resolveThumbnail = (cv: any) => {
    return cv.templateThumbnailUrl || '/images/cv-placeholder.png';
};

const handleImageError = (e: Event) => {
    const img = e.target as HTMLImageElement;
    img.onerror = null;
    img.src = '/images/cv-placeholder.png';
};

const userCVs = ref<any[]>([]);
const isLoadingCVs = ref(false);

const loadCVs = async () => {
  isLoadingCVs.value = true;
  try {
    const res = await cvApi.getAll();
    userCVs.value = res.data;
  } finally {
    isLoadingCVs.value = false;
  }
};

const deleteCV = async (id: number) => {
  if (!confirm(t('common.confirm_delete'))) return;
  await cvApi.delete(id);
  userCVs.value = userCVs.value.filter(cv => cv.id !== id);
};

const openCV = (id: number) => {
  router.push(`/cv/editor/${id}`);
};

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
};

onMounted(async () => {
  loadCVs();
  await planStore.init();
});
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-100 via-white to-blue-100 flex justify-center">

    <div class="w-full max-w-6xl px-6 py-10">

      <!-- TITLE -->
      <div class="text-center mb-6">
        <h2 class="text-3xl font-bold text-slate-800">
          {{ t('profile.workspace') }}
        </h2>
      </div>

      <!-- 🔥 CENTERED TAB -->
      <div class="flex justify-center mb-10 px-4">
        <div class="bg-slate-100 p-1 rounded-full flex overflow-x-auto shadow-inner w-full max-w-2xl gap-1">

          <button
              @click="activeTab = 'cvs'"
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-full transition whitespace-nowrap"
              :class="activeTab === 'cvs' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-700 hover:bg-white hover:text-slate-900'"
          >
            <span class="text-lg">📄</span>
            <span>{{ t('profile.tab_cvs') }}</span>
          </button>

          <button
              @click="activeTab = 'account'"
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-full transition whitespace-nowrap"
              :class="activeTab === 'account' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-700 hover:bg-white hover:text-slate-900'"
          >
            <span class="text-lg">👤</span>
            <span>{{ t('profile.tab_account') }}</span>
          </button>

          <button
              @click="activeTab = 'subscription'"
              class="flex-1 flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium rounded-full transition whitespace-nowrap"
              :class="activeTab === 'subscription' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-700 hover:bg-white hover:text-slate-900'"
          >
            <span class="text-lg">💎</span>
            <span>{{ t('profile.tab_subscription') }}</span>
          </button>

        </div>
      </div>

      <!-- CONTENT CARD -->
      <div class="bg-white border border-slate-200 rounded-2xl shadow-sm p-6">

        <!-- ================= CVs ================= -->
        <div v-if="activeTab === 'cvs'" class="space-y-6">

          <div class="flex justify-between items-center">
            <h3 class="text-xl font-bold text-slate-800">
              {{ t('profile.my_resumes') }}
            </h3>

            <router-link
                to="/"
                class="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition text-sm"
            >
              {{ t('profile.create_new') }}
            </router-link>
          </div>

          <!-- loading -->
          <div v-if="isLoadingCVs" class="text-center py-10 text-slate-400">
            {{ t('profile.loading_cvs') }}
          </div>

          <!-- empty -->
          <div v-else-if="userCVs.length === 0" class="text-center py-14 text-slate-500">
            📝 {{ t('profile.no_cvs') }}
          </div>

          <!-- 🔥 CV GRID LIST -->
          <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">

            <div
                v-for="cv in userCVs"
                :key="cv.id"
                class="bg-white border border-slate-200 rounded-2xl flex flex-col hover:shadow-lg transition duration-300 overflow-hidden group"
            >
              
              <!-- CV Preview Thumbnail Container -->
              <div class="aspect-[210/297] bg-slate-50 relative overflow-hidden border-b border-slate-100 flex-shrink-0">
                  <img 
                      :src="resolveThumbnail(cv)" 
                      @error="handleImageError"
                      loading="lazy"
                      class="w-full h-full object-cover group-hover:scale-105 transition duration-500" 
                      alt="CV Preview"
                  />
              </div>

              <!-- Details & Actions -->
              <div class="p-4 flex flex-col flex-1">
                <div class="font-semibold text-slate-800 truncate" :title="cv.title">
                  {{ cv.title || t('dashboard.untitled') }}
                </div>

                <div class="text-xs text-slate-400 mt-1 mb-4">
                  {{ t('profile.updated') }}: {{ formatDate(cv.updatedAt) }}
                </div>

                <div class="flex gap-2 mt-auto">
                  <button
                      @click="openCV(cv.id)"
                      class="flex-1 bg-blue-50 text-blue-600 font-semibold text-sm py-2 rounded-lg hover:bg-blue-100 transition"
                  >
                    {{ t('common.edit') }}
                  </button>

                  <button
                      @click="deleteCV(cv.id)"
                      class="px-3 py-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition border border-transparent hover:border-red-100"
                      title="Delete CV"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                    </svg>
                  </button>
                </div>
              </div>

            </div>

          </div>

        </div>

        <!-- ================= ACCOUNT ================= -->
        <div v-if="activeTab === 'account'" class="space-y-6 max-w-lg mx-auto">

          <h3 class="text-xl font-bold text-slate-800 text-center">
            {{ t('profile.account_info') }}
          </h3>

          <div>
            <label class="text-sm text-slate-500">{{ t('profile.full_name') }}</label>
            <div class="mt-1 p-3 bg-slate-50 border border-slate-200 rounded-xl">
              {{ auth.user?.name }}
            </div>
          </div>

          <div>
            <label class="text-sm text-slate-500">{{ t('profile.email') }}</label>

            <div class="mt-1 p-3 bg-slate-50 border border-slate-200 rounded-xl flex justify-between items-center">
              <span>{{ auth.user?.email }}</span>

              <span
                  v-if="auth.user?.isVerified"
                  class="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded-full"
              >
                {{ t('profile.verified') }}
              </span>

              <span
                  v-else
                  class="text-xs bg-yellow-100 text-yellow-700 px-2 py-1 rounded-full"
              >
                {{ t('profile.unverified') }}
              </span>
            </div>

          </div>
        </div>

        <!-- ================= SUBSCRIPTION ================= -->
        <div v-if="activeTab === 'subscription'" class="space-y-8 max-w-2xl mx-auto py-4">
          
          <!-- Plan Header -->
          <div class="text-center">
            <div class="inline-block p-3 bg-blue-50 text-blue-600 rounded-2xl mb-4">
              <span class="text-2xl">💎</span>
            </div>
            <h3 class="text-2xl font-bold text-slate-800">
              {{ planStore.currentSubscription?.plan.name || 'Free Plan' }}
            </h3>
            <p class="text-slate-500 mt-1">
              {{ t('profile.current_plan_desc') }}
            </p>
          </div>

          <!-- Plan Information Card -->
          <div class="grid grid-cols-2 gap-4">
            <div class="p-4 bg-white border border-slate-200 rounded-2xl shadow-sm">
               <span class="block text-[10px] uppercase text-slate-400 font-bold mb-1">Plan Type</span>
               <span class="text-sm font-bold text-slate-700 uppercase">{{ planStore.currentSubscription?.plan.code }}</span>
            </div>
            <div class="p-4 bg-white border border-slate-200 rounded-2xl shadow-sm">
               <span class="block text-[10px] uppercase text-slate-400 font-bold mb-1">Daily Limit</span>
               <span class="text-sm font-bold text-slate-700">
                  {{ planStore.isUnlimited ? 'Unlimited' : planStore.currentSubscription?.plan.maxAiRequestsPerDay + ' req/day' }}
               </span>
            </div>
          </div>

          <!-- AI Usage Card -->
          <div class="bg-slate-50 border border-slate-200 rounded-2xl p-6">
            <div class="flex justify-between items-center mb-4">
              <h4 class="font-bold text-slate-700 flex items-center gap-2">
                🤖 AI Usage
                <span v-if="planStore.isUnlimited" class="text-[10px] bg-green-100 text-green-700 px-2 py-0.5 rounded-full uppercase tracking-wider">Unlimited</span>
              </h4>
              <span class="text-sm font-medium text-slate-600">
                <template v-if="planStore.isUnlimited">∞</template>
                <template v-else>
                  {{ planStore.currentSubscription?.usage.usedToday }} / {{ planStore.currentSubscription?.plan.maxAiRequestsPerDay }}
                </template>
              </span>
            </div>

            <!-- Progress Bar -->
            <div class="w-full h-3 bg-slate-200 rounded-full overflow-hidden mb-3">
              <div 
                class="h-full transition-all duration-500" 
                :class="[
                  planStore.usagePercentage < 70 ? 'bg-green-500' :
                  planStore.usagePercentage < 90 ? 'bg-yellow-500' : 'bg-red-500'
                ]"
                :style="{ width: planStore.isUnlimited ? '0%' : planStore.usagePercentage + '%' }"
              ></div>
            </div>

            <div class="flex justify-between text-[11px] text-slate-400">
              <span>{{ t('profile.usage_resets') }}: {{ planStore.currentSubscription?.usage.resetAt }}</span>
              <span v-if="!planStore.isUnlimited">{{ planStore.currentSubscription?.usage.remaining }} {{ t('profile.remaining') }}</span>
            </div>
          </div>

          <!-- Dates & Status -->
          <div class="grid grid-cols-2 gap-4">
            <div class="p-4 border border-slate-100 rounded-xl">
              <label class="text-[10px] uppercase text-slate-400 font-bold block mb-1">Status</label>
              <span class="text-sm font-semibold text-slate-700">{{ planStore.currentSubscription?.subscription.status }}</span>
            </div>
            <div class="p-4 border border-slate-100 rounded-xl">
              <label class="text-[10px] uppercase text-slate-400 font-bold block mb-1">Valid Until</label>
              <span class="text-sm font-semibold text-slate-700">
                {{ planStore.currentSubscription?.subscription.endDate ? formatDate(planStore.currentSubscription?.subscription.endDate) : 'Never' }}
              </span>
            </div>
          </div>

          <!-- CTA -->
          <div class="text-center pt-4">
            <router-link to="/pricing" class="inline-block bg-slate-900 text-white px-8 py-3 rounded-xl font-bold hover:bg-black transition shadow-lg">
              Upgrade Your Experience
            </router-link>
          </div>

        </div>

      </div>

    </div>

  </div>
</template>

<style scoped>
</style>