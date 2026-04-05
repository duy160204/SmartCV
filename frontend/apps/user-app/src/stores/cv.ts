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

        if (!cv.content.profile) {
            cv.content.profile = {};
        }
        if (!cv.content.profile.extras) {
            cv.content.profile.extras = {};
        }

        // Clean up invalid legacy fields to valid ones
        if (cv.content.careerObjective) {
            if (!cv.content.profile.summary) {
                cv.content.profile.summary = cv.content.careerObjective;
            }
            delete cv.content.careerObjective;
        }
        if (cv.content.certificates) {
            if (!cv.content.certifications) {
                cv.content.certifications = cv.content.certificates;
            }
            delete cv.content.certificates;
        }
        if (cv.content.profile && cv.content.profile.dob) {
            if (!cv.content.profile.birthday) {
                cv.content.profile.birthday = cv.content.profile.dob;
            }
            delete cv.content.profile.dob;
        }
        if (cv.content.profile && cv.content.profile.birthdate) {
            if (!cv.content.profile.birthday) {
                cv.content.profile.birthday = cv.content.profile.birthdate;
            }
            delete cv.content.profile.birthdate;
        }

        // Move unknown profile fields to extras
        const coreProfileFields = ['name', 'title', 'email', 'phone', 'website', 'location', 'summary', 'photo', 'gender', 'birthday', 'address', 'extras'];
        Object.keys(cv.content.profile).forEach(key => {
            if (!coreProfileFields.includes(key) && key !== 'dob' && key !== 'birthdate' && key !== 'dateOfBirth') {
                cv.content.profile.extras[key] = cv.content.profile[key];
                delete cv.content.profile[key];
            }
        });

        // Ensure Profile defaults for UI
        const defaultProfile = {
            name: '', title: '', email: '', phone: '', website: '', location: '', summary: '', photo: '', gender: '', birthday: '', address: ''
        };
        cv.content.profile = { ...defaultProfile, ...cv.content.profile };

        // Ensure Arrays
        if (typeof cv.content.interests === 'string') {
            cv.content.interests = cv.content.interests.split(',').map((i: string) => i.trim()).filter(Boolean);
        }
        // Ensure all collections are instantiated as Arrays specifically
        const arraySections = ['experience', 'education', 'skills', 'languages', 'references', 'projects', 'certifications', 'awards', 'interests'];
        arraySections.forEach(sec => {
            if (!Array.isArray(cv.content[sec])) {
                cv.content[sec] = [];
            }
        });

        // Normalize Dates
        cv.content.experience.forEach((e: any) => {
            if (e.startDate) { e.date = e.startDate; delete e.startDate; }
            if (e.endDate) { delete e.endDate; }
            if (!e.date) e.date = '';
        });
        cv.content.education.forEach((e: any) => {
            if (e.startDate) { e.date = e.startDate; delete e.startDate; }
            if (e.endDate) { delete e.endDate; }
            if (!e.date) e.date = '';
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

    function buildPayload(form: any) {
        return {
            profile: {
                name: form.profile?.name || '', title: form.profile?.title || '', email: form.profile?.email || '',
                phone: form.profile?.phone || '', website: form.profile?.website || '', location: form.profile?.location || '',
                summary: form.profile?.summary || form.careerObjective || '', photo: form.profile?.photo || '', gender: form.profile?.gender || '',
                birthday: form.profile?.birthday || form.profile?.dob || form.profile?.birthdate || '', address: form.profile?.address || '',
                extras: form.profile?.extras || {}
            },
            experience: Array.isArray(form.experience) ? form.experience.map((e: any) => ({
                company: e.company || '', position: e.position || '', date: e.date || '', description: e.description || '', extras: e.extras || {}
            })) : [],
            skills: Array.isArray(form.skills) ? form.skills.map((s: any) => ({ name: s.name || '', level: s.level || '', extras: s.extras || {} })) : [],
            projects: Array.isArray(form.projects) ? form.projects.map((p: any) => ({ name: p.name || '', role: p.role || '', date: p.date || '', description: p.description || '', link: p.link || '', extras: p.extras || {} })) : [],
            languages: Array.isArray(form.languages) ? form.languages.map((l: any) => ({ language: l.language || '', proficiency: l.proficiency || '', extras: l.extras || {} })) : [],
            certifications: Array.isArray(form.certifications) ? form.certifications.map((c: any) => ({ name: c.name || '', issuer: c.issuer || '', date: c.date || '', extras: c.extras || {} })) : [],
            awards: Array.isArray(form.awards) ? form.awards.map((a: any) => ({ name: a.name || '', issuer: a.issuer || '', year: a.year || '', extras: a.extras || {} })) : [],
            education: Array.isArray(form.education) ? form.education.map((e: any) => ({ school: e.school || '', degree: e.degree || '', major: e.major || '', date: e.date || '', extras: e.extras || {} })) : [],
            interests: Array.isArray(form.interests) ? form.interests : (form.interests ? [form.interests] : []),
            references: Array.isArray(form.references) ? form.references.map((r: any) => ({ name: r.name || '', position: r.position || '', company: r.company || '', contact: r.contact || '', extras: r.extras || {} })) : []
        };
    }

    async function saveCV() {
        if (!currentCV.value) return;

        try {
            isSaving.value = true;
            await cvApi.autosave(currentCV.value.id, buildPayload(currentCV.value.content));

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
