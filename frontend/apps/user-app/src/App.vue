<script setup lang="ts">
import { useLanguageStore } from '@/stores/language.store';
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import Navbar from '@/components/layout/Navbar.vue';

// Initialize the store immediately so it runs before any route render
const langStore = useLanguageStore();
const route = useRoute();

const hiddenNavbarPaths = [
  '/login',
  '/register',
  '/forgot-password',
  '/oauth/callback',
  '/auth/callback'
];

const showNavbar = computed(() => {
  if (!route.path) return false;
  // Check exact matches or starts with for editor
  if (hiddenNavbarPaths.some(p => route.path.startsWith(p))) return false;
  if (route.path.startsWith('/cv/editor') || route.path.startsWith('/cv/edit') || route.path.startsWith('/editor')) return false;
  return true;
});
</script>

<template>
  <div class="min-h-screen flex flex-col font-sans bg-gray-50">
    <Navbar v-if="showNavbar" />
    <router-view class="flex-1"></router-view>
  </div>
</template>
