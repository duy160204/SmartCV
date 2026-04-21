<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCVStore } from '@/stores/cv';
import CVRenderer from '@/components/core/CVRenderer.vue';
import CVForm from '@/components/core/CVForm.vue';
import AIGeneratorModal from '@/components/core/AIGeneratorModal.vue';
import { cvApi, subscriptionApi } from '@/api/user.api';

const route  = useRoute();
const router = useRouter();
const store  = useCVStore();

// ─── Tab State ─────────────────────────────────────────────────────────────
const activeMobileTab = ref<'edit' | 'preview'>('edit');
const activeFormTab   = ref('profile');
const isAIGeneratorOpen = ref(false);

// ─── Config Sections ───────────────────────────────────────────────────────
const configSections = computed(() => {
    try {
        if (store.currentTemplate?.configJson) {
            const parsed = typeof store.currentTemplate.configJson === 'string'
                ? JSON.parse(store.currentTemplate.configJson)
                : store.currentTemplate.configJson;
            if (parsed?.sections && Array.isArray(parsed.sections)) return parsed.sections;
        }
    } catch (e) {
        console.error('Failed to parse configJson for sections', e);
    }
    return ['profile', 'experience', 'education', 'skills', 'projects', 'languages', 'certifications', 'awards'];
});

onMounted(() => {
    const id = Number(route.params.id);
    if (id) store.loadCV(id);
});

// ─── Actions ───────────────────────────────────────────────────────────────
const handleAIGenerate = async (prompt: string) => {
    isAIGeneratorOpen.value = false;
    try {
        await store.generateCv(prompt);
    } catch (e: any) {
        alert('Failed to generate CV: ' + e.message);
    }
};

const deleteCV = async () => {
    if (!store.currentCV?.id) return;
    if (!confirm('Are you sure you want to delete this CV? This cannot be undone.')) return;
    try {
        await cvApi.delete(store.currentCV.id);
        router.push('/');
    } catch (e: any) {
        alert('Failed to delete: ' + e.message);
    }
};

