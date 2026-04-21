<script setup lang="ts">
import { ref } from 'vue';
import { useCVStore } from '@/stores/cv';
import { processAiAnswer } from '@/utils/aiMarkdown';

const store = useCVStore();

const isOpen = ref(false);

const messages = ref<
  { role: 'user' | 'assistant'; content: string; html?: string; level?: string | null }[]
>([]);

const currentInput = ref('');
const isProcessing = ref(false);

// ⚠️ FIX: default luôn là General (null nhưng UI vẫn show rõ)
const selectedLevel = ref<string | null>(null);

const levels = [
  { label: 'General', value: null },
  { label: 'Intern', value: 'INTERN' },
  { label: 'Fresher', value: 'FRESHER' },
  { label: 'Junior', value: 'JUNIOR' },
  { label: 'Middle', value: 'MIDDLE' },
  { label: 'Senior', value: 'SENIOR' }
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

    const res = await aiApi.chat(store.currentCV?.id!, msg, levelContext);

    const rawContent =
      res.data?.answer ||
      res.data?.message ||
      (typeof res.data === 'string' ? res.data : 'AI Response Received');

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
          Chat with AI Assistant
        </div>

        <!-- RIGHT LEVEL SELECT (FORCED VISIBILITY) -->
        <div class="flex items-center gap-2">
          <span class="text-[10px] opacity-80">Level</span>

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
          Ask me anything about your CV
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
            Level: {{ m.level }}
          </div>

          <div v-if="m.html" v-html="m.html"></div>
          <div v-else>{{ m.content }}</div>
        </div>
      </div>

      <!-- INPUT -->
      <div class="p-2 border-t flex gap-2">
        <input
          v-model="currentInput"
          class="flex-1 border rounded px-2 py-1 text-sm"
          placeholder="Type message..."
        />

        <button
          @click="sendMessage"
          class="bg-indigo-600 text-white px-3 py-1 rounded text-sm"
          :disabled="isProcessing"
        >
          Send
        </button>
      </div>
    </div>
  </div>
</template>