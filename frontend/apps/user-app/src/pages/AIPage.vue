<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { cvApi, aiApi } from '@/api/user.api';
import { ref, onMounted, computed } from 'vue';
import { processAiAnswer } from '@/utils/aiMarkdown';

const auth = useAuthStore();
const myCVs = ref<any[]>([]);
const selectedCVId = ref<number | null>(null);
const inputMessage = ref('');
const isLoadingCVs = ref(false);
const isSending = ref(false);
const selectedLevel = ref<string | null>(null);
const selectedCVDetail = ref<any | null>(null);

const levels = [
    { label: "General", value: null },
    { label: "Intern", value: "INTERN" },
    { label: "Fresher", value: "FRESHER" },
    { label: "Junior", value: "JUNIOR" },
    { label: "Middle", value: "MIDDLE" },
    { label: "Senior", value: "SENIOR" }
];

import { watch } from 'vue';
import { useCVStore } from '@/stores/cv';
import CVRenderer from '@/components/core/CVRenderer.vue';

const cvStore = useCVStore();

const getParsedContent = (content: any) => {
    if (!content) return {};
    if (typeof content === 'object') return content;
    try {
        return JSON.parse(content);
    } catch (e) {
        console.error("Content Parse Error:", e);
        return {};
    }
};

const resolveImage = (src: string | null) => {
    if (!src) return 'https://ui-avatars.com/api/?background=random&color=fff&name=User';
    if (src.startsWith('http') || src.startsWith('blob:') || src.startsWith('data:')) return src;
    return `http://localhost:8080${src.startsWith('/') ? '' : '/'}${src}`;
};

watch(selectedCVId, async (newId) => {
    if (!newId) return;
    try {
        // Use store's loadCV to ensure proper parsing and normalization
        await cvStore.loadCV(newId);
        selectedCVDetail.value = cvStore.currentCV;
        console.log("[CV DEBUG] Loaded via Store:", selectedCVDetail.value);
    } catch (e) {
        console.error("[CV ERROR]", e);
    }
}, { immediate: true });

const cvRenderData = computed(() => {
    const cv = selectedCVDetail.value;
    if (!cv) return null;

    let tmplData = { html: '', css: '' };
    if (cv.templateSnapshot) {
        try {
            tmplData = typeof cv.templateSnapshot === 'string' 
                ? JSON.parse(cv.templateSnapshot) 
                : cv.templateSnapshot;
        } catch (e) {
            console.error("[CV PREVIEW] Template parse error:", e);
        }
    }

    return {
        html: tmplData.html || '',
        css: tmplData.css || '',
        data: getParsedContent(cv.dataJson || cv.content)
    };
});

