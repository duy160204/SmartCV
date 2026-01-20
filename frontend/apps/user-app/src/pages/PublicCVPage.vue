<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { publicCVApi } from '../api/user.api';
import CVRenderer from '../components/core/CVRenderer.vue';

const route = useRoute();
const token = route.params.token as string;
const loading = ref(true);
const error = ref<string | null>(null);
const cv = ref<any>(null);

const fetchCV = async () => {
    try {
        loading.value = true;
        const response = await publicCVApi.getPublicCV(token);
        // The DTO returns content (json), html (string), css (string)
        cv.value = response.data;
    } catch (err: any) {
        console.error(err);
        error.value = "Failed to load CV. The link may be invalid or expired.";
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (token) {
        fetchCV();
    } else {
        error.value = "Invalid Link";
        loading.value = false;
    }
});
</script>

<template>
    <div class="public-cv-container min-h-screen bg-gray-100 flex flex-col">
        <!-- Header -->
        <div class="bg-white shadow-sm p-4 flex justify-between items-center z-10 sticky top-0">
            <h1 class="text-xl font-bold text-gray-800">SmartCV</h1>
            <a 
                :href="`/api/public/cv/${token}/download`" 
                class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded shadow transition flex items-center gap-2"
                v-if="cv"
                download="cv.pdf"
            >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                </svg>
                Download PDF
            </a>
        </div>

        <!-- Content -->
        <div class="flex-1 overflow-hidden relative">
            <div v-if="loading" class="flex items-center justify-center h-full text-gray-500">
                Loading CV...
            </div>
            
            <div v-else-if="error" class="flex items-center justify-center h-full text-red-500 font-medium">
                {{ error }}
            </div>

            <CVRenderer 
                v-else-if="cv" 
                :html="cv.html" 
                :css="cv.css" 
                :data="cv.content" 
            />
        </div>
    </div>
</template>

<style scoped>
.public-cv-container {
    height: 100vh;
}
</style>
