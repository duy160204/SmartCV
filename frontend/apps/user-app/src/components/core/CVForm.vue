<script setup lang="ts">
import { useCVStore } from '@/stores/cv';
import { ref } from 'vue';
import AIImproveModal from './AIImproveModal.vue';

const store = useCVStore();
const activeTab = ref('profile');

// AI State
const showAI = ref(false);
const aiTargetField = ref('');
const aiTargetValue = ref('');
const aiLoading = ref(false);

const openAI = (fieldPath: string, val: string) => {
    aiTargetField.value = fieldPath;
    aiTargetValue.value = val;
    showAI.value = true;
};

const handleAIApply = async (instruction: string) => {
    showAI.value = false;
    aiLoading.value = true;
    try {
        const improved = await store.improveText(aiTargetField.value, aiTargetValue.value, instruction);
        if (improved) {
            // Traverse path to update value
            // Simple approach for specific known fields, generic is harder without lodash set
            // For now, implementing map for Profile Summary as primary use case
            if (aiTargetField.value === 'profile.summary') {
                if (store.currentCV?.content?.profile) {
                    store.currentCV.content.profile.summary = improved;
                }
            } else if (aiTargetField.value.startsWith('experience')) {
                // e.g. experience[0].description
                // Parse index
                const parts = aiTargetField.value.split('.'); 
                // experience[0] -> experience, 0
                // Hacky parse for now, assuming explicit calls
            }
             // For strict implementation, we'd use a deep set util, but here we can just update the model driven by UI
             // But wait, the previous code doesn't do deep set helper. 
             // We'll update manually based on bindings.
        }
    } catch (e: any) {
        alert("AI Failed: " +  (e.message || e));
    } finally {
        aiLoading.value = false;
    }
};

// Add item
const addItem = (section: string) => {
    if (!store.currentCV?.content) return;
    if (!store.currentCV.content[section]) store.currentCV.content[section] = [];
    store.currentCV.content[section].push({});
};

const removeItem = (section: string, index: number | string) => {
    if (store.currentCV?.content?.[section]) store.currentCV.content[section].splice(Number(index), 1);
};
</script>

<template>
  <div class="flex flex-col h-full bg-slate-50 relative">
    
    <AIImproveModal 
        :isOpen="showAI" 
        :currentText="aiTargetValue"
        @close="showAI = false"
        @apply="handleAIApply"
    />
    
    <div v-if="aiLoading" class="absolute inset-0 bg-white bg-opacity-50 z-40 flex items-center justify-center">
        <span class="text-purple-600 font-bold animate-pulse">✨ AI Generating...</span>
    </div>

    <!-- Tabs -->
    <div class="flex border-b bg-white overflow-x-auto">
        <button 
            v-for="tab in ['profile', 'experience', 'education', 'skills', 'projects', 'languages', 'certifications', 'awards']" 
            :key="tab"
            @click="activeTab = tab"
            :class="['px-4 py-3 capitalize font-medium whitespace-nowrap', activeTab === tab ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-500 hover:text-gray-700']"
        >{{ tab }}</button>
    </div>

    <!-- Content -->
    <div class="p-6 flex-1 overflow-y-auto" v-if="store.currentCV?.content && store.currentCV.content.profile">
        
        <!-- Profile Tab -->
        <div v-if="activeTab === 'profile'" class="space-y-4">
            <h3 class="font-bold text-lg">Personal Details</h3>
            
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
                <label class="block text-sm font-medium mb-1">Summary</label>
                <div class="relative">
                    <textarea v-model="store.currentCV.content.profile.summary" rows="4" class="w-full border p-2 rounded"></textarea>
                    <button 
                        @click="openAI('profile.summary', store.currentCV.content.profile.summary)" 
                        class="absolute bottom-2 right-2 text-xs bg-purple-100 text-purple-700 px-2 py-1 rounded shadow hover:bg-purple-200"
                    >
                        ✨ AI Improve
                    </button>
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
                <div class="grid grid-cols-2 gap-4 mb-2">
                    <input v-model="exp.date" placeholder="Date Range" class="border p-2 rounded" />
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
                 <input v-model="edu.date" placeholder="Date" class="border p-2 rounded w-full" />
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
        
    </div>
  </div>
</template>
