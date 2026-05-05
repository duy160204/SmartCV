<script setup lang="ts">
import Navbar from '@/components/layout/Navbar.vue';
import AiQuotaLimitModal from '@/components/common/AiQuotaLimitModal.vue';
</script>

<template>
    <div class="flex flex-col font-sans bg-gray-50">
        <main class="flex-1">
            <slot></slot>
        </main>

        <footer class="bg-white border-t py-12 text-center text-gray-500 text-sm">
            <p>© 2026 SmartCV. All rights reserved.</p>
        </footer>

        <AiQuotaLimitModal />
    </div>
</template>
