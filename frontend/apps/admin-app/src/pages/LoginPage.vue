<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

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
        error.value = e.message || "Login failed";
    }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gray-900">
    <div class="p-8 bg-white rounded shadow-md w-96">
      <h1 class="text-2xl font-bold mb-4 text-red-600">Admin Portal</h1>
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
            <label class="block mb-1">Email</label>
            <input v-model="email" type="email" class="w-full border p-2 rounded" required />
        </div>
        <div class="mb-4">
            <label class="block mb-1">Password</label>
            <input v-model="password" type="password" class="w-full border p-2 rounded" required />
        </div>
        <p v-if="error" class="text-red-500 mb-2">{{ error }}</p>
        <button type="submit" class="w-full bg-red-600 text-white py-2 rounded">Login</button>
      </form>
    </div>
  </div>
</template>
