<script setup lang="ts">
import { ref } from 'vue';
import FullscreenPreviewModal from '@/components/common/FullscreenPreviewModal.vue';

const emit = defineEmits(['trigger-auth']);

const previewModalOpen = ref(false);
const currentPreviewImage = ref<string | null>(null);

// Mocks with IDs for v-for
const mockTemplates = [
    { id: 1, name: 'Professional Modern', color: 'bg-blue-100' },
    { id: 2, name: 'Executive Suite', color: 'bg-gray-100' },
    { id: 3, name: 'Creative Portfolio', color: 'bg-purple-100' },
    { id: 4, name: 'Minimalist Clean', color: 'bg-green-100' },
];

const openAuth = () => {
    emit('trigger-auth');
};

const openPreview = (tmpl: any) => {
    // In real app, these would be real assets
    // forcing a placeholder for now
    currentPreviewImage.value = null; 
    previewModalOpen.value = true;
};
</script>

<template>
    <section class="py-20 bg-gray-50 mb-10">
        <div class="max-w-6xl mx-auto px-8">
            <div class="text-center mb-12">
                <h2 class="text-3xl font-bold mb-4 text-gray-800">Professional Templates</h2>
                <p class="text-gray-600">Designed to pass ATS and catch recruiter attention.</p>
            </div>
            
            <!-- Grid -->
            <div class="grid grid-cols-2 md:grid-cols-4 gap-8 mb-12">
                 <div 
                    v-for="tmpl in mockTemplates" 
                    :key="tmpl.id"
                    class="group relative bg-white rounded-xl shadow-sm hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-1 overflow-hidden border border-gray-100"
                 >
                     <!-- Mock Image Area -->
                     <div class="aspect-[210/297] bg-white p-3 flex flex-col gap-2 relative">
                         <!-- Static content to look like a CV -->
                         <div class="h-4 w-1/3 bg-gray-200 rounded"></div>
                         <div class="h-full w-full" :class="tmpl.color + ' rounded opacity-50'"></div>

                         <!-- Hover Overlay -->
                         <div class="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center p-4 gap-3 backdrop-blur-[2px]">
                             <button 
                                @click="openPreview(tmpl)" 
                                class="bg-white/20 text-white border border-white/50 w-full py-2.5 rounded-lg font-bold hover:bg-white hover:text-black transition backdrop-blur-sm"
                             >
                                 Preview
                             </button>
                             <button 
                                @click="openAuth" 
                                class="bg-blue-600 text-white w-full py-2.5 rounded-lg font-bold hover:bg-blue-700 transition shadow-lg"
                             >
                                 Use Template
                             </button>
                         </div>
                     </div>
                 </div>
            </div>
            
            <div class="text-center">
                 <p class="text-sm text-gray-500">Sign up to customize these templates with our AI assistant.</p>
            </div>
        </div>

        <FullscreenPreviewModal 
            :isOpen="previewModalOpen"
            :imageSrc="currentPreviewImage"
            title="Template Preview"
            @close="previewModalOpen = false"
        />
    </section>
</template>
