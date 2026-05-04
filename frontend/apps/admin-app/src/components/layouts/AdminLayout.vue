<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useRoute } from 'vue-router';
import { computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue';

const { t, locale } = useI18n();
const auth = useAuthStore();
const route = useRoute();

// Update document title when locale changes
watch(locale, () => {
  document.title = t('admin.title');
}, { immediate: true });

// Menu items for sidebar
const menuItems = computed(() => [
    { path: '/', label: t('nav.dashboard'), icon: '📊' },
    { path: '/users', label: t('nav.users'), icon: '👥' },
    { path: '/cv', label: t('nav.brand'), icon: '📄' },
    { path: '/templates', label: t('nav.templates'), icon: '🎨' },
    { path: '/payments', label: t('payments.title'), icon: '💳' },
    { path: '/subscriptions', label: t('subscriptions.title'), icon: '⭐' },
    { path: '/plans', label: t('nav.pricing'), icon: '📝' },
]);

const isActive = (path: string) => {
    if (path === '/') return route.path === '/';
    return route.path.startsWith(path);
};
</script>

<template>
  <div class="flex min-h-screen bg-gray-100">
      <!-- Sidebar - ALWAYS VISIBLE -->
      <aside class="w-64 bg-gray-900 text-white flex flex-col fixed h-full">
          <div class="p-6 border-b border-gray-700 flex items-center justify-between">
              <span class="font-bold text-lg flex items-center gap-2">
                  <span>🛠️</span>
                  <span class="truncate">{{ t('admin.title') }}</span>
              </span>
              <LanguageSwitcher />
          </div>
          <nav class="flex-1 p-4 space-y-2 overflow-y-auto">
              <router-link 
                  v-for="item in menuItems" 
                  :key="item.path"
                  :to="item.path" 
                  :class="[
                      'block px-4 py-3 rounded-lg transition-all duration-200',
                      isActive(item.path) 
                          ? 'bg-blue-600 text-white font-medium' 
                          : 'hover:bg-gray-800 text-gray-300'
                  ]"
              >
                  <span class="mr-3">{{ item.icon }}</span>
                  {{ item.label }}
              </router-link>
          </nav>
          <div class="p-4 border-t border-gray-700">
              <div class="text-sm text-gray-400 mb-2">
                  {{ auth.user?.email }}
              </div>
              <button 
                  type="button"
                  @click="auth.logout()" 
                  class="w-full text-left px-4 py-2 text-red-400 hover:text-red-300 hover:bg-gray-800 rounded transition"
              >
                  🚪 {{ t('common.logout') }}
              </button>
          </div>
      </aside>

      <!-- Main Content - Scrollable, offset by sidebar width -->
      <main class="flex-1 ml-64 min-h-screen">
          <!-- Slot for page content -->
          <router-view></router-view>
      </main>
  </div>
</template>
