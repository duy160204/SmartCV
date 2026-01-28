<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { cvApi, aiApi } from '@/api/user.api';
import { ref, onMounted, computed } from 'vue';

const auth = useAuthStore();
const myCVs = ref<any[]>([]);
const selectedCVId = ref<number | null>(null);
const messages = ref<{role: 'user' | 'assistant', content: string}[]>([]);
const inputMessage = ref('');
const isLoadingCVs = ref(false);
const isSending = ref(false);

const features = [
    { 
        title: 'Smart CV Review',
        desc: 'Chat with AI to get feedback on your resume structure.',
        icon: '📝'
    },
    {
        title: 'Content Improvement', 
        desc: 'Ask AI to rewrite bullet points for better impact.',
        icon: '✨'
    },
    {
        title: 'Job Alignment',
        desc: 'Paste a job description and ask AI if your CV matches.',
        icon: '🎯'
    }
];

onMounted(async () => {
    if (auth.isAuthenticated) {
        isLoadingCVs.value = true;
        try {
            const res = await cvApi.getAll();
            myCVs.value = res.data;
            if (myCVs.value.length > 0) {
                selectedCVId.value = myCVs.value[0].id;
            }
        } catch (e) {
            console.error("Failed to fetch CVs", e);
        } finally {
            isLoadingCVs.value = false;
        }
    }
});

const selectedCV = computed(() => myCVs.value.find(c => c.id === selectedCVId.value));

const sendMessage = async () => {
    if (!inputMessage.value.trim() || !selectedCVId.value) return;

    const userMsg = inputMessage.value;
    messages.value.push({ role: 'user', content: userMsg });
    inputMessage.value = '';
    isSending.value = true;

    try {
        const res = await aiApi.chat(selectedCVId.value, userMsg);
        // Assuming response structure, adapting if necessary. 
        // Based on typical AI response, it might be res.data.content or similar.
        // Let's assume the API returns the string or an object with 'message'/'content'.
        // If undefined, fallback to "AI processed your request."
        const reply = res.data.answer || res.data.message || res.data.content || typeof res.data === 'string' ? res.data : "AI Response Received";
        messages.value.push({ role: 'assistant', content: reply });
    } catch (e: any) {
        messages.value.push({ role: 'assistant', content: "Sorry, I encountered an error: " + (e.message || "Unknown error") });
    } finally {
        isSending.value = false;
    }
};
</script>

<template>
    <div class="min-h-screen bg-gray-50 pb-20">
        <!-- Hero / Header -->
        <section class="bg-indigo-700 text-white py-12 px-6">
            <div class="max-w-4xl mx-auto text-center">
                <h1 class="text-3xl md:text-4xl font-extrabold mb-4">AI Career Assistant</h1>
                <p class="text-indigo-100 max-w-2xl mx-auto">
                    Select a CV and chat with our AI to optimize your content.
                </p>
                <div v-if="!auth.isAuthenticated" class="mt-6">
                     <button @click="$router.push('/register')" class="bg-white text-indigo-700 px-6 py-2 rounded-full font-bold hover:bg-gray-100 transition shadow">
                        Get Started Free
                    </button>
                </div>
            </div>
        </section>

        <!-- User AI Workspace (Logged In) -->
        <section v-if="auth.isAuthenticated" class="max-w-5xl mx-auto mt-8 px-6 flex flex-col md:flex-row gap-6 h-[600px]">
            
            <!-- Sidebar: Select CV -->
            <div class="md:w-1/3 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col">
                <div class="p-4 border-b bg-gray-50">
                    <h3 class="font-bold text-gray-700">Select CV to Analyze</h3>
                </div>
                <div class="overflow-y-auto flex-1 p-2">
                    <div v-if="isLoadingCVs" class="text-center py-4 text-gray-400">Loading your CVs...</div>
                    <div v-else-if="myCVs.length === 0" class="text-center py-8 px-4 text-gray-500 text-sm">
                        You have no CVs yet. <router-link to="/" class="text-blue-600 underline">Create one</router-link> to use AI.
                    </div>
                    <button 
                        v-else
                        v-for="cv in myCVs" 
                        :key="cv.id" 
                        @click="selectedCVId = cv.id"
                        :class="['w-full text-left p-3 rounded-lg mb-1 transition text-sm flex items-center gap-3', selectedCVId === cv.id ? 'bg-indigo-50 text-indigo-700 font-bold border border-indigo-200' : 'hover:bg-gray-50 text-gray-600']"
                    >
                        <span class="text-lg">📄</span>
                        <div class="truncate">
                            <div class="truncate">{{ cv.title || 'Untitled CV' }}</div>
                            <div class="text-[10px] opacity-70">{{ new Date(cv.updatedAt).toLocaleDateString() }}</div>
                        </div>
                    </button>
                </div>
            </div>

            <!-- Chat Area -->
            <div class="flex-1 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col">
                <div class="p-4 border-b bg-gray-50 flex justify-between items-center">
                    <h3 class="font-bold text-gray-700">
                        Chatting about: <span class="text-indigo-600">{{ selectedCV?.title || 'No CV Selected' }}</span>
                    </h3>
                </div>

                <!-- Messages -->
                <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50/50">
                    <div v-if="messages.length === 0" class="text-center text-gray-400 py-20">
                        <div class="text-4xl mb-4">🤖</div>
                        <p>Ask AI to review this CV, suggest improvements, or rewrite sections.</p>
                    </div>
                    <div 
                        v-for="(msg, idx) in messages" 
                        :key="idx" 
                        :class="['max-w-[85%] p-3 rounded-xl text-sm leading-relaxed', msg.role === 'user' ? 'bg-blue-600 text-white self-end ml-auto' : 'bg-white border text-gray-800 self-start']"
                    >
                        {{ msg.content }}
                    </div>
                    <div v-if="isSending" class="self-start bg-gray-100 p-3 rounded-xl text-xs text-gray-500">
                        AI is thinking...
                    </div>
                </div>

                <!-- Input -->
                <div class="p-4 border-t bg-white">
                    <div class="flex gap-2">
                        <input 
                            v-model="inputMessage" 
                            @keyup.enter="sendMessage"
                            :disabled="!selectedCVId || isSending"
                            placeholder="Ask AI to improve your CV..." 
                            class="flex-1 border p-3 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none disabled:bg-gray-100"
                        />
                        <button 
                            @click="sendMessage" 
                            :disabled="!selectedCVId || isSending"
                            class="bg-indigo-600 text-white px-6 rounded-lg font-bold hover:bg-indigo-700 disabled:bg-gray-300 transition"
                        >
                            Send
                        </button>
                    </div>
                </div>
            </div>
        </section>

        <!-- Feature Grid (Logged Out View mostly) -->
        <section v-if="!auth.isAuthenticated" class="max-w-6xl mx-auto px-6 mt-10">
            <div class="grid md:grid-cols-3 gap-6">
                <div v-for="f in features" :key="f.title" class="bg-white p-8 rounded-xl shadow-lg border border-gray-100">
                    <div class="text-4xl mb-4">{{ f.icon }}</div>
                    <h3 class="text-xl font-bold text-gray-800 mb-2">{{ f.title }}</h3>
                    <p class="text-gray-600">{{ f.desc }}</p>
                </div>
            </div>
        </section>
    </div>
</template>
