<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
    isOpen: boolean;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'generate', prompt: string): void;
}>();

const promptText = ref('');

const handleGenerate = () => {
    if (!promptText.value.trim()) return;
    emit('generate', promptText.value);
    promptText.value = ''; // Reset after emit
};
</script>

<template>
    <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-lg overflow-hidden">
            <div class="p-4 border-b bg-purple-50 flex justify-between items-center">
                <h3 class="font-bold text-purple-700 flex items-center gap-2">
                    ✨ Generate with AI
                </h3>
                <button @click="$emit('close')" class="text-gray-500 hover:text-gray-700 text-xl font-bold">&times;</button>
            </div>
            
            <div class="p-6">
                <p class="text-sm text-gray-600 mb-4">
                    Describe your background, skills, and experience. The AI will generate a structured CV tailored to this template.
                </p>
                
                <textarea 
                    v-model="promptText"
                    rows="6"
                    class="w-full border p-3 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-purple-500 outline-none resize-none"
                    placeholder="E.g., I'm a Senior Frontend Engineer with 5 years of experience using Vue and React. I've worked on large-scale e-commerce platforms..."
                ></textarea>
            </div>
            
            <div class="p-4 border-t bg-gray-50 flex justify-end gap-3">
                <button @click="$emit('close')" class="px-4 py-2 border rounded text-gray-600 hover:bg-gray-100 font-medium">
                    Cancel
                </button>
                <button 
                    @click="handleGenerate"
                    :disabled="!promptText.trim()"
                    class="px-4 py-2 bg-purple-600 text-white rounded shadow font-medium hover:bg-purple-700 disabled:opacity-50 flex items-center gap-2"
                >
                    Generate CV
                </button>
            </div>
        </div>
    </div>
</template>
