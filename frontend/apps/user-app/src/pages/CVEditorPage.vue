<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCVStore } from '@/stores/cv';
import CVRenderer from '@/components/core/CVRenderer.vue';
import CVForm from '@/components/core/CVForm.vue';
import AIChatSidebar from '@/components/core/AIChatSidebar.vue';
import { cvApi } from '@/api/user.api';

const route = useRoute();
const router = useRouter();
const store = useCVStore();

onMounted(() => {
    const id = Number(route.params.id);
    if (id) {
        store.loadCV(id);
    }
});

const deleteCV = async () => {
    if (!store.currentCV?.id) return;
    if (!confirm("Are you sure you want to delete this CV? This cannot be undone.")) return;
    try {
        await cvApi.delete(store.currentCV.id);
        router.push('/dashboard');
    } catch (e: any) {
        alert("Failed to delete: " + e.message);
    }
};

const downloadCV = async () => {
    if (!store.currentCV?.id) return;
    try {
        const response = await cvApi.download(store.currentCV.id);
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${store.currentCV.title || 'cv'}.pdf`);
        document.body.appendChild(link);
        link.click();
        link.remove();
    } catch (e: any) {
        alert("Download failed: " + e.message);
    }
};
</script>

<template>
    <div class="h-screen flex flex-col" v-if="store.currentCV && store.currentTemplate">
        <!-- Top Bar -->
        <header class="h-16 bg-white border-b flex items-center px-4 justify-between z-10">
            <div class="flex items-center gap-4">
                <router-link to="/dashboard" class="text-gray-500 hover:text-black">← Back to Dashboard</router-link>
                <input 
                    v-model="store.currentCV.title" 
                    @blur="store.saveCV()"
                    class="font-bold text-lg border-transparent hover:border-gray-300 border px-2 rounded"
                />
            </div>
            
            <div class="flex items-center gap-4">
                <span v-if="store.isSaving" class="text-sm text-yellow-600">Saving...</span>
                <span v-else-if="store.lastSaved" class="text-sm text-green-600">Saved {{ store.lastSaved.toLocaleTimeString() }}</span>
                
                <!-- Share Button with simple toggle for now -->
                <div class="relative group">
                    <button class="text-gray-600 hover:text-blue-600 font-medium">Share</button>
                    <!-- Dropdown -->
                    <div class="hidden group-hover:block absolute top-full right-0 bg-white border shadow-lg p-4 w-80 rounded z-50">
                        <div v-if="store.currentCV.isPublic">
                            <p class="text-sm text-green-600 font-bold mb-2">CV is Public</p>
                            <input readonly :value="`http://localhost:3000/cv/public/${store.currentCV.publicToken}`" class="w-full text-xs border p-1 rounded mb-2 bg-gray-50" />
                            <button @click="store.unpublishCV()" class="text-xs text-red-500 hover:underline">Revoke Link</button>
                        </div>
                        <div v-else>
                            <p class="text-sm text-gray-600 mb-2">Publish your CV to get a shareable link.</p>
                            <button @click="store.publishCV()" class="w-full bg-blue-100 text-blue-700 py-1 rounded text-sm hover:bg-blue-200">Generate Link</button>
                        </div>
                    </div>
                </div>

                <button @click="deleteCV" class="text-gray-500 hover:text-red-600 font-medium">Delete</button>

                <button @click="downloadCV" class="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700">Download PDF</button>
            </div>
        </header>

        <!-- Main Editor Area -->
        <div class="flex flex-1 overflow-hidden">
            <!-- Left Panel: Form -->
            <div class="w-1/2 md:w-5/12 lg:w-1/3 bg-white border-r overflow-y-auto">
                <CVForm />
            </div>

            <!-- Right Panel: Preview -->
            <div class="flex-1 bg-gray-100 overflow-hidden relative">
                <CVRenderer 
                    :html="store.currentTemplate.html"
                    :css="store.currentTemplate.css"
                    :data="store.currentCV.content"
                />
                
                <!-- AI Chat Sidebar -->
                <AIChatSidebar />
            </div>
        </div>
    </div>
    
    <div v-else class="h-screen flex items-center justify-center">
        Loading Editor...
    </div>
</template>
