<script setup lang="ts">
import { ref, onMounted, watch, toRaw } from 'vue';
import Handlebars from 'handlebars';
import DOMPurify from 'dompurify';

const props = defineProps<{
  html: string;
  css: string;
  data: any;
}>();

const container = ref<HTMLElement | null>(null);
let shadow: ShadowRoot | null = null;
let styleElement: HTMLStyleElement | null = null;
let contentElement: HTMLDivElement | null = null;

const render = () => {
    if (!shadow || !styleElement || !contentElement) return;

    try {
        // Safe Compile
        if (!props.html) {
             contentElement.innerHTML = '<div style="padding: 20px; text-align: center; color: #666;">No Template Loaded</div>';
             return;
        }
        // Register helper for truncating words
        Handlebars.registerHelper('limitWords', function(str, limit) {
             if (typeof str !== 'string') return '';
             var words = str.split(' ');
             if (words.length > limit) {
                 return words.slice(0, limit).join(' ') + '...';
             }
             return str;
        });
        
        // Prepare template - automatically truncating fields based on constraints
        // Using regex to replace {{summary}} to {{limitWords summary 80}}, etc.
        let processedHtml = props.html
            .replace(/\{\{\s*profile\.summary\s*\}\}/g, '{{limitWords profile.summary 80}}')
            .replace(/\{\{\s*description\s*\}\}/g, '{{limitWords description 60}}');

        const template = Handlebars.compile(processedHtml);
        
        console.warn('-------- CVRenderer Debug --------');
        console.warn('HTML Template Sample:', props.html.substring(0, 500));
        console.warn('Data Received:', props.data);
        console.warn('Has Profile Name?', props.data?.profile?.name);
        
        // 2️⃣ Sanitize Reactive Data Before Handlebars (CRITICAL)
        // Convert Vue Proxy -> Plain Object snapshot to ensure Handlebars receives clean data
        // structuredClone ensures a deep copy without Vue reactivity pointers
        const rawData = props.data ? structuredClone(toRaw(props.data)) : {};
        
        const result = template(rawData);
        
        // Sanitize HTML (Allowing style attributes might be needed for some templates, but safer to block scripts)
        // DOMPurify defaults are good (no <script>, no onerror, etc)
        const sanitized = DOMPurify.sanitize(result);

        // Update content
        contentElement.innerHTML = sanitized;
        
        // Ensure constraints CSS
        const constraintCss = `
            .cv-container { width:210mm; min-height:297mm; padding:20mm; box-sizing:border-box; }
            section { margin-bottom:18px; }
            .item { page-break-inside:avoid; }
            body { margin: 0; padding: 0; }
        `;
        // Update CSS
        styleElement.textContent = constraintCss + '\n' + props.css;

    } catch (e: any) {
        contentElement.innerHTML = `<div style="color:red; padding: 20px;">Template Error: ${e.message}</div>`;
    }
};

onMounted(() => {
    if (container.value) {
        shadow = container.value.attachShadow({ mode: 'open' });
        
        // Create permanent elements in shadow dom to update efficiently
        styleElement = document.createElement('style');
        contentElement = document.createElement('div');
        // Add a wrapper class often used by A4
        contentElement.className = 'cv-container';
        
        shadow.appendChild(styleElement);
        shadow.appendChild(contentElement);

        render();
    }
});

// 1️⃣ Fix Watch Strategy (CRITICAL)
// Watch data deeply and explicitly to catch nested changes
watch(
  () => props.data,
  render,
  { deep: true }
);

// Watch templates separately (shallow change is enough for strings)
watch(
  () => [props.html, props.css],
  render
);
</script>

<template>
  <div class="cv-render-wrapper flex justify-center bg-gray-200 py-8 overflow-auto h-full">
      <!-- Host for Shadow DOM -->
      <div 
        ref="container" 
        class="cv-host bg-white shadow-lg print:shadow-none"
        style="width: 210mm; min-height: 297mm;"
      ></div>
  </div>
</template>

<style scoped>
/* Scoped styles for the wrapper, NOT the CV content */
</style>
