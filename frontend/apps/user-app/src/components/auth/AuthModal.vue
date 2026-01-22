<script setup lang="ts">
import { ref, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import OAuthProviderButton from '@/components/auth/OAuthProviderButton.vue';

const props = defineProps<{
    isOpen: boolean;
    initialMode: 'login' | 'register';
}>();

const emit = defineEmits(['close']);
// ... existing script content ...
const auth = useAuthStore();
const router = useRouter();

const mode = ref<'login' | 'register'>(props.initialMode);
const email = ref('');
const password = ref('');
const name = ref(''); // For register
const isLoading = ref(false);
const error = ref('');

// Sync mode with prop
watch(() => props.initialMode, (newVal) => {
    mode.value = newVal;
    error.value = '';
});

// Reset form when opening
watch(() => props.isOpen, (isOpen) => {
    if (isOpen) {
        email.value = '';
        password.value = '';
        name.value = '';
        error.value = '';
    }
});

const switchMode = () => {
    mode.value = mode.value === 'login' ? 'register' : 'login';
    error.value = '';
};

const handleSubmit = async () => {
    isLoading.value = true;
    error.value = '';
    try {
        if (mode.value === 'login') {
            await auth.login({ email: email.value, password: password.value });
        } else {
            // Register flow
            await auth.register({ name: name.value, email: email.value, password: password.value });
        }
        // Success
        emit('close');
        // Optional: Trigger global event or notification
    } catch (e: any) {
        error.value = e.message || "Authentication failed";
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" @click="$emit('close')"></div>
        
        <!-- Modal -->
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-md relative z-10 overflow-hidden animate-in zoom-in-95 duration-200">
             <!-- Header -->
             <div class="px-8 pt-8 pb-6 text-center">
                 <h2 class="text-2xl font-bold text-gray-800">{{ mode === 'login' ? 'Welcome Back' : 'Create Account' }}</h2>
                 <p class="text-gray-500 mt-2 text-sm">
                     {{ mode === 'login' ? 'Login to access your CVs' : 'Join thousands of professionals today' }}
                 </p>
             </div>

             <!-- Form -->
             <form @submit.prevent="handleSubmit" class="px-8 pb-8 space-y-4">
                 <div v-if="mode === 'register'">
                     <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Full Name</label>
                     <input v-model="name" type="text" required class="w-full border border-gray-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition" placeholder="John Doe">
                 </div>
                 
                 <div>
                     <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Email Address</label>
                     <input v-model="email" type="email" required class="w-full border border-gray-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition" placeholder="you@example.com">
                 </div>

                 <div>
                     <label class="block text-xs font-bold text-gray-700 uppercase mb-1">Password</label>
                     <input v-model="password" type="password" required class="w-full border border-gray-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition" placeholder="••••••••">
                 </div>

                 <div v-if="error" class="p-3 bg-red-50 text-red-600 text-sm rounded-lg border border-red-100 flex items-center gap-2">
                     ⚠️ {{ error }}
                 </div>

                 <button 
                    type="submit" 
                    :disabled="isLoading"
                    class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3.5 rounded-xl shadow-lg shadow-blue-500/30 transition transform active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed"
                 >
                     <span v-if="isLoading">Processing...</span>
                     <span v-else>{{ mode === 'login' ? 'Login' : 'Create Account' }}</span>
                 </button>
             </form>
             
             <!-- Footer / OAuth -->
             <div class="bg-gray-50 px-8 py-6 border-t border-gray-100 text-center">
                 <p class="text-sm text-gray-600 mb-4">Or continue with</p>
                 <div class="flex flex-wrap gap-3 justify-center mb-6">
                     <OAuthProviderButton provider="google" />
                     <OAuthProviderButton provider="github" />
                     <OAuthProviderButton provider="facebook" />
                     <OAuthProviderButton provider="linkedin" />
                     <OAuthProviderButton provider="zalo" />
                 </div>
                 
                 <div class="text-sm">
                     <span class="text-gray-500">{{ mode === 'login' ? "Don't have an account?" : "Already have an account?" }}</span>
                     <button @click="switchMode" class="ml-2 font-bold text-blue-600 hover:underline">
                         {{ mode === 'login' ? 'Sign up' : 'Login' }}
                     </button>
                 </div>
             </div>
             
             <!-- Close Button -->
             <button @click="$emit('close')" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">✕</button>
        </div>
    </div>
</template>