const downloadCV = async () => {
    if (!store.currentCV?.id) return;
    try {
        await subscriptionApi.checkDownload();
        const response = await cvApi.download(store.currentCV.id);
        const url  = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${store.currentCV.title || 'cv'}.pdf`);
        document.body.appendChild(link);
        link.click();
        link.remove();
    } catch (e: any) {
        if (e.response?.status === 403) {
            alert('Feature not available in your plan.');
        } else {
            alert('Download failed: ' + (e.message || 'Unknown error'));
        }
    }
};

// ─── Resizable Split View ──────────────────────────────────────────────────
/**
 * Architecture:
 *   workspaceRef (flex row, w-full, h-full, overflow:hidden)
 *     ├─ leftPanel  (flex-shrink-0, width: leftWidthPx, overflow-y-auto)
 *     ├─ divider    (flex-shrink-0, width: 5px, cursor: col-resize)
 *     └─ rightPanel (flex: 1, overflow:hidden → CVRenderer manages its own scroll)
 *
 * State: leftWidthPx (px).
 * On drag: clamp [LEFT_MIN, total - DIVIDER_W - RIGHT_MIN].
 * Double-click divider → reset 50/50.
 *
 * No transform on editor. No zoom. No scale on editor side.
 * CVRenderer reads its own container width via ResizeObserver → computes scale independently.
 */

const DIVIDER_W  = 5;   // px
const LEFT_MIN   = 300; // px
const RIGHT_MIN  = 400; // px

const workspaceRef  = ref<HTMLElement | null>(null);
const leftWidthPx   = ref(0); // set after mount to 40% of workspace
const isDragging    = ref(false);
let   dragStartX    = 0;
let   dragStartW    = 0;

onMounted(() => {
    if (workspaceRef.value) {
        // Initialize: left panel = 40% of workspace
        leftWidthPx.value = Math.round(workspaceRef.value.clientWidth * 0.40);
    }
});

// ─── Drag handlers (attached to document so they fire even outside divider) ─
const onMouseMove = (e: MouseEvent) => {
    if (!isDragging.value || !workspaceRef.value) return;
    const totalW = workspaceRef.value.clientWidth;
    const maxLeft = totalW - DIVIDER_W - RIGHT_MIN;
    const delta  = e.clientX - dragStartX;
    leftWidthPx.value = Math.max(LEFT_MIN, Math.min(maxLeft, dragStartW + delta));
};

const onMouseUp = () => {
    if (!isDragging.value) return;
    isDragging.value = false;
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    // Re-enable text selection
    document.body.style.userSelect = '';
};

const startDrag = (e: MouseEvent) => {
    e.preventDefault();
    isDragging.value = true;
    dragStartX = e.clientX;
    dragStartW = leftWidthPx.value;
    // Disable text selection while dragging
    document.body.style.userSelect = 'none';
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup',   onMouseUp);
};

const resetSplit = () => {
    if (!workspaceRef.value) return;
    leftWidthPx.value = Math.round(workspaceRef.value.clientWidth * 0.50);
};

// Cleanup on unmount (safety net in case mouseup fires outside)
onUnmounted(() => {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup',   onMouseUp);
    document.body.style.userSelect = '';
});
</script>

<template>
    <!-- AI Generator Modal -->
    <AIGeneratorModal
        :isOpen="isAIGeneratorOpen"
        @close="isAIGeneratorOpen = false"
        @generate="handleAIGenerate"
    />

    <!-- LOADING STATE -->
    <div v-if="store.isLoading || !store.currentCV"
         class="h-screen flex flex-col items-center justify-center bg-gray-50 fixed inset-0 z-50">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
        <p class="text-gray-500 font-medium">Loading Editor...</p>
    </div>

    <!-- ERROR STATE -->
    <div v-else-if="store.error"
         class="h-screen flex flex-col items-center justify-center bg-gray-50 p-6 text-center fixed inset-0 z-50">
        <div class="bg-red-100 p-4 rounded-full mb-4 text-red-600 text-3xl">⚠️</div>
        <h2 class="text-2xl font-bold text-gray-800 mb-2">Failed to Load CV</h2>
        <p class="text-red-500 mb-6 max-w-md">{{ store.error }}</p>
        <router-link to="/" class="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition">
            Return to Dashboard
        </router-link>
    </div>

    <!-- MAIN EDITOR -->
    <div v-else class="h-screen flex flex-col overflow-hidden">

        <!-- ── Top Bar ──────────────────────────────────────────────────── -->
        <header class="h-16 bg-white border-b flex items-center px-4 justify-between z-10 flex-shrink-0">
            <div class="flex items-center gap-4">
                <router-link to="/" class="text-gray-500 hover:text-black">← Back</router-link>
                <input
                    v-model="store.currentCV.title"
                    @blur="store.saveCV()"
                    class="font-bold text-lg border-transparent hover:border-gray-300 border px-2 rounded"
                />
            </div>

            <div class="flex items-center gap-4">
                <span v-if="store.isSaving"   class="text-sm text-yellow-600">Saving...</span>
                <span v-else-if="store.lastSaved" class="text-sm text-green-600">
                    Saved {{ store.lastSaved.toLocaleTimeString() }}
                </span>

                <!-- Share dropdown -->
                <div class="relative group">
                    <button class="text-gray-600 hover:text-blue-600 font-medium">Share</button>
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

                <button @click="deleteCV"   class="text-gray-500 hover:text-red-600 font-medium">Delete</button>
                <button @click="downloadCV" class="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700">Download PDF</button>
            </div>
        </header>

        <!-- ── Mobile Tab Switcher ─────────────────────────────────────── -->
        <div class="md:hidden flex border-b bg-white flex-shrink-0 z-10">
            <button
                @click="activeMobileTab = 'edit'"
                :class="['flex-1 py-3 font-bold text-center border-b-2 transition',
                    activeMobileTab === 'edit' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500']"
            >Edit Content</button>
            <button
                @click="activeMobileTab = 'preview'"
                :class="['flex-1 py-3 font-bold text-center border-b-2 transition',
                    activeMobileTab === 'preview' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500']"
            >Preview</button>
        </div>

        <!-- ── Workspace (Desktop split | Mobile single) ───────────────── -->
        <!--
            KEY RULES:
            - flex-1 + overflow-hidden: workspace fills remaining height exactly.
            - leftPanel: fixed width (leftWidthPx), overflow-y-auto → normal scroll.
            - divider: 5px, flex-shrink-0, cursor: col-resize.
            - rightPanel: flex-1 (takes remaining), overflow:hidden (CVRenderer scrolls internally).
        -->
        <div
            ref="workspaceRef"
            class="flex-1 flex overflow-hidden"
            :class="{ 'select-none': isDragging }"
        >

            <!-- ── Sections Sidebar (desktop only) ────────────────────── -->
            <div class="hidden md:flex flex-col flex-shrink-0 bg-gray-50 border-r"
                 style="width: 192px;">
                <div class="p-4 border-b font-bold text-gray-700 text-sm flex-shrink-0">CV Sections</div>
                <div class="flex-1 overflow-y-auto p-2 space-y-1">
                    <button
                        v-for="section in configSections"
                        :key="section"
                        @click="activeFormTab = section"
                        :class="[
                            'w-full text-left px-3 py-2 rounded capitalize font-medium text-sm transition',
                            activeFormTab === section
                                ? 'bg-blue-100 text-blue-700'
                                : 'text-gray-600 hover:bg-gray-200'
                        ]"
                    >{{ section }}</button>
                </div>
                <!-- AI button -->
                <div class="p-4 border-t bg-purple-50 flex-shrink-0">
                    <p class="text-xs text-purple-800 font-bold mb-2">AI Assistant</p>
                    <button
                        @click="isAIGeneratorOpen = true"
                        class="w-full bg-purple-600 text-white py-2 rounded shadow hover:bg-purple-700 font-medium transition text-sm flex items-center justify-center gap-1"
                    >✨ Generate with AI</button>
                </div>
            </div>

            <!-- ── Left Panel: Editor ──────────────────────────────────── -->
            <!--
                - flex-shrink-0 so it never collapses by flex rules.
                - overflow-y-auto + overflow-x-hidden: normal scroll, no overflow lỗi.
                - NO transform, NO scale, NO zoom.
                - Width controlled by leftWidthPx state (drag → update → repaint).
            -->
            <div
                class="flex-shrink-0 flex flex-col overflow-hidden bg-white border-r"
                :class="{ 'hidden': activeMobileTab !== 'edit', 'md:flex': true }"
                :style="{ width: `${leftWidthPx}px` }"
            >
                <!-- Mobile section tabs -->
                <div class="md:hidden flex overflow-x-auto border-b bg-gray-50 p-2 gap-2 flex-shrink-0">
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
                <!-- Mobile AI button -->
                <div class="md:hidden p-2 bg-purple-50 flex-shrink-0">
                    <button @click="isAIGeneratorOpen = true"
                            class="w-full bg-purple-600 text-white text-sm py-2 rounded shadow flex items-center justify-center gap-2">
                        ✨ Generate with AI
                    </button>
                </div>

                <!--
                    Editor scroll area.
                    overflow-y-auto here (not on CVForm) = single scroll context, no double-scroll.
                -->
                <div class="flex-1 overflow-y-auto overflow-x-hidden">
                    <CVForm :activeTab="activeFormTab" />
                </div>
            </div>

            <!-- ── Divider ────────────────────────────────────────────── -->
            <!--
                - hidden on mobile, shown only md+.
                - mousedown starts drag.
                - dblclick resets to 50/50.
                - cursor: col-resize signals drag capability.
                - No transition on divider itself to avoid lag.
            -->
            <div
                class="hidden md:flex flex-shrink-0 items-center justify-center bg-gray-200 hover:bg-blue-300 active:bg-blue-400 transition-colors cursor-col-resize group relative z-10"
                style="width: 5px;"
                @mousedown.prevent="startDrag"
                @dblclick="resetSplit"
                title="Drag to resize · Double-click to reset"
            >
                <!-- Visual handle dots -->
                <div class="flex flex-col gap-1 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                    <span class="w-1 h-1 rounded-full bg-blue-600 block"></span>
                    <span class="w-1 h-1 rounded-full bg-blue-600 block"></span>
                    <span class="w-1 h-1 rounded-full bg-blue-600 block"></span>
                </div>
            </div>

            <!-- ── Right Panel: Preview ────────────────────────────────── -->
            <!--
                - flex-1 takes all remaining width after left panel + divider.
                - overflow:hidden here because CVRenderer.vue itself is overflow-y-auto (it has its own scroll).
                - CVRenderer reads ITS OWN clientWidth via ResizeObserver → computes scale.
                  No width is passed down as prop; it's fully self-contained.
            -->
            <div
                class="flex-1 overflow-hidden flex flex-col"
                :class="{ 'hidden': activeMobileTab !== 'preview', 'md:flex': true }"
            >
                <CVRenderer
                    :html="store.currentTemplate?.html ?? ''"
                    :css="store.currentTemplate?.css ?? ''"
                    :data="store.currentCV.content"
                    class="flex-1 min-h-0"
                />
            </div>
        </div>
    </div>
</template>
