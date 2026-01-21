<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCVStore } from '@/stores/cv';
import CVRenderer from '@/components/core/CVRenderer.vue';
import CVForm from '@/components/core/CVForm.vue';
import { cvApi, subscriptionApi } from '@/api/user.api';

const route = useRoute();
const router = useRouter();
const store = useCVStore();
const activeMobileTab = ref<'edit' | 'preview'>('edit');

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
        router.push('/');
    } catch (e: any) {
        alert("Failed to delete: " + e.message);
    }
};

const downloadCV = async () => {
    if (!store.currentCV?.id) return;
    try {
        // 1. Check permission FIRST
        await subscriptionApi.checkDownload();

        // 2. Proceed if success
        const response = await cvApi.download(store.currentCV.id);
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${store.currentCV.title || 'cv'}.pdf`);
        document.body.appendChild(link);
        link.click();
        link.remove();
    } catch (e: any) {
        // Handle 403 specially
        if (e.response && e.response.status === 403) {
            alert("Feature not available in your plan.");
        } else {
            alert("Download failed: " + (e.message || "Unknown error"));
        }
    }
};
</script>

<template>
    <!-- 1. LOADING STATE (Highest Priority) -->
    <div v-if="store.isLoading || !store.currentCV" class="h-screen flex flex-col items-center justify-center bg-gray-50 z-50 fixed inset-0">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
        <p class="text-gray-500 font-medium">Loading Editor...</p>
    </div>

    <!-- 2. ERROR STATE -->
    <div v-else-if="store.error" class="h-screen flex flex-col items-center justify-center bg-gray-50 p-6 text-center z-50 fixed inset-0">
        <div class="bg-red-100 p-4 rounded-full mb-4 text-red-600 text-3xl">⚠️</div>
        <h2 class="text-2xl font-bold text-gray-800 mb-2">Failed to Load CV</h2>
        <p class="text-red-500 mb-6 max-w-md">{{ store.error }}</p>
        <router-link to="/" class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition">
            Return to Dashboard
        </router-link>
    </div>

    <!-- 3. MAIN EDITOR CONTENT (Only renders if Loaded & No Error) -->
    <div v-else class="h-screen flex flex-col">
        <!-- Top Bar -->
        <header class="h-16 bg-white border-b flex items-center px-4 justify-between z-10 shrink-0">
            <div class="flex items-center gap-4">
                <router-link to="/" class="text-gray-500 hover:text-black">← Back to Home</router-link>
                <input 
                    v-model="store.currentCV.title" 
                    @blur="store.saveCV()"
                    class="font-bold text-lg border-transparent hover:border-gray-300 border px-2 rounded"
                />
            </div>
            
            <div class="flex items-center gap-4">
                <span v-if="store.isSaving" class="text-sm text-yellow-600">Saving...</span>
                <span v-else-if="store.lastSaved" class="text-sm text-green-600">Saved {{ store.lastSaved.toLocaleTimeString() }}</span>
                
                <!-- Share Button -->
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

        <!-- Editor Workspace (Split View) -->
        <div class="flex flex-1 overflow-hidden relative flex-col md:flex-row">
            
            <!-- Mobile Tab Controls (Sticky Top) -->
            <div class="md:hidden flex border-b bg-white z-20 shrink-0">
                <button 
                    @click="activeMobileTab = 'edit'"
                    :class="['flex-1 py-3 font-bold text-center border-b-2 transition', activeMobileTab === 'edit' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500']"
                >
                    Edit Content
                </button>
                <button 
                     @click="activeMobileTab = 'preview'"
                     :class="['flex-1 py-3 font-bold text-center border-b-2 transition', activeMobileTab === 'preview' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500']"
                >
                    Preview Mode
                </button>
            </div>

            <!-- Left Panel: Form -->
            <!-- 
                Desktop: Always Visible (w-5/12 or w-1/3)
                Mobile: Visible ONLY if activeMobileTab == 'edit' (using v-show equivalent via classes to keep state)
             -->
            <div 
                class="bg-white border-r overflow-y-auto md:w-5/12 lg:w-1/3 w-full flex-shrink-0"
                :class="{'hidden md:block': activeMobileTab !== 'edit'}"
            >
                <CVForm />
            </div>

            <!-- Right Panel: Preview -->
            <!-- 
                Desktop: Always Visible (flex-1)
                Mobile: Visible ONLY if activeMobileTab == 'preview'
             -->
            <div 
                class="bg-gray-100 overflow-hidden relative md:flex-1 w-full flex flex-col"
                :class="{'hidden md:flex': activeMobileTab !== 'preview', 'flex flex-1': activeMobileTab === 'preview'}"
            >
                <CVRenderer 
                    :html="store.currentTemplate ? store.currentTemplate.html : ''"
                    :css="store.currentTemplate ? store.currentTemplate.css : ''"
                    :data="store.currentCV.content"
                    class="flex-1"
                />
            </div>
        </div>
    </div>
</template>
