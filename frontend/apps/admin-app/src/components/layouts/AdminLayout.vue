<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useRoute } from 'vue-router';
import { computed } from 'vue';

const auth = useAuthStore();
const route = useRoute();

// Menu items for sidebar
const menuItems = [
    { path: '/', label: 'Dashboard', icon: '📊' },
    { path: '/users', label: 'Users', icon: '👥' },
    { path: '/cv', label: 'CV Management', icon: '📄' },
    { path: '/templates', label: 'Templates', icon: '🎨' },
    { path: '/payments', label: 'Payments', icon: '💳' },
    { path: '/subscriptions', label: 'Subscriptions', icon: '⭐' },
    { path: '/plans', label: 'Plans', icon: '📝' },
];

const isActive = (path: string) => {
    if (path === '/') return route.path === '/';
    return route.path.startsWith(path);
};
</script>

<template>
  <div class="flex min-h-screen bg-gray-100">
      <!-- Sidebar - ALWAYS VISIBLE -->
      <aside class="w-64 bg-gray-900 text-white flex flex-col fixed h-full">
          <div class="p-6 font-bold text-2xl border-b border-gray-700">
              🛠️ Admin Panel
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
                  🚪 Logout
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
