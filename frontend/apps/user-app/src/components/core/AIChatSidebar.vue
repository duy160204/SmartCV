<script setup lang="ts">
import { ref } from 'vue';
import { useCVStore } from '@/stores/cv';
import { useUserPlanStore } from '@/stores/user-plan.store';
import { processAiAnswer } from '@/utils/aiMarkdown';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const store = useCVStore();
const planStore = useUserPlanStore();

const isOpen = ref(false);

const messages = ref<
  { role: 'user' | 'assistant'; content: string; html?: string; level?: string | null }[]
>([]);

const currentInput = ref('');
const isProcessing = ref(false);

// ⚠️ FIX: default luôn là General (null nhưng UI vẫn show rõ)
const selectedLevel = ref<string | null>(null);

const levels = [
  { label: t('ai.level_general'), value: null },
  { label: t('ai.level_intern'), value: 'INTERN' },
  { label: t('ai.level_fresher'), value: 'FRESHER' },
  { label: t('ai.level_junior'), value: 'JUNIOR' },
  { label: t('ai.level_middle'), value: 'MIDDLE' },
  { label: t('ai.level_senior'), value: 'SENIOR' }
];

const toggleChat = () => {
  isOpen.value = !isOpen.value;
};

const sendMessage = async () => {
  if (!currentInput.value.trim() || isProcessing.value) return;

  messages.value.push({
    role: 'user',
    content: currentInput.value
  });

  const msg = currentInput.value;
  currentInput.value = '';
  isProcessing.value = true;

  try {
    await store.saveCV();

    const { aiApi } = await import('@/api/user.api');

    const levelContext = selectedLevel.value;

    const { useLanguageStore } = await import('@/stores/language.store');
    const langStore = useLanguageStore();

    // FIX TS2345: coalesce null → undefined for strict type compatibility
    const res = await aiApi.chat(store.currentCV?.id!, msg, levelContext || undefined, langStore.locale);

    const rawContent =
      res.data?.answer ||
      res.data?.message ||
      (typeof res.data === 'string' ? res.data : t('ai.default_response'));

    const processedHtml = await processAiAnswer(rawContent);

    messages.value.push({
      role: 'assistant',
      content: rawContent,
      html: processedHtml,
      level: levelContext
    });
  } catch (e: any) {
    messages.value.push({
      role: 'assistant',
      content: e.response?.data?.message || e.message
    });
  } finally {
    isProcessing.value = false;
    planStore.fetchSubscription();
  }
};
</script>

<template>
  <div class="fixed bottom-4 right-4 z-50 flex flex-col items-end">

    <!-- TOGGLE BUTTON -->
    <button
      @click="toggleChat"
      class="bg-indigo-600 hover:bg-indigo-700 text-white p-4 rounded-full shadow-lg"
    >
      <span v-if="!isOpen">🤖</span>
      <span v-else>✕</span>
    </button>

    <!-- CHAT WINDOW -->
    <div
      v-show="isOpen"
      class="bg-white border rounded-lg shadow-xl mt-4 w-80 h-96 flex flex-col overflow-hidden"
    >

      <!-- HEADER (FIXED + ALWAYS VISIBLE LEVEL SELECT) -->
      <div class="bg-indigo-600 text-white px-3 py-2 flex items-center justify-between">

        <!-- LEFT TITLE -->
        <div class="font-semibold text-sm">
          {{ t('ai.chat_title') }}
        </div>

        <!-- RIGHT LEVEL SELECT (FORCED VISIBILITY) -->
        <div class="flex items-center gap-2">
          <span class="text-[10px] opacity-80">{{ t('ai.level_label') }}</span>

          <select
            v-model="selectedLevel"
            class="bg-white text-gray-800 text-[11px] px-2 py-1 rounded border border-gray-300 focus:outline-none"
            :disabled="isProcessing"
          >
            <option
              v-for="l in levels"
              :key="String(l.value)"
              :value="l.value"
            >
              {{ l.label }}
            </option>
          </select>
        </div>
      </div>

      <!-- MESSAGES -->
      <div class="flex-1 overflow-y-auto p-3 space-y-2 bg-gray-50">
        <div
          v-if="messages.length === 0"
          class="text-center text-gray-500 text-sm mt-4"
        >
          {{ t('ai.empty_hint') }}
        </div>

        <div
          v-for="(m, i) in messages"
          :key="i"
          :class="[
            'max-w-[85%] rounded p-2 text-sm',
            m.role === 'user'
              ? 'bg-blue-100 ml-auto text-blue-900'
              : 'bg-white border text-gray-800'
          ]"
        >
          <div
            v-if="m.role === 'assistant' && m.level"
            class="text-[9px] text-indigo-500 mb-1"
          >
            {{ t('ai.level_display') }}: {{ m.level }}
          </div>

          <div v-if="m.html" v-html="m.html"></div>
          <div v-else>{{ m.content }}</div>
        </div>
      </div>

      <!-- INPUT -->
      <div class="p-2 border-t flex gap-2">
        <input
          v-model="currentInput"
          class="flex-1 border rounded px-2 py-1 text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
          :placeholder="planStore.currentSubscription?.usage.remaining === 0 && !planStore.isUnlimited ? t('ai.limit_reached_placeholder') : t('ai.input_placeholder')"
          :disabled="isProcessing || (planStore.currentSubscription?.usage.remaining === 0 && !planStore.isUnlimited)"
        />

        <button
          @click="sendMessage"
          class="bg-indigo-600 text-white px-3 py-1 rounded text-sm disabled:bg-indigo-300 disabled:cursor-not-allowed"
          :disabled="isProcessing || (planStore.currentSubscription?.usage.remaining === 0 && !planStore.isUnlimited)"
        >
          {{ t('ai.send') }}
        </button>
      </div>
    </div>
  </div>
</template>