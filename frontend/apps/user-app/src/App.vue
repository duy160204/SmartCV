<script setup lang="ts">
import { useLanguageStore } from '@/stores/language.store';

// Initialize the store immediately so it runs before any route render
const langStore = useLanguageStore();
</script>

<template>
  <router-view></router-view>
</template>
