<script setup lang="ts">
import { ref } from 'vue';
import { useCVStore } from '@/stores/cv';
import api from '@/api/axios'; // Direct Axios or via store
import { processAiAnswer } from '@/utils/aiMarkdown';

const store = useCVStore();

const isOpen = ref(false);
const messages = ref<{role: 'user' | 'assistant', content: string, html?: string}[]>([]);
const currentInput = ref('');
const isProcessing = ref(false);

const toggleChat = () => {
    isOpen.value = !isOpen.value;
};

const sendMessage = async () => {
    if (!currentInput.value.trim() || isProcessing.value) return;
    
    // Add user message
    messages.value.push({
        role: 'user',
        content: currentInput.value
    });
    
    const msg = currentInput.value;
    currentInput.value = '';
    isProcessing.value = true;
    
    try {
        // Save CV first to ensure context is fresh
        await store.saveCV();
        
        const { aiApi } = await import('@/api/user.api');
        
        const res = await aiApi.chat(store.currentCV?.id!, msg);
        const rawContent = res.data?.answer || res.data?.message || (typeof res.data === 'string' ? res.data : "AI Response Received");
        console.log("[AIChatSidebar] API Answer Received:", rawContent);
        
        const processedHtml = await processAiAnswer(rawContent);
        console.log("[AIChatSidebar] Processed HTML:", processedHtml);

        messages.value.push({
            role: 'assistant',
            content: rawContent,
            html: processedHtml
        });
        console.log("[AIChatSidebar] Messages length after push:", messages.value.length);
        
    } catch (e: any) {
        // Backend will return usage limit errors
        const errorMsg = e.response?.data?.message || e.message;
        messages.value.push({
            role: 'assistant',
            content: errorMsg
        });
    } finally {
        isProcessing.value = false;
    }
};
</script>

<template>
  <div class="fixed bottom-4 right-4 z-50 flex flex-col items-end">
      <!-- Toggle Button -->
      <button 
        @click="toggleChat"
        class="bg-indigo-600 hover:bg-indigo-700 text-white p-4 rounded-full shadow-lg flex items-center justify-center transition-all duration-300 transform hover:scale-105"
        :class="{'rotate-45': isOpen}"
      >
          <span v-if="!isOpen" class="text-xl">🤖</span>
          <span v-else class="text-xl">✕</span>
      </button>
      
      <!-- Chat Window -->
      <div v-show="isOpen" class="bg-white border rounded-lg shadow-xl mt-4 w-80 h-96 flex flex-col overflow-hidden transition-all origin-bottom-right">
          <!-- Header -->
          <div class="bg-indigo-600 text-white p-3 font-bold flex justify-between items-center">
              <span>Chat with AI Assistant</span>
          </div>
          
          <!-- Messages -->
          <div class="flex-1 overflow-y-auto p-3 space-y-3 bg-gray-50">
              <div v-if="messages.length === 0" class="text-center text-gray-500 text-sm mt-4">
                  Ask me anything about your CV! <br> I can help improve wording, suggest skills, or spot errors.
              </div>
              <div 
                v-for="(m, i) in messages" 
                :key="i"
                :class="['max-w-[85%] rounded p-2 text-sm', m.role === 'user' ? 'bg-blue-100 ml-auto text-blue-900' : 'bg-white border text-gray-800 self-start ai-markdown']"
              >
                  <div v-if="m.html" v-html="m.html"></div>
                  <div v-else>{{ m.content }}</div>
              </div>
              <div v-if="isProcessing" class="text-xs text-gray-400 italic">AI is thinking...</div>
          </div>
          
          <!-- Input -->
          <div class="p-2 border-t bg-white">
              <form @submit.prevent="sendMessage" class="flex gap-2">
                  <input 
                    v-model="currentInput" 
                    placeholder="Type a message..." 
                    class="flex-1 border rounded px-2 py-1 text-sm focus:outline-none focus:border-indigo-500"
                    :disabled="isProcessing"
                  />
                  <button 
                    type="submit" 
                    class="bg-indigo-600 text-white px-3 py-1 rounded text-sm disabled:opacity-50"
                    :disabled="!currentInput.trim() || isProcessing"
                  >
                      Send
                  </button>
              </form>
          </div>
      </div>
  </div>
</template>

<style scoped>
.ai-markdown :deep(h1), .ai-markdown :deep(h2), .ai-markdown :deep(h3), .ai-markdown :deep(h4) {
    margin-top: 0.5rem;
    margin-bottom: 0.5rem;
    font-weight: bold;
}
.ai-markdown :deep(h1) { font-size: 1.5rem; }
.ai-markdown :deep(h2) { font-size: 1.25rem; }
.ai-markdown :deep(h3) { font-size: 1.125rem; }
.ai-markdown :deep(ul), .ai-markdown :deep(ol) {
    padding-left: 1.5rem;
    margin-bottom: 0.5rem;
}
.ai-markdown :deep(ul) { list-style-type: disc; }
.ai-markdown :deep(ol) { list-style-type: decimal; }
.ai-markdown :deep(p) {
    margin-bottom: 0.5rem;
    line-height: 1.5;
}
.ai-markdown :deep(code) {
    background-color: #f3f4f6;
    padding: 0.1rem 0.3rem;
    border-radius: 0.25rem;
    font-family: monospace;
}
.ai-markdown :deep(pre) {
    background-color: #1f2937;
    color: #f3f4f6;
    padding: 1rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin-bottom: 0.5rem;
}
.ai-markdown :deep(blockquote) {
    border-left: 4px solid #e5e7eb;
    padding-left: 1rem;
    color: #6b7280;
    font-style: italic;
}
</style>
