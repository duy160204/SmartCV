<script setup lang="ts">
import { useCVStore } from '@/stores/cv';
import { ref, computed } from 'vue';
import { cvApi } from '@/api/user.api';

const store = useCVStore();

const interestsText = computed({
    get: () => {
        const interests = store.currentCV?.content?.interests;
        return Array.isArray(interests) ? interests.join(', ') : '';
    },
    set: (val: string) => {
        if (store.currentCV?.content) {
            store.currentCV.content.interests = val.split(',').map(i => i.trim()).filter(Boolean);
        }
    }
});

const props = defineProps<{
    activeTab: string;
}>();
const addItem = (section: string) => {
    if (!store.currentCV?.content) return;
    if (!store.currentCV.content[section]) store.currentCV.content[section] = [];
    store.currentCV.content[section].push({});
};

const removeItem = (section: string, index: number | string) => {
    if (store.currentCV?.content?.[section]) store.currentCV.content[section].splice(Number(index), 1);
};

const addExtra = (section: string) => {
    if (!store.currentCV?.content) return;
    if (section === 'profile') {
        if (!store.currentCV.content.profile.extras) store.currentCV.content.profile.extras = {};
        const timestamp = Date.now();
        const newKey = `custom_field_${timestamp}`;
        store.currentCV.content.profile.extras[newKey] = '';
    }
};

const updateExtraKey = (section: string, oldKey: string, newKey: string, event: Event) => {
    if (oldKey === newKey || !newKey || !store.currentCV?.content) return;
    if (section === 'profile') {
        const extras = store.currentCV.content.profile.extras;
        extras[newKey] = extras[oldKey];
        delete extras[oldKey];
    }
};

const updateExtraValue = (section: string, key: string, value: string) => {
    if (!store.currentCV?.content) return;
    if (section === 'profile') {
        store.currentCV.content.profile.extras[key] = value;
    }
};

const removeExtra = (section: string, key: string) => {
    if (!store.currentCV?.content) return;
    if (section === 'profile') {
        delete store.currentCV.content.profile.extras[key];
    }
};

const getImageUrl = (url: string | null) => {
    if (!url) return '';
    if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url;
    const backendBaseUrl = (import.meta as any).env.VITE_BACKEND_URL || '';
    return backendBaseUrl + (url.startsWith('/') ? url : '/' + url);
};

const isUploadingAvatar = ref(false);
const uploadAvatar = async (event: Event) => {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file || !store.currentCV) return;

    isUploadingAvatar.value = true;
    try {
        const res = await cvApi.uploadAvatar(store.currentCV.id, file);
        if (store.currentCV.content && store.currentCV.content.profile) {
            store.currentCV.content.profile.photo = res.data;
        }
    } catch (e: any) {
        alert("Upload failed: " + (e.response?.data?.message || e.message));
    } finally {
        isUploadingAvatar.value = false;
        (event.target as HTMLInputElement).value = '';
    }
};
</script>

