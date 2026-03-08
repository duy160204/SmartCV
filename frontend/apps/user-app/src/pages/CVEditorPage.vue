<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCVStore } from '@/stores/cv';
import CVRenderer from '@/components/core/CVRenderer.vue';
import CVForm from '@/components/core/CVForm.vue';
import AIGeneratorModal from '@/components/core/AIGeneratorModal.vue';
import { cvApi, subscriptionApi } from '@/api/user.api';

const route = useRoute();
const router = useRouter();
const store = useCVStore();

const activeMobileTab = ref<'edit' | 'preview'>('edit');
const activeFormTab = ref('profile');
const isAIGeneratorOpen = ref(false);

const configSections = computed(() => {
    try {
        if (store.currentTemplate && store.currentTemplate.configJson) {
            const parsed = typeof store.currentTemplate.configJson === 'string' 
                ? JSON.parse(store.currentTemplate.configJson) 
                : store.currentTemplate.configJson;
            if (parsed && parsed.sections && Array.isArray(parsed.sections)) {
                return parsed.sections;
            }
        }
    } catch (e) {
        console.error("Failed to parse configJson for sections", e);
    }
    // Fallback default sections
    return ['profile', 'experience', 'education', 'skills', 'projects', 'languages', 'certifications', 'awards'];
});

onMounted(() => {
    const id = Number(route.params.id);
    if (id) {
        store.loadCV(id);
    }
});

const handleAIGenerate = async (prompt: string) => {
    isAIGeneratorOpen.value = false;
    try {
        await store.generateCv(prompt);
    } catch (e: any) {
        alert("Failed to generate CV: " + e.message);
    }
};

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
    <!-- AI Generator Modal -->
    <AIGeneratorModal 
        :isOpen="isAIGeneratorOpen" 
        @close="isAIGeneratorOpen = false" 
        @generate="handleAIGenerate"
    />

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

            <!-- New Sidebar: Sections & AI Assistant Panel (Desktop Only) -->
            <div class="bg-gray-50 border-r w-48 flex-shrink-0 hidden md:flex flex-col">
                <div class="p-4 border-b font-bold text-gray-700">CV Sections</div>
                
                <div class="flex-1 overflow-y-auto p-2 space-y-1">
                    <button 
                        v-for="section in configSections" 
                        :key="section"
                        @click="activeFormTab = section"
                        :class="[
                            'w-full text-left px-3 py-2 rounded capitalize font-medium transition duration-200',
                            activeFormTab === section ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-200'
                        ]"
                    >
                        {{ section }}
                    </button>
                </div>

                <!-- AI Assistant Panel -->
                <div class="p-4 border-t bg-purple-50">
                    <p class="text-xs text-purple-800 font-bold mb-2">AI Assistant Panel</p>
                    <button @click="isAIGeneratorOpen = true" class="w-full bg-purple-600 text-white py-2 rounded shadow hover:bg-purple-700 font-medium transition duration-200 flex items-center justify-center gap-2">
                        <span>✨ Generate with AI</span>
                    </button>
                </div>
            </div>

            <!-- Middle Panel: Form -->
            <div 
                class="bg-white border-r overflow-hidden md:w-5/12 lg:w-1/3 w-full flex-shrink-0 flex flex-col"
                :class="{'hidden md:flex': activeMobileTab !== 'edit'}"
            >
                <!-- Mobile Sections Tabs -->
                <div class="md:hidden flex overflow-x-auto border-b bg-gray-50 p-2 space-x-2 shrink-0">
                    <button 
                        v-for="section in configSections" 
                        :key="section"
                        @click="activeFormTab = section"
                        :class="[
                            'px-3 py-1 rounded capitalize text-sm whitespace-nowrap',
                            activeFormTab === section ? 'bg-blue-100 text-blue-700 font-bold' : 'bg-gray-200 text-gray-600'
                        ]"
                    >{{ section }}</button>
                </div>
                <!-- Mobile Generate AI Button -->
                <div class="md:hidden p-2 bg-purple-50 shrink-0">
                    <button @click="isAIGeneratorOpen = true" class="w-full bg-purple-600 text-white text-sm py-2 rounded shadow flex items-center justify-center gap-2">
                         <span>✨ Generate with AI</span>
                    </button>
                </div>

                <div class="flex-1 overflow-visible">
                    <CVForm :activeTab="activeFormTab" />
                </div>
            </div>

            <!-- Right Panel: Preview -->
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
