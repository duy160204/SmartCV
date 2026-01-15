<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
    isOpen: boolean;
    currentText: string;
}>();

const emit = defineEmits(['close', 'apply']);

const instruction = ref('');
const isLoading = ref(false);

const submit = () => {
    if (!instruction.value) return;
    emit('apply', instruction.value);
    instruction.value = '';
};
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded p-6 w-96 shadow-xl">
          <h3 class="font-bold text-lg mb-2">✨ AI Assistant</h3>
          <p class="text-xs text-gray-500 mb-4">Current text: "{{ currentText.substring(0, 50) }}..."</p>
          
          <label class="block text-sm font-medium mb-1">Instruction</label>
          <input 
            v-model="instruction" 
            placeholder="e.g. Fix grammar, Make it more professional..." 
            class="w-full border p-2 rounded mb-4"
            @keyup.enter="submit"
          />
          
          <div class="flex justify-end gap-2">
              <button @click="$emit('close')" class="px-3 py-1 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
              <button 
                @click="submit" 
                class="px-3 py-1 bg-purple-600 text-white rounded hover:bg-purple-700 font-medium"
              >
                 Generate
              </button>
          </div>
      </div>
  </div>
</template>
