<script setup lang="ts">
import TemplateEditor from '@/components/editor/TemplateEditor.vue';
import api from '@/api/axios';
import { useRoute } from 'vue-router';
import { ref, onMounted } from 'vue';

const route = useRoute();
const id = route.params.id === 'create' ? null : Number(route.params.id);

const html = ref('');
const css = ref('');
const name = ref('New Template');
const plan = ref('FREE');
const thumbnailUrl = ref('');
const selectedThumbnailFile = ref<File | null>(null);
const uploading = ref(false);
const uploadError = ref('');

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
        planRequired: plan.value
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
</script>

<template>
  <div class="h-screen flex flex-col">
      <div class="h-16 bg-white border-b flex items-center px-4 gap-4">
          <router-link to="/templates" class="text-gray-500 hover:text-black">← Back</router-link>
          <input v-model="name" class="border px-2 py-1 rounded" placeholder="Template Name" />
          <select v-model="plan" class="border px-2 py-1 rounded">
              <option value="FREE">FREE</option>
              <option value="PRO">PRO</option>
              <option value="PREMIUM">PREMIUM</option>
          </select>
          <div class="flex items-center gap-2 border-l pl-4">
              <span class="text-sm font-semibold">Thumbnail:</span>
              <img v-if="thumbnailUrl" :src="getImageUrl(thumbnailUrl)" class="h-10 w-10 object-cover rounded border" alt="Thumbnail" />
              <input type="file" accept="image/png, image/jpeg, image/webp" @change="handleThumbnailUpload" class="text-sm" :disabled="uploading" />
              <span v-if="uploading" class="text-sm text-blue-500">Saving...</span>
          </div>
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
