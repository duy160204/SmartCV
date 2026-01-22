<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import OAuthProviderButton from '@/components/auth/OAuthProviderButton.vue';

const auth = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');
const error = ref('');

const handleLogin = async () => {
    try {
        await auth.login({ email: email.value, password: password.value });
        router.push('/');
    } catch (e: any) {
        error.value = "Login failed";
    }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-100">
    <div class="p-8 bg-white rounded shadow-md w-96">
      <h1 class="text-2xl font-bold mb-4 text-center">Login</h1>
      
      <div class="flex flex-wrap gap-2 justify-center mb-6">
          <OAuthProviderButton provider="google" />
          <OAuthProviderButton provider="github" />
          <OAuthProviderButton provider="facebook" />
          <OAuthProviderButton provider="linkedin" />
          <OAuthProviderButton provider="zalo" />
      </div>

      <div class="relative mb-4">
          <div class="absolute inset-0 flex items-center"><span class="w-full border-t"></span></div>
          <div class="relative flex justify-center text-xs uppercase"><span class="bg-white px-2 text-gray-500">Or with email</span></div>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="mb-4">
            <label class="block mb-1">Email</label>
            <input v-model="email" type="email" class="w-full border p-2 rounded" required />
        </div>
        <div class="mb-4">
            <label class="block mb-1">Password</label>
            <input v-model="password" type="password" class="w-full border p-2 rounded" required />
        </div>
        <div class="mb-4 text-right">
            <router-link to="/forgot-password" class="text-sm text-blue-600 hover:underline">Forgot password?</router-link>
        </div>
        <p v-if="error" class="text-red-500 mb-2">{{ error }}</p>
        <button type="submit" class="w-full bg-blue-600 text-white py-2 rounded">Login</button>
      </form>
      
      <p class="mt-4 text-center text-sm text-gray-500">
          Don't have an account? 
          <router-link to="/register" class="text-blue-600 hover:underline font-medium">Sign up</router-link>
      </p>
    </div>
  </div>
</template>
