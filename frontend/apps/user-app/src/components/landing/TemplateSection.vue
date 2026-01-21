<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import GuestTemplatePreview from './GuestTemplatePreview.vue';
import UserTemplateGallery from './UserTemplateGallery.vue';

const auth = useAuthStore();
const emit = defineEmits(['trigger-auth']);

const handleTriggerAuth = () => {
    emit('trigger-auth');
};
</script>

<template>
    <div id="templates">
        <!-- User View -->
        <UserTemplateGallery v-if="auth.isAuthenticated" />
        
        <!-- Guest View -->
        <GuestTemplatePreview v-else @trigger-auth="handleTriggerAuth" />
    </div>
</template>
