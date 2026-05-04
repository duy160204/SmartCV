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

      <!-- NAV -->
      <nav class="bg-white border border-slate-200 px-6 py-4 flex justify-between items-center rounded-2xl shadow-sm">
        <div class="flex items-center gap-4">
          <h1 class="font-bold text-xl text-slate-800">
            <router-link to="/">{{ t('nav.brand') }}</router-link>
          </h1>
          <LanguageSwitcher />
        </div>

        <div class="flex items-center gap-4">
          <span class="font-medium text-slate-700">
            {{ auth.user?.name }}
          </span>

          <button
              @click="auth.logout()"
              class="text-sm text-red-500 hover:text-red-600 transition"
          >
            {{ t('common.logout') }}
          </button>
        </div>
      </nav>

      <!-- TITLE -->
      <div class="text-center mt-10 mb-6">
        <h2 class="text-3xl font-bold text-slate-800">
          {{ t('profile.workspace') }}
        </h2>
      </div>

      <!-- 🔥 CENTERED TAB -->
      <div class="flex justify-center mb-10">
        <div class="relative bg-white border border-slate-200 rounded-full p-1 flex shadow-sm">

          <!-- active indicator -->
          <div
              class="absolute top-1 bottom-1 w-1/2 rounded-full bg-blue-600 transition-all duration-300"
              :class="activeTab === 'account' ? 'translate-x-full' : 'translate-x-0'"
          ></div>

          <button
              @click="activeTab = 'cvs'"
              class="relative z-10 px-8 py-2 text-sm font-medium transition"
              :class="activeTab === 'cvs' ? 'text-white' : 'text-slate-600'"
          >
            📄 {{ t('profile.tab_cvs') }}
          </button>

          <button
              @click="activeTab = 'account'"
              class="relative z-10 px-8 py-2 text-sm font-medium transition"
              :class="activeTab === 'account' ? 'text-white' : 'text-slate-600'"
          >
            👤 {{ t('profile.tab_account') }}
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

          <!-- 🔥 HORIZONTAL CV LIST (LIKE TEMPLATES) -->
          <div v-else class="flex gap-4 overflow-x-auto pb-2">

            <div
                v-for="cv in userCVs"
                :key="cv.id"
                class="min-w-[260px] bg-slate-50 border border-slate-200 rounded-2xl p-4 flex flex-col justify-between hover:shadow-md transition"
            >

              <!-- icon + title -->
              <div>
                <div class="w-10 h-10 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center mb-3">
                  📄
                </div>

                <div class="font-semibold text-slate-800">
                  {{ cv.title || t('dashboard.untitled') }}
                </div>

                <div class="text-xs text-slate-400 mt-1">
                  {{ t('profile.updated') }}: {{ formatDate(cv.updatedAt) }}
                </div>
              </div>

              <!-- actions -->
              <div class="flex gap-2 mt-4">

                <button
                    @click="openCV(cv.id)"
                    class="flex-1 bg-white border border-slate-200 text-slate-700 text-sm py-2 rounded-lg hover:bg-slate-100 transition"
                >
                  {{ t('common.edit') }}
                </button>

                <button
                    @click="deleteCV(cv.id)"
                    class="px-3 py-2 text-red-500 hover:bg-red-50 rounded-lg transition"
                >
                  🗑️
                </button>

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

      </div>

    </div>

  </div>
</template>

<style scoped>
</style>