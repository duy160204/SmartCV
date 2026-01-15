<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import * as monaco from 'monaco-editor';
import CVRenderer from '@/components/core/CVRenderer.vue';

const props = defineProps<{
    initialHtml?: string;
    initialCss?: string;
}>();

const emit = defineEmits(['save']);

const htmlContent = ref(props.initialHtml || '<!-- HTML here -->');
const cssContent = ref(props.initialCss || '/* CSS here */');
const activeTab = ref<'html'|'css'>('html');

const editorContainer = ref<HTMLElement | null>(null);
let editorInstance: monaco.editor.IStandaloneCodeEditor | null = null;

// Mock Data for Preview
const mockData = ref({
    profile: {
        name: "John Doe",
        title: "Software Engineer",
        email: "john@example.com",
        phone: "+1 234 567 890",
        summary: "Top performing engineer with 5 years experience."
    },
    experience: [
        { company: "Tech Corp", position: "Senior Dev", date: "2020 - Present", description: "Built amazing things." },
        { company: "Startup Inc", position: "Junior Dev", date: "2018 - 2020", description: "Learned amazing things." }
    ],
    education: [
        { school: "MIT", degree: "BSc CS", date: "2014 - 2018" }
    ],
    skills: ["Vue", "React", "Java", "Spring Boot"]
});

onMounted(() => {
    if (editorContainer.value) {
        editorInstance = monaco.editor.create(editorContainer.value, {
            value: htmlContent.value,
            language: 'html',
            theme: 'vs-dark',
            automaticLayout: true
        });

        editorInstance.onDidChangeModelContent(() => {
            if (activeTab.value === 'html') {
                htmlContent.value = editorInstance?.getValue() || '';
            } else {
                cssContent.value = editorInstance?.getValue() || '';
            }
        });
    }
});

watch(activeTab, (newTab) => {
    if (editorInstance) {
        const newVal = newTab === 'html' ? htmlContent.value : cssContent.value;
        const newLang = newTab === 'html' ? 'html' : 'css';
        
        // Update model (simple way, better to swap models)
        editorInstance.setValue(newVal);
        monaco.editor.setModelLanguage(editorInstance.getModel()!, newLang);
    }
});

const save = () => {
    emit('save', {
        html: htmlContent.value,
        css: cssContent.value
    });
};
</script>

<template>
  <div class="flex h-full border text-sm">
      <!-- Editor Column -->
      <div class="w-1/2 flex flex-col border-r">
          <div class="flex bg-gray-800 text-white">
              <button 
                @click="activeTab = 'html'"
                :class="['px-4 py-2 hover:bg-gray-700', activeTab === 'html' ? 'bg-gray-700 font-bold' : '']"
              >HTML</button>
              <button 
                @click="activeTab = 'css'"
                :class="['px-4 py-2 hover:bg-gray-700', activeTab === 'css' ? 'bg-gray-700 font-bold' : '']"
              >CSS</button>
              <div class="flex-1"></div>
              <button @click="save" class="bg-blue-600 px-4 py-2 hover:bg-blue-500 font-bold">SAVE</button>
          </div>
          <div ref="editorContainer" class="flex-1 bg-gray-900"></div>
      </div>

      <!-- Preview Column -->
      <div class="w-1/2 bg-gray-100 flex flex-col">
          <div class="bg-white border-b px-4 py-2 font-bold text-gray-700">Live Preview</div>
          <div class="flex-1 overflow-hidden relative">
             <CVRenderer 
                :html="htmlContent"
                :css="cssContent"
                :data="mockData"
             />
          </div>
      </div>
  </div>
</template>
