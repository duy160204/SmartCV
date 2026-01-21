<script setup lang="ts">
import { ref } from 'vue';
import Navbar from '@/components/layout/Navbar.vue';
import AuthModal from '@/components/auth/AuthModal.vue';

const showAuthModal = ref(false);
const authMode = ref<'login' | 'register'>('login');

const openAuth = (mode: 'login' | 'register') => {
    authMode.value = mode;
    showAuthModal.value = true;
};

defineExpose({ openAuth });
</script>

<template>
    <div class="min-h-screen flex flex-col font-sans bg-gray-50">
        <Navbar @open-auth="openAuth" />
        
        <main class="flex-1">
            <slot></slot>
        </main>

        <footer class="bg-white border-t py-12 text-center text-gray-500 text-sm">
            <p>© 2026 SmartCV. All rights reserved.</p>
        </footer>

        <AuthModal 
            :isOpen="showAuthModal" 
            :initialMode="authMode" 
            @close="showAuthModal = false" 
        />
    </div>
</template>
