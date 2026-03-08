<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
    isOpen: boolean;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
    (e: 'generate', file: File): void;
}>();

const selectedFile = ref<File | null>(null);
const previewUrl = ref('');

const handleFile = (e: Event) => {
    const file = (e.target as HTMLInputElement).files?.[0];
    if (file) {
        selectedFile.value = file;
        previewUrl.value = URL.createObjectURL(file);
    }
};

const handleGenerate = () => {
    if (selectedFile.value) {
        emit('generate', selectedFile.value);
        // Clean up
        selectedFile.value = null;
        previewUrl.value = '';
    }
};

const close = () => {
    selectedFile.value = null;
    previewUrl.value = '';
    emit('close');
}
</script>

<template>
    <div v-if="isOpen" class="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-lg overflow-hidden flex flex-col">
            <div class="p-4 border-b bg-purple-50 flex justify-between items-center text-purple-700">
                <h3 class="font-bold flex items-center gap-2">✨ AI Template Builder</h3>
                <button @click="close" class="text-xl font-bold">&times;</button>
            </div>
            
            <div class="p-6">
                <p class="text-sm text-gray-600 mb-4">
                    Upload a mockup design of the CV template. Our AI will analyze the image and generate starter HTML and CSS.
                </p>
                
                <input type="file" accept="image/png, image/jpeg, image/webp" @change="handleFile" class="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded file:border-0 file:text-sm file:font-semibold file:bg-purple-50 file:text-purple-700 hover:file:bg-purple-100 mb-4" />
                
                <div v-if="previewUrl" class="border p-2 rounded bg-gray-50">
                    <img :src="previewUrl" class="max-h-64 object-contain mx-auto" />
                </div>
            </div>
            
            <div class="p-4 border-t bg-gray-50 flex justify-end gap-3">
                <button @click="close" class="px-4 py-2 border rounded text-gray-600 hover:bg-gray-100 font-medium">Cancel</button>
                <button @click="handleGenerate" :disabled="!selectedFile" class="px-4 py-2 bg-purple-600 text-white rounded shadow font-medium hover:bg-purple-700 disabled:opacity-50">Generate HTML/CSS</button>
            </div>
        </div>
    </div>
</template>
