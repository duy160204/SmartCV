<script setup lang="ts">
import TemplateEditor from '@/components/editor/TemplateEditor.vue';
import AITemplateModal from '@/components/editor/AITemplateModal.vue';
import api from '@/api/axios';
import { useRoute } from 'vue-router';
import { ref, onMounted, watch } from 'vue';

const route = useRoute();
const id = route.params.id === 'create' ? null : Number(route.params.id);

const html = ref('');
const css = ref('');
const name = ref('New Template');
const plan = ref('FREE');
const thumbnailUrl = ref('');
const selectedThumbnailFile = ref<File | null>(null);
const uploading = ref(false);
const configJson = ref('');

// Section selection
const availableSections = ['profile', 'experience', 'education', 'skills', 'languages', 'projects', 'certifications', 'awards', 'interests', 'references'];
const selectedSections = ref<string[]>(['profile', 'experience', 'education', 'skills']);

watch(selectedSections, (newVal) => {
    configJson.value = JSON.stringify({ sections: newVal });
}, { deep: true });

// AI state
const showAIModal = ref(false);
const aiLoading = ref(false);

const getImageUrl = (url: string | null) => {
    if (!url) return '';
    if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url;
    const backendBaseUrl = (import.meta as any).env.VITE_BACKEND_URL || '';
    return backendBaseUrl + (url.startsWith('/') ? url : '/' + url);
};

onMounted(async () => {
    if (id) {
        // Load template
        const res = await api.get(`/admin/templates/${id}`);
        // Fix: Unwrap ApiResponse
        const tmpl = res.data.data; 
        
        if (!tmpl) {
            alert("Template not found or invalid response");
            return;
        }

        name.value = tmpl.name;
        plan.value = tmpl.planRequired || 'FREE';
        thumbnailUrl.value = tmpl.thumbnailUrl || '';
        configJson.value = tmpl.configJson || '';
        
        try {
            if (configJson.value) {
                const parsedConfig = JSON.parse(configJson.value);
                if (parsedConfig.sections && Array.isArray(parsedConfig.sections)) {
                    selectedSections.value = parsedConfig.sections;
                }
            }
        } catch (e) {
            console.error("Failed to parse configJson on load", e);
        }
        
        let fullContent;
        try {
            if(typeof tmpl.fullContent === 'string') {
                 fullContent = JSON.parse(tmpl.fullContent);
            } else {
                 fullContent = tmpl.fullContent;
            }
            html.value = fullContent.html || '';
            css.value = fullContent.css || '';
        } catch (e) {
            console.error("Failed to parse template content", e);
        }
    }
});

const handleThumbnailUpload = (event: Event) => {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    selectedThumbnailFile.value = file;
    thumbnailUrl.value = URL.createObjectURL(file);
};

const handleSave = async (content: { html: string, css: string }) => {
    const payload = {
        name: name.value,
        previewContent: JSON.stringify(content), 
        fullContent: JSON.stringify(content), 
        planRequired: plan.value,
        configJson: configJson.value
    };
    
    uploading.value = true;
    try {
        let savedTemplateId = id;
        
        if (id) {
            await api.put(`/admin/templates/${id}`, payload);
        } else {
            const res = await api.post('/admin/templates', payload);
            savedTemplateId = res.data.data.id;
        }

        if (selectedThumbnailFile.value && savedTemplateId) {
            const formData = new FormData();
            formData.append('file', selectedThumbnailFile.value);
            const thumbRes = await api.post(`/admin/templates/${savedTemplateId}/thumbnail`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            thumbnailUrl.value = thumbRes.data.data;
            selectedThumbnailFile.value = null;
        }

        alert("Template saved!");
    } catch (e: any) {
        alert("Error: " + (e.response?.data?.message || e.message));
    } finally {
        uploading.value = false;
    }
};

const fileToBase64 = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result as string);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
};

