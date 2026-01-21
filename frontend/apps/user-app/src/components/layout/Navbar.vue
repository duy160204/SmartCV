<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const auth = useAuthStore();
const route = useRoute();
const mobileMenuOpen = ref(false);

const emit = defineEmits(['open-auth']);

const userInitials = computed(() => {
    return auth.user?.name?.charAt(0) || 'U';
});

const toggleMobileMenu = () => {
    mobileMenuOpen.value = !mobileMenuOpen.value;
};

const handleLoginClick = () => {
    emit('open-auth', 'login');
};

const handleRegisterClick = () => {
    emit('open-auth', 'register');
};

const logout = () => {
    auth.logout();
    router.push('/');
};

const goProfile = () => {
    router.push('/profile');
};
</script>

<template>
    <nav class="bg-white border-b border-gray-100 px-6 py-4 sticky top-0 z-50 backdrop-blur-md bg-white/90">
        <div class="max-w-7xl mx-auto flex justify-between items-center">
            <!-- Logo -->
            <div class="flex items-center gap-2 cursor-pointer" @click="router.push('/')">
                 <h1 class="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">SmartCV</h1>
            </div>

            <!-- Desktop Menu -->
            <div class="hidden md:flex items-center gap-8">
                <router-link to="/" class="text-gray-600 hover:text-blue-600 font-medium transition">Templates</router-link>
                <router-link to="/ai" class="text-gray-600 hover:text-blue-600 font-medium transition">AI Features</router-link>
                <router-link to="/pricing" class="text-gray-600 hover:text-blue-600 font-medium transition">Pricing</router-link>
            </div>

            <!-- Auth / User Actions -->
            <div class="hidden md:flex items-center gap-4">
                <template v-if="auth.isAuthenticated">
                     <router-link to="/ai" class="text-gray-600 hover:text-black font-medium transition">AI Features</router-link>
                     <div class="flex items-center gap-2 cursor-pointer hover:bg-gray-100 p-2 rounded transition relative group" @click="goProfile">
                        <img v-if="auth.user?.avatarURL" :src="auth.user.avatarURL" class="w-10 h-10 rounded-full border">
                        <div v-else class="w-10 h-10 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
                            {{ userInitials }}
                        </div>
                        
                        <!-- Dropdown -->
                        <div class="absolute top-full right-0 mt-2 w-48 bg-white border shadow-xl rounded-xl overflow-hidden hidden group-hover:block transition-all">
                             <div class="p-4 border-b">
                                 <p class="font-bold text-gray-800">{{ auth.user?.name }}</p>
                                 <p class="text-xs text-gray-500 truncate">{{ auth.user?.email }}</p>
                             </div>
                             <button @click="goProfile" class="w-full text-left px-4 py-3 hover:bg-gray-50 text-sm">Settings</button>
                             <button @click="logout" class="w-full text-left px-4 py-3 hover:bg-red-50 text-red-600 text-sm">Logout</button>
                        </div>
                     </div>
                </template>
                <template v-else>
                    <button @click="handleLoginClick" class="text-gray-600 font-medium hover:text-blue-600 transition">Login</button>
                    <button @click="handleRegisterClick" class="bg-blue-600 text-white px-6 py-2.5 rounded-full font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-500/30">Get Started</button>
                </template>
            </div>

            <!-- Mobile Toggle -->
            <button @click="toggleMobileMenu" class="md:hidden text-gray-700 text-2xl">
                ☰
            </button>
        </div>

        <!-- Mobile Drawer -->
        <div v-if="mobileMenuOpen" class="md:hidden absolute top-full left-0 right-0 bg-white border-b shadow-xl p-4 flex flex-col gap-4 animate-in slide-in-from-top-2">
            <router-link to="/" class="p-2 hover:bg-gray-50 rounded">Templates</router-link>
            <a href="#" class="p-2 hover:bg-gray-50 rounded">AI Features</a>
            <a href="#" class="p-2 hover:bg-gray-50 rounded">Pricing</a>
            <div class="h-px bg-gray-100 my-2"></div>
            
            <template v-if="auth.isAuthenticated">
                 <div class="flex items-center gap-3 p-2 bg-gray-50 rounded">
                        <div class="w-8 h-8 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
                            {{ userInitials }}
                        </div>
                        <span class="font-bold">{{ auth.user?.name }}</span>
                 </div>
                 <div class="px-2 pt-2 pb-3 space-y-1">
                    <router-link to="/ai" class="block px-4 py-2 text-gray-700 hover:bg-gray-100">AI Features</router-link>
                    <router-link to="/pricing" class="block px-4 py-2 text-gray-700 hover:bg-gray-100">Pricing</router-link>
                    <a @click="goProfile" class="block px-4 py-2 text-gray-700 hover:bg-gray-100 cursor-pointer">Profile</a>
                 </div>
                 <button @click="logout" class="text-left p-2 text-red-600 hover:bg-red-50 rounded">Logout</button>
            </template>
             <template v-else>
                    <button @click="handleLoginClick" class="w-full py-3 border rounded-xl font-bold">Login</button>
                    <button @click="handleRegisterClick" class="w-full py-3 bg-blue-600 text-white rounded-xl font-bold shadow-lg">Get Started</button>
            </template>
        </div>
    </nav>
</template>
