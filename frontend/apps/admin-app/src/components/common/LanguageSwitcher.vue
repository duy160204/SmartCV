<script setup lang="ts">
import { useLanguageStore } from '@/stores/language.store';

const langStore = useLanguageStore();

const switchLanguage = () => {
    const newLocale = langStore.locale === 'vi' ? 'en' : 'vi';
    langStore.setLocale(newLocale);
};
</script>

<template>
  <button
    @click="switchLanguage"
    class="flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-medium
           border border-gray-200 hover:bg-gray-100 transition text-gray-600"
    :title="langStore.locale === 'vi' ? 'Switch to English' : 'Chuyển sang Tiếng Việt'"
  >
    <span v-if="langStore.locale === 'vi'">🇻🇳 VI</span>
    <span v-else>🇺🇸 EN</span>
  </button>
</template>