const handleAIGenerate = async (file: File) => {
    showAIModal.value = false;
    aiLoading.value = true;
    try {
        const base64Str = await fileToBase64(file);
        const res = await api.post('/ai/template/build', { imageUrl: base64Str });
        const resultString = res.data.answer || res.data.content || res.data.message;
        
        let parsed;
        try {
            // Remove markdown ticks if present
            const cleanString = resultString.replace(/```json/g, '').replace(/```/g, '').trim();
            parsed = JSON.parse(cleanString);
        } catch (e) {
            console.error("Failed to parse AI output as JSON", e);
            throw new Error("AI output was not valid JSON");
        }

        if (parsed.html) html.value = parsed.html;
        if (parsed.css) css.value = parsed.css;
        if (parsed.configJson) {
            configJson.value = typeof parsed.configJson === 'string' ? parsed.configJson : JSON.stringify(parsed.configJson);
            try {
                const parsedConfig = JSON.parse(configJson.value);
                if (parsedConfig.sections && Array.isArray(parsedConfig.sections)) {
                    selectedSections.value = parsedConfig.sections;
                }
            } catch (e) {}
        }
        
        alert("AI successfully generated the template structure!");
    } catch (e: any) {
        alert("AI Generation failed: " + (e.response?.data?.message || e.message));
    } finally {
        aiLoading.value = false;
    }
};
</script>

<template>
  <div class="h-screen flex flex-col relative">
      <AITemplateModal 
          :isOpen="showAIModal" 
          @close="showAIModal = false" 
          @generate="handleAIGenerate"
      />

      <div v-if="aiLoading" class="absolute inset-0 bg-white bg-opacity-75 z-40 flex items-center justify-center">
        <div class="flex flex-col border p-6 bg-white shadow-lg rounded items-center">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600 mb-2"></div>
            <span class="text-purple-600 font-bold">✨ AI building template from image...</span>
        </div>
      </div>

      <div class="h-16 bg-white border-b flex items-center px-4 gap-4 justify-between">
          <div class="flex items-center gap-4">
              <router-link to="/templates" class="text-gray-500 hover:text-black">← Back</router-link>
              <input v-model="name" class="border px-2 py-1 rounded" placeholder="Template Name" />
              <select v-model="plan" class="border px-2 py-1 rounded">
                  <option value="FREE">FREE</option>
                  <option value="PRO">PRO</option>
                  <option value="PREMIUM">PREMIUM</option>
              </select>
          </div>
          <div class="flex items-center gap-4">
              <!-- AI Button -->
              <button @click="showAIModal = true" class="bg-purple-100 text-purple-700 px-3 py-1 font-bold rounded shadow hover:bg-purple-200">
                  ✨ AI Template Builder
              </button>

              <div class="flex items-center gap-2 border-l pl-4">
                  <span class="text-sm font-semibold">Thumbnail:</span>
                  <img v-if="thumbnailUrl" :src="getImageUrl(thumbnailUrl)" class="h-10 w-10 object-cover rounded border" alt="Thumbnail" />
                  <input type="file" ref="fileInput" accept="image/png, image/jpeg, image/webp" @change="handleThumbnailUpload" class="text-sm w-48" :disabled="uploading" />
                  <span v-if="uploading" class="text-sm text-blue-500">Saving...</span>
              </div>
          </div>
      </div>

      <!-- Config Sections Selectors -->
      <div class="bg-gray-50 border-b p-3 flex flex-wrap items-center gap-4 shrink-0">
          <span class="font-bold text-sm text-gray-700">Template Sections:</span>
          <label v-for="sec in availableSections" :key="sec" class="flex items-center gap-1 text-sm bg-white border px-2 py-1 rounded cursor-pointer hover:bg-gray-100">
              <input type="checkbox" :value="sec" v-model="selectedSections" />
              <span class="capitalize">{{ sec }}</span>
          </label>
      </div>

      <div class="flex-1 overflow-hidden">
          <TemplateEditor 
            v-if="!id || html"
            :initialHtml="html"
            :initialCss="css"
            @save="handleSave"
          />
      </div>
  </div>
</template>
