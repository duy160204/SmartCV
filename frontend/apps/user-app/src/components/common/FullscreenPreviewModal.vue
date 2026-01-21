<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue';

const props = defineProps<{
    isOpen: boolean;
    imageSrc: string | null;
    title?: string;
}>();

const emit = defineEmits(['close']);

const handleKeydown = (e: KeyboardEvent) => {
    if (e.key === 'Escape' && props.isOpen) {
        emit('close');
    }
};

onMounted(() => {
    window.addEventListener('keydown', handleKeydown);
});

onUnmounted(() => {
    window.removeEventListener('keydown', handleKeydown);
});
</script>

<template>
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 backdrop-blur-sm animate-in fade-in duration-200" @click="$emit('close')">
        <!-- Close Button -->
        <button class="absolute top-4 right-4 text-white hover:text-gray-300 z-50 p-2">
            <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
        </button>

        <!-- Content -->
        <div class="relative max-w-[90vw] max-h-[90vh] flex flex-col items-center" @click.stop>
            <div v-if="title" class="text-white text-lg font-bold mb-4 bg-black/50 px-4 py-2 rounded-full backdrop-blur">
                {{ title }}
            </div>
            
            <img 
                v-if="imageSrc" 
                :src="imageSrc" 
                class="max-w-full max-h-[85vh] object-contain rounded-lg shadow-2xl border border-gray-800"
                alt="Template Preview"
            />
            <div v-else class="w-[80vw] h-[80vh] bg-gray-800 flex items-center justify-center text-gray-500 rounded-lg">
                No Preview Available
            </div>
        </div>
    </div>
</template>
