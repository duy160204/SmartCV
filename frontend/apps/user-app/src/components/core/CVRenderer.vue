<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, toRaw } from 'vue';
import Handlebars from 'handlebars';
import DOMPurify from 'dompurify';

const props = defineProps<{
  html: string;
  css: string;
  data: any;
}>();

// A4 at 96dpi = 794px wide
const A4_WIDTH_PX = 794;

const panelRef    = ref<HTMLElement | null>(null);  // The scrollable outer panel
const a4HostRef   = ref<HTMLElement | null>(null);  // The 210mm × 297mm A4 element
const scale       = ref(1);
const scaledHeight = ref(1123); // 297mm ≈ 1123px default

let shadow: ShadowRoot | null = null;
let styleEl: HTMLStyleElement | null = null;
let contentEl: HTMLDivElement | null = null;
let ro: ResizeObserver | null = null;

// ─── Scale Calculation ─────────────────────────────────────────────────────
// Called whenever the RIGHT PANEL resizes.
// We read the panel width, compute scale so A4 fits without overflow.
// The A4 element itself never changes width — only transform:scale on a wrapper.
const recalcScale = () => {
  if (!panelRef.value) return;
  const panelW = panelRef.value.clientWidth;
  // padding 32px total (16px each side)
  const available = panelW - 32;
  scale.value = available < A4_WIDTH_PX ? available / A4_WIDTH_PX : 1;
};

// ─── Render Logic ──────────────────────────────────────────────────────────
const render = () => {
  if (!shadow || !styleEl || !contentEl) return;

  try {
    if (!props.html) {
      contentEl.innerHTML = '<div style="padding:20px;text-align:center;color:#999;">No Template Loaded</div>';
      return;
    }

    // Handlebars helpers (idempotent registration)
    Handlebars.registerHelper('limitWords', function(str: any, limit: number) {
      if (typeof str !== 'string') return '';
      const words = str.split(' ');
      return words.length > limit ? words.slice(0, limit).join(' ') + '...' : str;
    });

    const processedHtml = props.html
      .replace(/\{\{\s*profile\.summary\s*\}\}/g, '{{limitWords profile.summary 80}}')
      .replace(/\{\{\s*description\s*\}\}/g,      '{{limitWords description 60}}');

    const template = Handlebars.compile(processedHtml);

    // Deep-clone to strip Vue Proxy
    const rawData = props.data ? structuredClone(toRaw(props.data)) : {};

    if (rawData.profile) {
      // Resolve photo URL
      if (rawData.profile.photo) {
        const url = rawData.profile.photo;
        if (!url.startsWith('http') && !url.startsWith('blob:') && !url.startsWith('data:')) {
          const base = (import.meta as any).env.VITE_BACKEND_URL || '';
          rawData.profile.photo = base + (url.startsWith('/') ? url : '/' + url);
        }
      }
      // Schema aliases
      if (rawData.profile.birthday && !rawData.profile.dob) rawData.profile.dob = rawData.profile.birthday;
      if (rawData.profile.dob && !rawData.profile.birthday) rawData.profile.birthday = rawData.profile.dob;
      // Extras merge
      if (typeof rawData.profile.extras === 'object' && rawData.profile.extras !== null) {
        Object.assign(rawData.profile, rawData.profile.extras);
      }
    }

    ['experience','education','skills','projects','languages','certifications','awards'].forEach(sec => {
      if (Array.isArray(rawData[sec])) {
        rawData[sec].forEach((item: any) => {
          if (item && typeof item.extras === 'object' && item.extras !== null) {
            Object.assign(item, item.extras);
          }
        });
      }
    });

    const sanitized = DOMPurify.sanitize(template(rawData));
    contentEl.innerHTML = sanitized;

    // A4 constraint CSS inside shadow DOM
    styleEl.textContent = `
      .cv-page {
        width: 210mm;
        min-height: 297mm;
        padding: 20mm;
        box-sizing: border-box;
        background: #fff;
      }
      section { margin-bottom: 18px; }
      .item   { page-break-inside: avoid; }
      body    { margin: 0; padding: 0; }
    ` + '\n' + props.css;

  } catch (e: any) {
    if (contentEl) {
      contentEl.innerHTML = `<div style="color:red;padding:20px;">Template Error: ${e.message}</div>`;
    }
  }
};

// ─── Mount ─────────────────────────────────────────────────────────────────
onMounted(() => {
  // Shadow DOM on the A4 host div
  if (a4HostRef.value) {
    shadow  = a4HostRef.value.attachShadow({ mode: 'open' });
    styleEl   = document.createElement('style');
    contentEl = document.createElement('div');
    contentEl.className = 'cv-page';
    shadow.appendChild(styleEl);
    shadow.appendChild(contentEl);
    render();
  }

  // Observe the OUTER PANEL (panelRef) for width changes → recalc scale
  if (panelRef.value) {
    ro = new ResizeObserver(() => recalcScale());
    ro.observe(panelRef.value);
    recalcScale(); // initial
  }
});

onUnmounted(() => {
  if (ro) {
    ro.disconnect();
    ro = null;
  }
});

// ─── Watchers ──────────────────────────────────────────────────────────────
watch(() => props.data, () => render(), { deep: true });
watch(() => [props.html, props.css], () => render());
</script>

<template>
  <!--
    OUTER PANEL: fills the Right flex cell.
    overflow-y-auto  → vertical scroll when A4 is taller than panel (scaled).
    overflow-x-hidden → never show horizontal scrollbar.
    We measure THIS element's width → compute scale.
  -->
  <div
    ref="panelRef"
    class="w-full h-full overflow-y-auto overflow-x-hidden bg-gray-200 flex flex-col items-center py-4 print:bg-transparent print:p-0"
  >
    <!--
      SCALE WRAPPER: CSS transform:scale only on preview.
      origin-top so scaling starts from the top of the page.
      The trick to fix scroll height after scale-down:
        scaledHeight = A4_element.offsetHeight * scale
        We set the wrapper height explicitly so the scroll container
        sees the correct document height.
    -->
    <div
      class="flex-shrink-0 origin-top print:!transform-none print:!origin-top"
      :style="{
        transform:    `scale(${scale})`,
        width:        '210mm',
        marginBottom: `${(scale - 1) * 1123}px`,
      }"
    >
      <!-- A4 Canvas — Shadow DOM host. Width/Height always 210mm × 297mm. -->
      <div
        ref="a4HostRef"
        class="bg-white shadow-xl print:shadow-none"
        style="width: 210mm; min-height: 297mm; display: block;"
      />
    </div>
  </div>
</template>

<style scoped>
/* No extra styles needed — layout is driven by Tailwind + inline :style */
@media print {
  .cv-render-wrapper {
    overflow: visible !important;
    height: auto !important;
  }
}
</style>