<template>
  <div class="flex flex-col h-full relative">

    <!-- Content -->
    <div class="p-6 flex-1 overflow-y-auto" v-if="store.currentCV?.content && store.currentCV.content.profile">
        
        <!-- Profile Tab -->
        <div v-if="activeTab === 'profile'" class="space-y-4">
            <h3 class="font-bold text-lg">Personal Details</h3>
            
            <div>
                 <label class="block text-sm font-medium mb-1">Profile Photo</label>
                 <div class="flex items-center gap-4">
                     <img v-if="store.currentCV.content.profile.photo" :src="getImageUrl(store.currentCV.content.profile.photo)" class="w-16 h-16 rounded-full object-cover border" />
                     <div v-else class="w-16 h-16 rounded-full bg-gray-200 border flex items-center justify-center text-gray-400 text-xs">No Photo</div>
                     <div class="flex flex-col">
                         <input type="file" accept="image/png, image/jpeg, image/webp" @change="uploadAvatar" :disabled="isUploadingAvatar" class="text-sm" />
                         <span v-if="isUploadingAvatar" class="text-xs text-blue-500 mt-1">Uploading...</span>
                     </div>
                 </div>
            </div>

            <div>
                <label class="block text-sm font-medium mb-1">Full Name</label>
                <input v-model="store.currentCV.content.profile.name" class="w-full border p-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium mb-1">Title</label>
                <input v-model="store.currentCV.content.profile.title" class="w-full border p-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium mb-1">Email</label>
                <input v-model="store.currentCV.content.profile.email" class="w-full border p-2 rounded" />
            </div>
             <div>
                <label class="block text-sm font-medium mb-1">Phone</label>
                <input v-model="store.currentCV.content.profile.phone" class="w-full border p-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium mb-1">Website / Portfolio</label>
                <input v-model="store.currentCV.content.profile.website" class="w-full border p-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium mb-1">Location</label>
                <input v-model="store.currentCV.content.profile.location" class="w-full border p-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium mb-1">Gender</label>
                <input v-model="store.currentCV.content.profile.gender" class="w-full border p-2 rounded" />
            </div>
             <div>
                <label class="block text-sm font-medium mb-1">Date of Birth</label>
                <input v-model="store.currentCV.content.profile.birthday" class="w-full border p-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium mb-1">Address</label>
                <input v-model="store.currentCV.content.profile.address" class="w-full border p-2 rounded" />
            </div>
            <div>
                <label class="block text-sm font-medium mb-1">Summary / Career Objective</label>
                <div>
                    <textarea v-model="store.currentCV.content.profile.summary" rows="4" class="w-full border p-2 rounded"></textarea>
                </div>
            </div>

            <!-- Dynamic Extras -->
            <div class="mt-4 border-t pt-4">
                <div class="flex justify-between items-center mb-2">
                    <h4 class="font-bold text-sm">Additional Fields</h4>
                    <button @click="addExtra('profile')" class="text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded hover:bg-blue-200">+ Add Custom Field</button>
                </div>
                <div v-for="(val, key) in store.currentCV.content.profile.extras" :key="key" class="flex gap-2 mb-2 items-center">
                    <input :value="key" @change="updateExtraKey('profile', String(key), ($event.target as HTMLInputElement).value, $event)" placeholder="Field Name" class="w-1/3 border p-2 rounded text-sm" />
                    <input :value="val" @input="updateExtraValue('profile', String(key), ($event.target as HTMLInputElement).value)" placeholder="Value" class="flex-1 border p-2 rounded text-sm" />
                    <button @click="removeExtra('profile', String(key))" class="text-red-400 hover:text-red-600 bg-white border border-transparent hover:border-red-200 rounded p-1 shadow-sm">✕</button>
                </div>
            </div>
        </div>

        <!-- Experience Tab -->
        <div v-if="activeTab === 'experience'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Experience</h3>
                <button @click="addItem('experience')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-for="(exp, index) in store.currentCV.content.experience" :key="index" class="bg-white p-4 border rounded shadow-sm relative group">
                <button @click="removeItem('experience', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                
                <div class="grid grid-cols-2 gap-4 mb-2">
                    <input v-model="exp.company" placeholder="Company" class="border p-2 rounded" />
                    <input v-model="exp.position" placeholder="Position" class="border p-2 rounded" />
                </div>
                <div class="mb-2">
                    <input v-model="exp.date" placeholder="Date / Duration" class="border p-2 rounded w-full" />
                </div>
                <div>
                     <textarea v-model="exp.description" placeholder="Description" rows="3" class="w-full border p-2 rounded mb-1"></textarea>
                </div>
            </div>
        </div>

        <!-- Education Tab -->
        <div v-if="activeTab === 'education'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Education</h3>
                <button @click="addItem('education')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-for="(edu, index) in store.currentCV.content.education" :key="index" class="bg-white p-4 border rounded shadow-sm relative group">
                <button @click="removeItem('education', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                
                 <div class="grid grid-cols-2 gap-4 mb-2">
                    <input v-model="edu.school" placeholder="School" class="border p-2 rounded" />
                    <input v-model="edu.degree" placeholder="Degree" class="border p-2 rounded" />
                </div>
                 <div class="mb-2">
                    <input v-model="edu.date" placeholder="Date" class="border p-2 rounded w-full" />
                </div>
                <input v-model="edu.major" placeholder="Major" class="border p-2 rounded w-full" />
            </div>
        </div>

        <!-- Skills Tab (New Implementation if missed, or update existing) -->
         <div v-if="activeTab === 'skills'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Skills</h3>
                <button @click="addItem('skills')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-for="(skill, index) in store.currentCV.content.skills" :key="index" class="bg-white p-4 border rounded shadow-sm relative group flex gap-2 items-center">
                 <button @click="removeItem('skills', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                 <div class="flex-1">
                    <input v-model="skill.name" placeholder="Skill Name" class="border p-2 rounded w-full" />
                 </div>
                 <div class="w-1/3">
                    <input v-model="skill.level" placeholder="Level (e.g. Expert)" class="border p-2 rounded w-full" />
                 </div>
            </div>
        </div>

        <!-- Projects Tab -->
        <div v-if="activeTab === 'projects'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Projects</h3>
                <button @click="addItem('projects')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-if="store.currentCV.content.projects" class="space-y-4">
                <div v-for="(proj, index) in store.currentCV.content.projects" :key="index" class="bg-white p-4 border rounded shadow-sm relative group">
                    <button @click="removeItem('projects', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                    
                    <div class="grid grid-cols-2 gap-4 mb-2">
                        <input v-model="proj.name" placeholder="Project Name" class="border p-2 rounded" />
                        <input v-model="proj.role" placeholder="Your Role" class="border p-2 rounded" />
                    </div>
                    <div class="mb-2">
                        <input v-model="proj.date" placeholder="Date / Duration" class="border p-2 rounded w-full" />
                    </div>
                    <div>
                        <textarea v-model="proj.description" placeholder="Description" rows="3" class="w-full border p-2 rounded"></textarea>
                    </div>
                     <div>
                        <input v-model="proj.link" placeholder="Link / URL" class="border p-2 rounded w-full mt-2" />
                    </div>
                </div>
            </div>
             <div v-else class="text-gray-500 italic">This section is not initialized in the data. Make sure to create a new CV to see this.</div>
        </div>

        <!-- Languages Tab -->
        <div v-if="activeTab === 'languages'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Languages</h3>
                <button @click="addItem('languages')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-if="store.currentCV.content.languages">
                <div v-for="(lang, index) in store.currentCV.content.languages" :key="index" class="bg-white p-4 border rounded shadow-sm relative group flex gap-4">
                    <button @click="removeItem('languages', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                    <input v-model="lang.language" placeholder="Language" class="border p-2 rounded flex-1" />
                    <input v-model="lang.proficiency" placeholder="Proficiency" class="border p-2 rounded flex-1" />
                </div>
            </div>
        </div>

         <!-- Certifications Tab -->
        <div v-if="activeTab === 'certifications'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Certifications</h3>
                <button @click="addItem('certifications')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-if="store.currentCV.content.certifications">
                <div v-for="(cert, index) in store.currentCV.content.certifications" :key="index" class="bg-white p-4 border rounded shadow-sm relative group space-y-2">
                    <button @click="removeItem('certifications', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                    <input v-model="cert.name" placeholder="Certification Name" class="border p-2 rounded w-full" />
                    <div class="grid grid-cols-2 gap-4">
                         <input v-model="cert.issuer" placeholder="Issuer" class="border p-2 rounded" />
                         <input v-model="cert.date" placeholder="Date" class="border p-2 rounded" />
                    </div>
                </div>
            </div>
        </div>

        <!-- Awards Tab -->
        <div v-if="activeTab === 'awards'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Awards</h3>
                <button @click="addItem('awards')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-if="store.currentCV.content.awards">
                <div v-for="(award, index) in store.currentCV.content.awards" :key="index" class="bg-white p-4 border rounded shadow-sm relative group space-y-2">
                    <button @click="removeItem('awards', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                    <input v-model="award.name" placeholder="Award Name" class="border p-2 rounded w-full" />
                    <div class="grid grid-cols-2 gap-4">
                         <input v-model="award.issuer" placeholder="Issuer" class="border p-2 rounded" />
                         <input v-model="award.year" placeholder="Year" class="border p-2 rounded" />
                    </div>
                </div>
            </div>
        </div>
        <!-- Interests Tab -->
        <div v-if="activeTab === 'interests'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">Interests</h3>
            </div>
            <div>
                 <textarea v-model="interestsText" placeholder="Tell us about your interests (comma separated)..." rows="4" class="w-full border p-2 rounded mb-1"></textarea>
            </div>
        </div>

        <!-- References Tab -->
        <div v-if="activeTab === 'references'" class="space-y-6">
             <div class="flex justify-between items-center">
                <h3 class="font-bold text-lg">References</h3>
                <button @click="addItem('references')" class="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded">+ Add</button>
            </div>
            
            <div v-if="store.currentCV.content.references">
                <div v-for="(refItem, index) in store.currentCV.content.references" :key="index" class="bg-white p-4 border rounded shadow-sm relative group space-y-2">
                    <button @click="removeItem('references', index)" class="absolute top-2 right-2 text-red-400 hover:text-red-600 opacity-0 group-hover:opacity-100">✕</button>
                    <input v-model="refItem.name" placeholder="Reference Name" class="border p-2 rounded w-full" />
                    <div class="grid grid-cols-2 gap-4">
                         <input v-model="refItem.position" placeholder="Position" class="border p-2 rounded" />
                         <input v-model="refItem.company" placeholder="Company" class="border p-2 rounded" />
                    </div>
                    <input v-model="refItem.contact" placeholder="Contact Info" class="border p-2 rounded w-full" />
                </div>
            </div>
        </div>
        
    </div>
  </div>
</template>
