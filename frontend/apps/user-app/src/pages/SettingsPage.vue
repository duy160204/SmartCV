<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import api from '@/api/axios';
import { ref, onMounted } from 'vue';

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

// Subscription state
const subscription = ref<any>(null);
const subscriptionLoading = ref(false);
const subscriptionError = ref<string | null>(null);

onMounted(async () => {
    await loadSubscription();
});

const loadSubscription = async () => {
    try {
        subscriptionLoading.value = true;
        subscriptionError.value = null;
        const res = await api.get('/subscription/me');
        // Backend returns { message: "...", data: {...} }
        subscription.value = res.data?.data || res.data;
    } catch (e: any) {
        console.error('Failed to load subscription', e);
        subscriptionError.value = e.response?.data?.message || 'Failed to load subscription';
        // Fallback to FREE plan for display
        subscription.value = { plan: 'FREE', status: 'ACTIVE' };
    } finally {
        subscriptionLoading.value = false;
    }
};

const updateProfile = async () => {
    try {
        await api.put('/users/profile', profileForm.value);
        await auth.checkAuth();
        alert("Profile updated!");
    } catch (e: any) {
        alert("Error: " + (e.response?.data?.message || e.message));
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
    } catch (e: any) {
        alert("Error: " + (e.response?.data?.message || e.message));
    }
};

const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString();
};
</script>

<template>
  <div class="min-h-screen bg-gray-50">
      <nav class="bg-white border-b px-8 py-4 flex justify-between items-center">
          <h1 class="font-bold text-xl"><router-link to="/dashboard">SmartCV</router-link></h1>
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
              <button 
                @click="activeTab = 'subscription'"
                :class="['px-4 py-2', activeTab === 'subscription' ? 'border-b-2 border-blue-600 font-bold' : '']"
              >Subscription</button>
          </div>

          <!-- Profile Tab -->
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

          <!-- Password Tab -->
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

          <!-- Subscription Tab -->
          <div v-if="activeTab === 'subscription'">
              <div v-if="subscriptionLoading" class="text-gray-500">Loading subscription info...</div>
              <div v-else-if="subscription" class="space-y-6">
                  <div class="bg-gradient-to-r from-blue-500 to-indigo-600 text-white p-6 rounded-lg">
                      <div class="text-sm opacity-80 mb-1">Current Plan</div>
                      <div class="text-3xl font-bold">{{ subscription.plan || 'FREE' }}</div>
                      <div class="text-sm mt-2 opacity-80" v-if="subscription.endDate">
                          Expires: {{ formatDate(subscription.endDate) }}
                      </div>
                      <div class="text-sm mt-1 opacity-80">
                          Status: {{ subscription.status || 'ACTIVE' }}
                      </div>
                  </div>

                  <div class="grid grid-cols-3 gap-4">
                      <div class="border rounded-lg p-4" :class="subscription.plan === 'FREE' ? 'border-blue-500 bg-blue-50' : ''">
                          <h4 class="font-bold text-lg">Free</h4>
                          <p class="text-gray-500 text-sm mb-4">Basic features</p>
                          <ul class="text-sm space-y-1 text-gray-600">
                              <li>✓ 3 CVs</li>
                              <li>✓ Basic templates</li>
                              <li>✗ PDF Download</li>
                          </ul>
                      </div>
                      <div class="border rounded-lg p-4" :class="subscription.plan === 'PRO' ? 'border-blue-500 bg-blue-50' : ''">
                          <h4 class="font-bold text-lg">Pro</h4>
                          <p class="text-gray-500 text-sm mb-4">$9.99/month</p>
                          <ul class="text-sm space-y-1 text-gray-600">
                              <li>✓ 10 CVs</li>
                              <li>✓ All templates</li>
                              <li>✓ PDF Download</li>
                          </ul>
                          <button v-if="subscription.plan !== 'PRO' && subscription.plan !== 'PREMIUM'" 
                                  class="mt-4 w-full bg-blue-600 text-white py-2 rounded text-sm">
                              Upgrade
                          </button>
                      </div>
                      <div class="border rounded-lg p-4" :class="subscription.plan === 'PREMIUM' ? 'border-blue-500 bg-blue-50' : ''">
                          <h4 class="font-bold text-lg">Premium</h4>
                          <p class="text-gray-500 text-sm mb-4">$19.99/month</p>
                          <ul class="text-sm space-y-1 text-gray-600">
                              <li>✓ Unlimited CVs</li>
                              <li>✓ All templates</li>
                              <li>✓ AI Assistant</li>
                          </ul>
                          <button v-if="subscription.plan !== 'PREMIUM'" 
                                  class="mt-4 w-full bg-indigo-600 text-white py-2 rounded text-sm">
                              Upgrade
                          </button>
                      </div>
                  </div>
              </div>
              <div v-else class="text-gray-500">Unable to load subscription info</div>
          </div>
      </div>
  </div>
</template>

