<script setup lang="ts">
import { ref } from 'vue';
import { useCVStore } from '@/stores/cv';
import api from '@/api/axios'; // Direct Axios or via store

const store = useCVStore();

const isOpen = ref(false);
const messages = ref<{role: 'user' | 'assistant', content: string}[]>([]);
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
        
        // Import aiApi and call backend
        const { aiApi } = await import('@/api/user.api');
        
        const res = await aiApi.chat(store.currentCV?.id!, msg);
        
        messages.value.push({
            role: 'assistant',
            content: res.data.message
        });
        
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
              <span class="text-xs bg-green-400 text-green-900 px-2 py-1 rounded font-bold">FREE</span>
          </div>
          
          <!-- Messages -->
          <div class="flex-1 overflow-y-auto p-3 space-y-3 bg-gray-50">
              <div v-if="messages.length === 0" class="text-center text-gray-500 text-sm mt-4">
                  Ask me anything about your CV! <br> I can help improve wording, suggest skills, or spot errors.
                  <div class="text-xs mt-2 text-gray-400">Limited to 20 requests per day</div>
              </div>
              <div 
                v-for="(m, i) in messages" 
                :key="i"
                :class="['max-w-[85%] rounded p-2 text-sm', m.role === 'user' ? 'bg-blue-100 ml-auto text-blue-900' : 'bg-white border text-gray-800 self-start']"
              >
                  {{ m.content }}
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
