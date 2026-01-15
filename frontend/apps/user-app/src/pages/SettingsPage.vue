<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import api from '@/api/axios';
import { ref } from 'vue';

const auth = useAuthStore();
const activeTab = ref('profile');

const profileForm = ref({
    name: auth.user?.name || '',
    avatarURL: auth.user?.avatarURL || ''
});

const passwordForm = ref({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
});

const updateProfile = async () => {
    try {
        await api.put('/users/profile', profileForm.value);
        await auth.checkAuth(); // Refresh
        alert("Profile updated!");
    } catch (e) {
        alert("Error: " + e.message);
    }
};

const updatePassword = async () => {
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        alert("Passwords do not match");
        return;
    }
    try {
        await api.put('/users/password', {
            currentPassword: passwordForm.value.currentPassword,
            newPassword: passwordForm.value.newPassword
        });
        alert("Password changed!");
        passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
    } catch (e) {
        alert("Error: " + e.message);
    }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50">
      <nav class="bg-white border-b px-8 py-4 flex justify-between items-center">
          <h1 class="font-bold text-xl"><router-link to="/">SmartCV</router-link></h1>
          <div>{{ auth.user?.name }}</div>
      </nav>

      <div class="max-w-4xl mx-auto mt-8 p-6 bg-white rounded shadow">
          <h2 class="text-2xl font-bold mb-6">Account Settings</h2>
          
          <div class="flex border-b mb-6">
              <button 
                @click="activeTab = 'profile'"
                :class="['px-4 py-2', activeTab === 'profile' ? 'border-b-2 border-blue-600 font-bold' : '']"
              >Profile</button>
              <button 
                @click="activeTab = 'password'"
                :class="['px-4 py-2', activeTab === 'password' ? 'border-b-2 border-blue-600 font-bold' : '']"
              >Security</button>
          </div>

          <div v-if="activeTab === 'profile'" class="space-y-4 max-w-md">
              <div>
                  <label class="block mb-1 font-medium">Full Name</label>
                  <input v-model="profileForm.name" class="w-full border p-2 rounded" />
              </div>
              <div>
                   <label class="block mb-1 font-medium">Avatar URL</label>
                  <input v-model="profileForm.avatarURL" class="w-full border p-2 rounded" />
              </div>
              <button @click="updateProfile" class="bg-blue-600 text-white px-4 py-2 rounded">Save Changes</button>
          </div>

          <div v-if="activeTab === 'password'" class="space-y-4 max-w-md">
              <div>
                  <label class="block mb-1 font-medium">Current Password</label>
                  <input v-model="passwordForm.currentPassword" type="password" class="w-full border p-2 rounded" />
              </div>
              <div>
                  <label class="block mb-1 font-medium">New Password</label>
                  <input v-model="passwordForm.newPassword" type="password" class="w-full border p-2 rounded" />
              </div>
              <div>
                  <label class="block mb-1 font-medium">Confirm Password</label>
                  <input v-model="passwordForm.confirmPassword" type="password" class="w-full border p-2 rounded" />
              </div>
              <button @click="updatePassword" class="bg-red-600 text-white px-4 py-2 rounded">Change Password</button>
          </div>
      </div>
  </div>
</template>
