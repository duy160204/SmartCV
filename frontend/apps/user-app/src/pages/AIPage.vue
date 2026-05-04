<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { cvApi, aiApi } from '@/api/user.api';
import { ref, onMounted, computed, watch } from 'vue';
import { processAiAnswer } from '@/utils/aiMarkdown';
import { useCVStore } from '@/stores/cv';
import CVRenderer from '@/components/core/CVRenderer.vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const auth = useAuthStore();
const cvStore = useCVStore();

const myCVs = ref<any[]>([]);
const selectedCVId = ref<number | null>(null);
const selectedCVDetail = ref<any | null>(null);

const inputMessage = ref('');
const isLoadingCVs = ref(false);
const isSending = ref(false);

const selectedLevel = ref<string | null>(null);
const messages = ref<any[]>([]);

const levels = [
  { label: t('ai.level_general'), value: null },
  { label: t('ai.level_intern'), value: 'INTERN' },
  { label: t('ai.level_fresher'), value: 'FRESHER' },
  { label: t('ai.level_junior'), value: 'JUNIOR' },
  { label: t('ai.level_middle'), value: 'MIDDLE' },
  { label: t('ai.level_senior'), value: 'SENIOR' }
];

const getParsedContent = (content: any) => {
  if (!content) return {};
  if (typeof content === 'object') return content;
  try {
    return JSON.parse(content);
  } catch {
    return {};
  }
};

watch(selectedCVId, async (id) => {
  if (!id) return;
  await cvStore.loadCV(id);
  selectedCVDetail.value = cvStore.currentCV;
}, { immediate: true });

const cvRenderData = computed(() => {
  const cv = selectedCVDetail.value;
  if (!cv) return null;

  let tmpl = { html: '', css: '' };

  if (cv.templateSnapshot) {
    try {
      tmpl =
          typeof cv.templateSnapshot === 'string'
              ? JSON.parse(cv.templateSnapshot)
              : cv.templateSnapshot;
    } catch {}
  }

  return {
    html: tmpl.html || '',
    css: tmpl.css || '',
    data: getParsedContent(cv.dataJson || cv.content)
  };
});

