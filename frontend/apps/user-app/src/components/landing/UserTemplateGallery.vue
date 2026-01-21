<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';
import { cvApi } from '@/api/user.api';
import FullscreenPreviewModal from '@/components/common/FullscreenPreviewModal.vue';

const router = useRouter();
const templates = ref<any[]>([]);
const subscription = ref<any>(null);
const isLoading = ref(true);
const isCreating = ref(false);
const previewModalOpen = ref(false);
const currentPreviewImage = ref<string | null>(null);
const currentPreviewTitle = ref('');

const loadData = async () => {
    isLoading.value = true;
    try {
        const [tplRes, subRes] = await Promise.all([
            api.get('/templates'),
            api.get('/subscription/me')
        ]);
        templates.value = tplRes.data;
        subscription.value = subRes.data;
    } catch (e) {
        console.error("Failed to load user data", e);
    } finally {
        isLoading.value = false;
    }
};

const openPreview = (tmpl: any) => {
    currentPreviewImage.value = tmpl.thumbnailUrl || null;
    currentPreviewTitle.value = tmpl.name;
    previewModalOpen.value = true;
};

const createCV = async (templateId: number) => {
    if (isCreating.value) return;
    
    // Optimistic check if we have subscription data (optional, backend is source of truth)
    // if (subscription.value && subscription.value.cvCount >= subscription.value.maxCVs) { ... }

    isCreating.value = true;
    try {
        const res = await cvApi.create({
            title: 'My Resume',
            templateId: templateId,
            content: {} 
        });
        router.push(`/cv/editor/${res.data.id}`);
    } catch (e: any) {
        if (e.response && e.response.status === 403) {
            // Plan limit reached
            alert("Limit Reached: You have reached the maximum number of CVs for your current plan.");
            // Ideally replace alert with a nice modal or toast
        } else {
            alert("Failed to create CV: " + (e.response?.data?.message || e.message));
        }
    } finally {
        isCreating.value = false;
    }
};

onMounted(() => {
    loadData();
});
</script>

<template>
    <section class="py-12 bg-gray-50 min-h-[500px]">
        <div class="max-w-7xl mx-auto px-6">
            <div class="flex justify-between items-center mb-10">
                <h2 class="text-3xl font-bold text-gray-800">Select a Template</h2>
                <div v-if="subscription" class="text-sm bg-blue-50 text-blue-700 px-4 py-2 rounded-lg font-medium border border-blue-100 flex items-center gap-2">
                    <span>Plan: <span class="font-bold">{{ subscription.plan }}</span></span>
                    <span class="text-gray-400">|</span>
                    <span>Usage: {{ subscription.cvCount }} / {{ subscription.maxCVs === -1 ? '∞' : subscription.maxCVs }}</span>
                </div>
            </div>

            <div v-if="isLoading" class="grid grid-cols-2 md:grid-cols-4 gap-6 animate-pulse">
                <div v-for="i in 4" :key="i" class="aspect-[210/297] bg-gray-200 rounded-xl"></div>
            </div>

            <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
                <div 
                    v-for="tmpl in templates" 
                    :key="tmpl.id"
                    class="group relative bg-white rounded-xl shadow-sm hover:shadow-xl transition-all duration-300 border border-gray-100 overflow-hidden"
                >
                    <!-- Preview Image -->
                    <div class="aspect-[210/297] bg-gray-100 relative">
                        <img v-if="tmpl.thumbnailUrl" :src="tmpl.thumbnailUrl" class="w-full h-full object-cover">
                        <div v-else class="flex items-center justify-center h-full text-gray-400 font-medium">No Preview</div>

                        <!-- Hover Overlay -->
                        <div class="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center p-4 gap-3">
                             <button 
                                @click="openPreview(tmpl)"
                                class="bg-white/20 text-white border border-white/50 w-full py-2 rounded-lg font-bold hover:bg-white hover:text-black transition backdrop-blur-sm"
                            >
                                Preview
                            </button>
                            <button 
                                @click="createCV(tmpl.id)"
                                :disabled="isCreating"
                                class="bg-blue-600 text-white w-full py-2 rounded-lg font-bold hover:bg-blue-700 transition shadow-lg disabled:bg-gray-500 disabled:cursor-not-allowed"
                            >
                                {{ isCreating ? 'Creating...' : 'Use This Template' }}
                            </button>
                        </div>
                    </div>
                    
                    <div class="p-4 relative">
                        <h3 class="font-bold text-gray-800 truncate">{{ tmpl.name }}</h3>
                        <p class="text-xs text-gray-500 mt-1">Professional & ATS-Friendly</p>
                         <span v-if="tmpl.planRequired !== 'FREE'" class="absolute top-4 right-4 bg-yellow-100 text-yellow-800 text-[10px] font-bold px-2 py-0.5 rounded-full border border-yellow-200">
                            {{ tmpl.planRequired }}
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <FullscreenPreviewModal 
            :isOpen="previewModalOpen"
            :imageSrc="currentPreviewImage"
            :title="currentPreviewTitle"
            @close="previewModalOpen = false"
        />
    </section>
</template>
