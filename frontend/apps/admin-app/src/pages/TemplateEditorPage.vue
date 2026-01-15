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
        const tmpl = res.data;
        name.value = tmpl.name;
        
        let fullContent;
        if(typeof tmpl.fullContent === 'string') {
             fullContent = JSON.parse(tmpl.fullContent);
        } else {
             fullContent = tmpl.fullContent;
        }
        
        html.value = fullContent.html;
        css.value = fullContent.css;
    }
});

const handleSave = async (content: { html: string, css: string }) => {
    const payload = {
        name: name.value,
        previewContent: JSON.stringify(content), // Simplified for now
        fullContent: JSON.stringify(content),
        planRequired: plan.value
    };
    
    // In a real app we need standard form-data or params based on Controller
    // Controller expects @RequestParam ...
    // Let's assume we adjusted controller to @RequestBody or we send params.
    // The Controller: @RequestParam String name, ...
    
    const formData = new FormData();
    formData.append('name', payload.name);
    formData.append('previewContent', payload.previewContent);
    formData.append('fullContent', payload.fullContent);
    formData.append('planRequired', payload.planRequired);

    try {
        if (id) {
            await api.put(`/admin/templates/${id}`, formData);
        } else {
            await api.post('/admin/templates', formData);
        }
        alert("Template saved!");
    } catch (e: any) {
        alert("Error: " + e.message);
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
