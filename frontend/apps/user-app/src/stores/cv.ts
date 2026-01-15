import { defineStore } from 'pinia';
import { ref, reactive, watch } from 'vue';
import api from '@/api/axios';
import { useRoute } from 'vue-router';

// Types (Simplified for now)
interface CV {
    id: number;
    title: string;
    content: any; // JSON Object
    templateId: number;
    status: string;
    isPublic?: boolean;
    publicToken?: string;
}

interface Template {
    id: number;
    fullContent: string; // JSON String {html, css, meta}
}

export const useCVStore = defineStore('cv', () => {
    const currentCV = ref<CV | null>(null);
    const currentTemplate = ref<any | null>(null);

    // UI States
    const isLoading = ref(false);
    const isSaving = ref(false);
    const lastSaved = ref<Date | null>(null);

    // Load CV by ID
    async function loadCV(id: number) {
        isLoading.value = true;
        try {
            const res = await api.get(`/cv/${id}`);
            const cvData = res.data;

            // Parse content if string, though backend says it stores JSON, 
            // if it returns object, good. If string, parse.
            if (typeof cvData.content === 'string') {
                cvData.content = JSON.parse(cvData.content);
            }

            currentCV.value = cvData;

            // Load Template
            await loadTemplate(cvData.templateId);
        } catch (e) {
            console.error(e);
        } finally {
            isLoading.value = false;
        }
    }

    // Load Template by ID
    async function loadTemplate(id: number) {
        try {
            const res = await api.get(`/templates/${id}`);
            const tmpl = res.data;
            // Parse fullContent
            if (typeof tmpl.fullContent === 'string') {
                currentTemplate.value = JSON.parse(tmpl.fullContent);
            } else {
                currentTemplate.value = tmpl.fullContent;
            }
        } catch (e) {
            console.error("Failed to load template", e);
            // Fallback
            currentTemplate.value = { html: '<h1>Error loading template</h1>', css: '' };
        }
    }

    // Autosave Logic
    let saveTimeout: NodeJS.Timeout;

    // Watch for deep changes in content
    watch(() => currentCV.value?.content, (newVal) => {
        if (!newVal) return;

        // Set saving state UI
        isSaving.value = true;

        clearTimeout(saveTimeout);
        saveTimeout = setTimeout(async () => {
            await saveCV();
        }, 2000); // 2s debounce

    }, { deep: true });

    async function saveCV() {
        if (!currentCV.value) return;

        try {
            isSaving.value = true;
            // PATCH /api/cv/{id}/autosave
            // Body: { content: JSON.stringify(content) } if backend expects string map
            // Controller: "String content = body.get("content");"
            // So we must send { "content": JSON.stringify(obj) }

            const payload = {
                content: JSON.stringify(currentCV.value.content)
            };

            await api.patch(`/cv/${currentCV.value.id}/autosave`, payload);

            lastSaved.value = new Date();
        } catch (e) {
            console.error("Autosave failed", e);
        } finally {
            isSaving.value = false;
        }
    }

    // AI Chat Integration
    async function improveText(fieldPath: string, text: string, instruction: string) {
        if (!currentCV.value) return;

        // 1. Force Save first
        await saveCV();

        try {
            isSaving.value = true; // reusing saving indicator or add isProcessingAI
            const res = await api.post('/ai/cv/chat', {
                cvId: currentCV.value.id,
                message: `Original text: "${text}". Instruction: ${instruction}. Return ONLY the improved text.`
            });

            return res.data.message; // Assume response wrapper
        } finally {
            isSaving.value = false;
        }
    }

    // Share CV
    async function publishCV() {
        if (!currentCV.value) return;
        try {
            const res = await api.post(`/subscription/cv/${currentCV.value.id}/public`);
            // Assume response returns { token: "...", publicUrl: "..." }
            // Or just success and we construct url. 
            // Let's assume backend updates CV object or returns token.
            // If backend follows REST, maybe it returns the updated CV resource or a DTO.
            // Updating local state:
            currentCV.value.isPublic = true;
            currentCV.value.publicToken = res.data.token || "generated-token";
        } catch (e: any) {
            alert("Failed to publish: " + e.message);
        }
    }

    async function unpublishCV() {
        if (!currentCV.value) return;
        try {
            await api.delete(`/subscription/cv/${currentCV.value.id}/public`);
            currentCV.value.isPublic = false;
            currentCV.value.publicToken = undefined;
        } catch (e: any) {
            alert("Failed to unpublish: " + e.message);
        }
    }

    return {
        currentCV,
        currentTemplate,
        isLoading,
        isSaving,
        lastSaved,
        loadCV,
        saveCV,
        improveText,
        publishCV,
        unpublishCV
    };
});
