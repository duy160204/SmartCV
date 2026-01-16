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

const handleSave = async (content: { html: string, css: string }) => {
    // Controller expects @RequestBody TemplateRequestDTO (JSON)
    const payload = {
        name: name.value,
        thumbnailUrl: '', // Add thumbnail if needed, backend requires it in DTO? Check DTO. 
        // For now leave empty string if validated.
        previewContent: JSON.stringify(content), 
        fullContent: JSON.stringify(content), 
        planRequired: plan.value
    };
    
    try {
        if (id) {
            await api.put(`/admin/templates/${id}`, payload);
        } else {
            await api.post('/admin/templates', payload);
        }
        alert("Template saved!");
    } catch (e: any) {
        alert("Error: " + (e.response?.data?.message || e.message));
    }
};
</script>

<template>
  <div class="h-screen flex flex-col">
      <div class="h-14 bg-white border-b flex items-center px-4 gap-4">
          <router-link to="/templates" class="text-gray-500 hover:text-black">← Back</router-link>
          <input v-model="name" class="border px-2 py-1 rounded" placeholder="Template Name" />
          <select v-model="plan" class="border px-2 py-1 rounded">
              <option value="FREE">FREE</option>
              <option value="PRO">PRO</option>
              <option value="PREMIUM">PREMIUM</option>
          </select>
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
