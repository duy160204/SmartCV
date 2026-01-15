<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
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
        const template = Handlebars.compile(props.html);
        const result = template(props.data);
        const sanitized = DOMPurify.sanitize(result);

        contentElement.innerHTML = sanitized;
        styleElement.textContent = props.css;

    } catch (e: any) {
        contentElement.innerHTML = `<div style="color:red; padding: 20px;">Template Error: ${e.message}</div>`;
    }
};

onMounted(() => {
    if (container.value) {
        shadow = container.value.attachShadow({ mode: 'open' });
        
        styleElement = document.createElement('style');
        contentElement = document.createElement('div');
        contentElement.className = 'cv-page';
        
        shadow.appendChild(styleElement);
        shadow.appendChild(contentElement);

        render();
    }
});

watch(() => [props.html, props.css, props.data], render, { deep: true });
</script>

<template>
  <div class="cv-render-wrapper flex justify-center bg-gray-200 py-8 overflow-auto h-full">
      <div 
        ref="container" 
        class="cv-host bg-white shadow-lg print:shadow-none"
        style="width: 210mm; min-height: 297mm;"
      ></div>
  </div>
</template>