onMounted(async () => {
  if (!auth.isAuthenticated) return;

  isLoadingCVs.value = true;
  try {
    const res = await cvApi.getAll();
    myCVs.value = res.data;

    if (myCVs.value.length > 0) {
      selectedCVId.value = myCVs.value[0].id;
    }
  } finally {
    isLoadingCVs.value = false;
  }
});

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return;

  if (!selectedCVId.value) {
    messages.value.push({
      role: 'assistant',
      content: t('ai.empty_hint')
    });
    return;
  }

  const userMsg = inputMessage.value;

  messages.value.push({ role: 'user', content: userMsg });
  inputMessage.value = '';
  isSending.value = true;

  try {
    const { useLanguageStore } = await import('@/stores/language.store');
    const langStore = useLanguageStore();

    // FIX TS2345: coalesce null → undefined for strict type compatibility
    const res = await aiApi.chat(selectedCVId.value, userMsg, selectedLevel.value || undefined, langStore.locale);
    const reply = res.data?.answer || res.data?.message || t('ai.default_response');

    const html = await processAiAnswer(reply);

    messages.value.push({
      role: 'assistant',
      content: reply,
      html,
      level: selectedLevel.value
    });
  } catch (e: any) {
    messages.value.push({
      role: 'assistant',
      content: e.message || t('common.noData')
    });
  } finally {
    isSending.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-slate-50 via-white to-blue-50">

    <!-- HEADER -->
    <section class="bg-gradient-to-r from-indigo-600 to-blue-600 text-white py-10 px-6">
      <div class="max-w-4xl mx-auto text-center">
        <h1 class="text-4xl font-bold tracking-tight">
          {{ t('ai.chat_title') }}
        </h1>
        <p class="text-indigo-100 mt-2 text-sm">
          {{ t('ai.empty_hint') }}
        </p>
      </div>
    </section>

    <!-- WORKSPACE -->
    <section v-if="auth.isAuthenticated"
             class="max-w-[1600px] mx-auto px-6 mt-6 h-[calc(100vh-200px)]">

      <div class="flex gap-4 h-full">

        <!-- CHAT -->
        <div class="w-[32%] bg-white rounded-2xl border border-blue-100 flex flex-col overflow-hidden shadow-sm">

          <!-- header -->
          <div class="p-3 border-b flex justify-between items-center bg-blue-50/60">
            <span class="text-xs font-semibold text-blue-700">
              {{ t('ai.chat_title') }}
            </span>

            <select
                v-model="selectedLevel"
                class="text-xs border border-blue-100 rounded-lg px-2 py-1 bg-white text-blue-700"
                :disabled="isSending"
            >
              <option v-for="l in levels" :key="l.label" :value="l.value">
                {{ l.label }}
              </option>
            </select>
          </div>

          <!-- messages -->
          <div class="flex-1 overflow-y-auto p-4 space-y-3 bg-blue-50/30">

            <div v-if="messages.length === 0"
                 class="text-center text-blue-300 mt-10 text-sm">
              🤖 {{ t('ai.empty_hint') }}
            </div>

            <div
                v-for="(m, i) in messages"
                :key="i"
                class="max-w-[85%] px-4 py-3 rounded-2xl text-sm shadow-sm transition"
                :class="m.role === 'user'
                ? 'bg-indigo-500 text-white ml-auto rounded-br-sm'
                : 'bg-white border border-blue-100 text-slate-700 rounded-bl-sm'"
            >
              <div v-if="m.level" class="text-[10px] opacity-60 mb-1">
                {{ m.level }}
              </div>

              <div v-if="m.html" v-html="m.html"></div>
              <div v-else>{{ m.content }}</div>
            </div>

            <div v-if="isSending"
                 class="text-xs text-blue-400 flex items-center gap-2">
              <div class="w-2 h-2 bg-blue-400 rounded-full animate-pulse"></div>
              {{ t('common.loading') }}
            </div>

          </div>

          <!-- input -->
          <div class="p-3 border-t bg-white">
            <textarea
                v-model="inputMessage"
                @keyup.enter.prevent="sendMessage"
                class="w-full border border-blue-100 rounded-xl p-3 text-sm h-20 resize-none focus:ring-2 focus:ring-blue-300 focus:outline-none"
                :placeholder="t('ai.input_placeholder')"
            />

            <button
                @click="sendMessage"
                :disabled="isSending"
                class="mt-2 w-full bg-blue-600 text-white py-2 rounded-xl font-semibold hover:bg-blue-700 disabled:opacity-50 transition"
            >
              {{ t('ai.send') }}
            </button>
          </div>

        </div>

        <!-- CV PREVIEW -->
        <div class="flex-1 bg-white rounded-2xl border border-blue-100 flex flex-col overflow-hidden shadow-sm">

          <div class="p-3 border-b bg-blue-50/40">
            <span class="text-xs font-semibold text-blue-700 uppercase">
              {{ t('ai.chat_title') }}
            </span>
          </div>

          <div class="flex-1 overflow-hidden bg-white">
            <CVRenderer
                v-if="cvRenderData"
                :html="cvRenderData.html"
                :css="cvRenderData.css"
                :data="cvRenderData.data"
            />
          </div>

        </div>

        <!-- CV LIST -->
        <div class="w-[22%] bg-white rounded-2xl border border-blue-100 flex flex-col overflow-hidden shadow-sm">

          <div class="p-3 border-b flex justify-between items-center bg-blue-50/40">
            <span class="text-xs font-semibold text-blue-700">
              {{ t('dashboard.title') }}
            </span>

            <span class="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">
              {{ myCVs.length }}
            </span>
          </div>

          <div class="flex-1 overflow-y-auto p-2 space-y-2">

            <div v-if="isLoadingCVs"
                 class="text-xs text-blue-300 text-center py-4">
              {{ t('common.loading') }}
            </div>

            <button
                v-for="cv in myCVs"
                :key="cv.id"
                @click="selectedCVId = cv.id"
                class="w-full text-left p-3 rounded-xl border transition text-sm"
                :class="selectedCVId === cv.id
                ? 'bg-blue-50 border-blue-200'
                : 'hover:bg-blue-50/50 border-blue-100'"
            >
              <div class="font-semibold text-slate-700 truncate">
                {{ cv.title || t('dashboard.untitled') }}
              </div>

              <div class="text-[10px] text-slate-400 mt-1">
                {{ new Date(cv.updatedAt).toLocaleDateString() }}
              </div>
            </button>

          </div>

        </div>

      </div>
    </section>

  </div>
</template>

<style scoped>
</style>