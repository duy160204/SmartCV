<script setup lang="ts">
import { ref } from 'vue';
import axios from 'axios';

const email = ref('');
const error = ref('');
const isLoading = ref(false);
const success = ref(false);

const authApi = axios.create({
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true
});

const handleSubmit = async () => {
    error.value = '';
    
    try {
        isLoading.value = true;
        await authApi.post('/auth/forgot-password', null, {
            params: { email: email.value }
        });
        success.value = true;
    } catch (e: any) {
        // Backend doesn't reveal if email exists for security
        // Always show success message
        success.value = true;
    } finally {
        isLoading.value = false;
    }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="p-8 bg-white rounded-xl shadow-lg w-96">
      <h1 class="text-2xl font-bold mb-2 text-gray-800">Forgot Password</h1>
      <p class="text-gray-500 mb-6 text-sm">Enter your email and we'll send you a new password</p>
      
      <!-- Success State -->
      <div v-if="success" class="text-center py-8">
          <div class="text-5xl mb-4">📧</div>
          <h2 class="text-xl font-bold text-green-600 mb-2">Check Your Email</h2>
          <p class="text-gray-600 mb-6">If an account exists with <strong>{{ email }}</strong>, we've sent a new password.</p>
          <router-link to="/login" class="text-blue-600 hover:underline font-medium">Back to Login</router-link>
      </div>
      
      <!-- Form -->
      <form v-else @submit.prevent="handleSubmit" class="space-y-4">
        <div>
            <label class="block mb-1 text-sm font-medium text-gray-700">Email Address</label>
            <input 
              v-model="email" 
              type="email" 
              class="w-full border p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500" 
              placeholder="you@example.com" 
              required 
            />
        </div>
        
        <p v-if="error" class="text-red-500 text-sm bg-red-50 p-3 rounded-lg">{{ error }}</p>
        
        <button 
          type="submit" 
          :disabled="isLoading"
          class="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 transition disabled:opacity-50"
        >
          {{ isLoading ? 'Sending...' : 'Send New Password' }}
        </button>
      </form>
      
      <p class="mt-6 text-center text-sm text-gray-500">
          Remember your password? 
          <router-link to="/login" class="text-blue-600 hover:underline font-medium">Sign in</router-link>
      </p>
    </div>
  </div>
</template>