const messages = ref<{role: 'user' | 'assistant', content: string, html?: string, level?: string | null}[]>([]);

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
    if (!inputMessage.value.trim()) return;
    
    if (!selectedCVId.value) {
        messages.value.push({
            role: 'assistant',
            content: "Please select a CV from the list on the right to start our conversation! 📝"
        });
        return;
    }

    const userMsg = inputMessage.value;
    messages.value.push({ role: 'user', content: userMsg });
    inputMessage.value = '';
    isSending.value = true;

    try {
        const currentLevel = selectedLevel.value;
        const res = await aiApi.chat(selectedCVId.value, userMsg, currentLevel);
        
        const reply = res.data?.answer || res.data?.message || (typeof res.data === 'string' ? res.data : "AI Response Received");
        
        const processedHtml = await processAiAnswer(reply);
        
        messages.value.push({ 
            role: 'assistant', 
            content: reply, 
            html: processedHtml,
            level: currentLevel
        });
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
        <section v-if="auth.isAuthenticated" class="max-w-[100vw] mx-auto px-4 mt-6 h-[calc(100vh-250px)] min-h-[600px]">
            <div class="flex flex-col md:flex-row gap-4 h-full">
                
                <!-- 1. LEFT PANEL: AI CHAT (33%) -->
                <div class="flex-1 md:w-1/3 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col overflow-hidden max-h-full">
                   <div class="p-3 border-b bg-gray-50 flex justify-between items-center shrink-0">
                        <h3 class="font-bold text-gray-700 text-sm">AI Assistant</h3>
                        <!-- Optional Level Selection included as requested -->
                        <div class="flex items-center gap-1.5">
                            <span class="text-[9px] text-gray-400 font-bold">LEVEL:</span>
                            <select 
                                v-model="selectedLevel" 
                                class="text-[10px] border rounded bg-white px-1.5 py-0.5"
                                :disabled="isSending"
                            >
                                <option v-for="l in levels" :key="String(l.value)" :value="l.value">{{ l.label }}</option>
                            </select>
                        </div>
                    </div>

                    <!-- Messages -->
                    <div class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50/50">
                        <div v-if="messages.length === 0" class="text-center text-gray-400 py-10">
                            <div class="text-4xl mb-4">🤖</div>
                            <p class="text-sm">Select a CV and ask me to review it!</p>
                        </div>
                        <div 
                            v-for="(msg, idx) in messages" 
                            :key="idx" 
                            :class="['max-w-[90%] p-3 rounded-xl text-sm leading-relaxed relative shadow-sm', msg.role === 'user' ? 'bg-indigo-600 text-white self-end ml-auto' : 'bg-white border text-gray-800 self-start ai-markdown']"
                        >
                            <!-- Level Badge -->
                            <div v-if="msg.role === 'assistant'" class="mb-2 pb-1.5 border-b border-gray-100 flex items-center justify-between gap-2">
                                <span class="text-[9px] font-bold text-indigo-600 uppercase">
                                    [{{ msg.level || 'GENERAL' }}]
                                </span>
                            </div>

                            <div v-if="msg.html" v-html="msg.html"></div>
                            <div v-else>{{ msg.content }}</div>
                        </div>
                        <div v-if="isSending" class="self-start bg-white border p-3 rounded-xl text-[10px] text-gray-400 italic shadow-sm">
                            AI is thinking...
                        </div>
                    </div>

                    <!-- Input -->
                    <div class="p-3 border-t bg-white">
                        <div class="flex flex-col gap-2">
                            <textarea 
                                v-model="inputMessage" 
                                @keyup.enter.prevent="sendMessage"
                                :disabled="isSending"
                                placeholder="Type a message..." 
                                class="w-full border p-2 text-sm rounded-lg focus:ring-1 focus:ring-indigo-500 focus:outline-none resize-none h-20"
                            ></textarea>
                            <button 
                                @click="sendMessage" 
                                :disabled="isSending"
                                class="bg-indigo-600 text-white py-2 rounded-lg font-bold hover:bg-indigo-700 disabled:bg-gray-300 transition text-sm shadow-md"
                            >
                                Send Message
                            </button>
                        </div>
                    </div>
                </div>

                <!-- 2. CENTER PANEL: CV PREVIEW (42%) -->
                <div class="flex-[1.25] bg-gray-100 rounded-xl border border-gray-200 flex flex-col overflow-hidden relative">
                    <div v-if="selectedCVDetail && cvRenderData" class="h-full flex flex-col">
                        <div class="p-3 bg-white border-b flex justify-between items-center shadow-sm">
                            <span class="text-xs font-bold text-gray-500 uppercase tracking-wider">Preview: {{ selectedCVDetail.title }}</span>
                        </div>
                        <div class="flex-1 overflow-hidden">
                           <CVRenderer 
                            v-if="cvRenderData"
                            :html="cvRenderData.html"
                            :css="cvRenderData.css"
                            :data="cvRenderData.data"
                           />
                        </div>
                        
                        <!-- TEMP DEBUG -->
                        <div v-if="selectedCVDetail" class="p-2 border-t bg-gray-50 max-h-32 overflow-auto">
                            <pre class="text-[8px] text-gray-400">DEBUG: {{ selectedCVDetail.title }} loaded</pre>
                        </div>
                    </div>
                    <div v-else class="flex-1 flex flex-col items-center justify-center text-gray-400 bg-gray-50 italic">
                        <div class="text-5xl mb-4 opacity-20">🔎</div>
                        <p class="text-sm font-medium">Select a CV on the right to preview content here</p>
                    </div>
                </div>

                <!-- 3. RIGHT PANEL: CV SELECTOR (25%) -->
                <div class="md:w-1/4 bg-white rounded-xl shadow-sm border border-gray-100 flex flex-col overflow-hidden">
                    <div class="p-3 border-b bg-gray-50 flex items-center justify-between">
                        <h3 class="font-bold text-gray-700 text-sm">Your CVs</h3>
                        <span class="bg-gray-200 text-gray-600 text-[10px] px-2 py-0.5 rounded-full font-bold">{{ myCVs.length }}</span>
                    </div>
                    <div class="overflow-y-auto flex-1 p-2 space-y-2">
                        <div v-if="isLoadingCVs" class="text-center py-4 text-gray-300 text-xs animate-pulse">Fetching CVs...</div>
                        <div v-else-if="myCVs.length === 0" class="text-center py-8 px-4 text-gray-500 text-xs italic">
                            No CVs found.
                        </div>
                        <button 
                            v-for="cv in myCVs" 
                            :key="cv.id" 
                            @click="selectedCVId = cv.id"
                            :class="['w-full text-left p-3 rounded-xl border transition-all duration-300 transform', selectedCVId === cv.id ? 'bg-indigo-50 border-indigo-400 shadow-md ring-1 ring-indigo-200 scale-[1.02]' : 'bg-white border-gray-100 hover:border-indigo-200 hover:bg-gray-50']"
                        >
                            <div class="flex items-start gap-3">
                                <div :class="['w-8 h-8 rounded-lg flex items-center justify-center shrink-0', selectedCVId === cv.id ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-400']">
                                    <span class="text-xs">📄</span>
                                </div>
                                <div class="overflow-hidden">
                                    <div :class="['text-sm truncate font-bold', selectedCVId === cv.id ? 'text-indigo-900' : 'text-gray-700']">{{ cv.title || 'Untitled' }}</div>
                                    <div class="text-[10px] text-gray-400 mt-1 flex items-center gap-1">
                                        🕒 {{ new Date(cv.updatedAt).toLocaleDateString() }}
                                    </div>
                                </div>
                            </div>
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

<style scoped>
.ai-markdown :deep(h1), .ai-markdown :deep(h2), .ai-markdown :deep(h3), .ai-markdown :deep(h4) {
    margin-top: 0.5rem;
    margin-bottom: 0.5rem;
    font-weight: bold;
}
.ai-markdown :deep(h1) { font-size: 1.5rem; }
.ai-markdown :deep(h2) { font-size: 1.25rem; }
.ai-markdown :deep(h3) { font-size: 1.125rem; }
.ai-markdown :deep(ul), .ai-markdown :deep(ol) {
    padding-left: 1.5rem;
    margin-bottom: 0.5rem;
}
.ai-markdown :deep(ul) { list-style-type: disc; }
.ai-markdown :deep(ol) { list-style-type: decimal; }
.ai-markdown :deep(p) {
    margin-bottom: 0.5rem;
    line-height: 1.5;
}
.ai-markdown :deep(code) {
    background-color: #f3f4f6;
    padding: 0.1rem 0.3rem;
    border-radius: 0.25rem;
    font-family: monospace;
}
.ai-markdown :deep(pre) {
    background-color: #1f2937;
    color: #f3f4f6;
    padding: 1rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin-bottom: 0.5rem;
}
.ai-markdown :deep(blockquote) {
    border-left: 4px solid #e5e7eb;
    padding-left: 1rem;
    color: #6b7280;
    font-style: italic;
}
</style>
