import { defineStore } from 'pinia';
import { ref, reactive, watch } from 'vue';
import { cvApi, templateApi, aiApi, subscriptionApi } from '@/api/user.api';
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
    const error = ref<string | null>(null);
    const isSaving = ref(false);
    const lastSaved = ref<Date | null>(null);

    // Helper: Normalize CV Data Structure
    function normalizeCV(cv: any) {
        if (!cv.content || typeof cv.content !== 'object') {
            cv.content = {};
        }

        // Ensure Profile
        if (!cv.content.profile) {
            cv.content.profile = {};
        }
        const defaultProfile = {
            name: '',
            title: '',
            email: '',
            phone: '',
            website: '',
            location: '',
            summary: '',
            photo: '',
            gender: '',
            dob: '',
            address: ''
        };
        cv.content.profile = { ...defaultProfile, ...cv.content.profile };

        // Ensure Top-Level Strings
        if (typeof cv.content.careerObjective !== 'string') cv.content.careerObjective = '';
        if (typeof cv.content.interests !== 'string') cv.content.interests = '';

        // Ensure Arrays
        const arraySections = ['experience', 'education', 'skills', 'languages', 'references'];
        arraySections.forEach(sec => {
            if (!Array.isArray(cv.content[sec])) {
                cv.content[sec] = [];
            }
        });

        // Normalize Dates
        cv.content.experience.forEach((e: any) => {
            if (e.date) { e.startDate = e.date; delete e.date; }
            if (!e.startDate) e.startDate = '';
            if (!e.endDate) e.endDate = '';
        });
        cv.content.education.forEach((e: any) => {
            if (e.date) { e.startDate = e.date; delete e.date; }
            if (!e.startDate) e.startDate = '';
            if (!e.endDate) e.endDate = '';
            if (!e.major) e.major = '';
        });

        return cv;
    }

    // Load CV by ID
    async function loadCV(id: number) {
        isLoading.value = true;
        error.value = null; // Reset error
        try {
            const res = await cvApi.getById(id);
            const cvData = res.data;

            // Parse content if string
            if (typeof cvData.content === 'string') {
                try {
                    cvData.content = JSON.parse(cvData.content);
                } catch (e) {
                    console.error("Failed to parse JSON content", e);
                    cvData.content = {};
                }
            }

            // Normalization (CRITICAL FIX)
            normalizeCV(cvData);

            currentCV.value = cvData;

            // Load Template
            await loadTemplate(cvData.templateId);
        } catch (e: any) {
            console.error(e);
            error.value = e.response?.data?.message || e.message || "Failed to load CV";
        } finally {
            isLoading.value = false;
        }
    }

    // Load Template by ID
    async function loadTemplate(id: number) {
        try {
            const res = await templateApi.getById(id);
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
            // Use cvApi which handles serialization automatically
            await cvApi.autosave(currentCV.value.id, currentCV.value.content);

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
            isSaving.value = true;
            const res = await aiApi.improveText(text, instruction);

            return res.data.answer || res.data.message || res.data.content;
        } finally {
            isSaving.value = false;
        }
    }

    async function generateCv(prompt: string) {
        if (!currentCV.value || !currentTemplate.value) return;

        try {
            isLoading.value = true;
            const configJson = currentTemplate.value.configJson || JSON.stringify({ sections: ['profile', 'experience', 'education', 'skills'] });
            const res = await aiApi.generateCv(prompt, typeof configJson === 'string' ? configJson : JSON.stringify(configJson));

            // Expected to return JSON string in content
            let generatedData = res.data.answer || res.data.message || res.data.content;
            if (typeof generatedData === 'string') {
                // remove markdown ticks if any
                generatedData = generatedData.replace(/```json/g, '').replace(/```/g, '').trim();
                const parsedData = JSON.parse(generatedData);

                // Merge into currentCV content safely
                currentCV.value.content = {
                    ...currentCV.value.content,
                    ...parsedData
                };
                normalizeCV(currentCV.value);
                await saveCV();
            }
        } catch (e: any) {
            console.error("AI Generation JSON parse or endpoint error", e);
            throw new Error(e.response?.data?.message || e.message || "Failed to generate CV");
        } finally {
            isLoading.value = false;
        }
    }

    // Share CV
    async function publishCV() {
        if (!currentCV.value) return;
        try {
            const res = await subscriptionApi.publicCV(currentCV.value.id);
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
            await subscriptionApi.revokePublicLink(currentCV.value.id);
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
        error,
        isSaving,
        lastSaved,
        loadCV,
        saveCV,
        improveText,
        generateCv,
        publishCV,
        unpublishCV
    };
});
