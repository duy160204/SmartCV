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
        
        // 🚀 Fix 2 & 3: Environment Context & Schema Normalization 
        if (rawData.profile) {
            // Normalize image path for absolute rendering in legacy templates
            if (rawData.profile.photo) {
                const url = rawData.profile.photo;
                if (!url.startsWith('http') && !url.startsWith('blob:') && !url.startsWith('data:')) {
                    const backendBaseUrl = (import.meta as any).env.VITE_BACKEND_URL || '';
                    rawData.profile.photo = backendBaseUrl + (url.startsWith('/') ? url : '/' + url);
                }
            }

            // Fix 3: Schema Alias: Ensure templates expecting 'dob' gracefully find normalized 'birthday'
            if (rawData.profile.birthday && !rawData.profile.dob) {
                rawData.profile.dob = rawData.profile.birthday;
            }
            if (rawData.profile.dob && !rawData.profile.birthday) {
                rawData.profile.birthday = rawData.profile.dob;
            }

            // Fix: Ensure Extras are forcibly merged BEFORE template rendering 
            // So any extra fields like custom_label directly resolve if queried at the root level
            if (typeof rawData.profile.extras === 'object' && rawData.profile.extras !== null) {
                 Object.assign(rawData.profile, rawData.profile.extras);
            }
        }
        
        // Merge extras for array sections too
        const sectionsToHydrate = ['experience', 'education', 'skills', 'projects', 'languages', 'certifications', 'awards'];
        sectionsToHydrate.forEach(sec => {
             if (Array.isArray(rawData[sec])) {
                 rawData[sec].forEach((item: any) => {
                     if (item && typeof item.extras === 'object' && item.extras !== null) {
                         Object.assign(item, item.extras);
                     }
                 });
             }
        });
        
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
// Watch the props structure dynamically so direct mutations to the deeply bound pinia proxy fire rerender
watch(
  () => props.data,
  (newVal, oldVal) => {
      render();
  },
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
