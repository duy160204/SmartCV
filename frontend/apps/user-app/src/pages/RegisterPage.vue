<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();

const form = ref({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
});
const error = ref('');
const isLoading = ref(false);
const success = ref(false);

const authApi = axios.create({
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true
});

const handleRegister = async () => {
    error.value = '';
    
    if (form.value.password !== form.value.confirmPassword) {
        error.value = 'Passwords do not match';
        return;
    }
    
    if (form.value.password.length < 6) {
        error.value = 'Password must be at least 6 characters';
        return;
    }
    
    try {
        isLoading.value = true;
        await authApi.post('/auth/register', {
            name: form.value.name,
            email: form.value.email,
            password: form.value.password
        });
        success.value = true;
    } catch (e: any) {
        error.value = e.response?.data?.message || 'Registration failed';
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="p-8 bg-white rounded-xl shadow-lg w-96">
      <h1 class="text-2xl font-bold mb-2 text-gray-800">Create Account</h1>
      <p class="text-gray-500 mb-6 text-sm">Join SmartCV to build your professional resume</p>
      
      <!-- Success State -->
      <div v-if="success" class="text-center py-8">
          <div class="text-5xl mb-4">✉️</div>
          <h2 class="text-xl font-bold text-green-600 mb-2">Check Your Email!</h2>
          <p class="text-gray-600 mb-6">We've sent a verification link to <strong>{{ form.email }}</strong></p>
          <router-link to="/login" class="text-blue-600 hover:underline">Back to Login</router-link>
      </div>
      
      <!-- Registration Form -->
      <form v-else @submit.prevent="handleRegister" class="space-y-4">
        <div>
            <label class="block mb-1 text-sm font-medium text-gray-700">Full Name</label>
            <input v-model="form.name" type="text" class="w-full border p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" placeholder="John Doe" required />
        </div>
        <div>
            <label class="block mb-1 text-sm font-medium text-gray-700">Email</label>
            <input v-model="form.email" type="email" class="w-full border p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" placeholder="you@example.com" required />
        </div>
        <div>
            <label class="block mb-1 text-sm font-medium text-gray-700">Password</label>
            <input v-model="form.password" type="password" class="w-full border p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" placeholder="••••••••" required />
        </div>
        <div>
            <label class="block mb-1 text-sm font-medium text-gray-700">Confirm Password</label>
            <input v-model="form.confirmPassword" type="password" class="w-full border p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" placeholder="••••••••" required />
        </div>
        
        <p v-if="error" class="text-red-500 text-sm bg-red-50 p-3 rounded-lg">{{ error }}</p>
        
        <button 
          type="submit" 
          :disabled="isLoading"
          class="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 transition disabled:opacity-50"
        >
          {{ isLoading ? 'Creating Account...' : 'Create Account' }}
        </button>
      </form>
      
      <p class="mt-6 text-center text-sm text-gray-500">
          Already have an account? 
          <router-link to="/login" class="text-blue-600 hover:underline font-medium">Sign in</router-link>
      </p>
    </div>
  </div>
</template>
