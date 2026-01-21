<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { ref } from 'vue';

const auth = useAuthStore();
const features = [
    { 
        title: 'Smart CV Review',
        desc: 'Get instant feedback on your resume structure and content.',
        icon: '📝'
    },
    {
        title: 'Bullet Point Improver', 
        desc: 'Turn weak descriptions into powerful, action-oriented achievements.',
        icon: '✨'
    },
    {
        title: 'ATS Optimization',
        desc: 'Identify keywords missing from your CV based on job descriptions.',
        icon: '🎯'
    }
];

const mockAnalyses = ref([
    { id: 1, cvTitle: 'Software Engineer Resume', score: 85, status: 'Good' },
    { id: 2, cvTitle: 'Project Manager CV', score: 72, status: 'Needs Improvement' },
]);
</script>

<template>
    <div class="min-h-screen bg-gray-50 pb-20">
        <!-- Hero / Header -->
        <section class="bg-indigo-700 text-white py-16 px-6 relative overflow-hidden">
            <div class="max-w-4xl mx-auto text-center relative z-10">
                <h1 class="text-4xl md:text-5xl font-extrabold mb-6">AI Career Assistant</h1>
                <p class="text-indigo-100 text-xl max-w-2xl mx-auto">
                    Power up your job search with our advanced AI tools. 
                    Structure your resume, write better bullet points, and beat the ATS.
                </p>
                <div v-if="!auth.isAuthenticated" class="mt-8">
                     <button @click="$router.push('/register')" class="bg-white text-indigo-700 px-8 py-3 rounded-full font-bold hover:bg-gray-100 transition shadow-lg">
                        Get Started Free
                    </button>
                    <p class="mt-4 text-xs opacity-70">No credit card required</p>
                </div>
            </div>
            
            <!-- Decor -->
            <div class="absolute top-0 left-0 w-64 h-64 bg-indigo-500 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
            <div class="absolute bottom-0 right-0 w-64 h-64 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>
        </section>

        <!-- Feature Grid -->
        <section class="max-w-6xl mx-auto px-6 -mt-10 relative z-20">
            <div class="grid md:grid-cols-3 gap-6">
                <div v-for="f in features" :key="f.title" class="bg-white p-8 rounded-xl shadow-lg border border-gray-100 hover:-translate-y-1 transition duration-300">
                    <div class="text-4xl mb-4">{{ f.icon }}</div>
                    <h3 class="text-xl font-bold text-gray-800 mb-2">{{ f.title }}</h3>
                    <p class="text-gray-600">{{ f.desc }}</p>
                </div>
            </div>
        </section>

        <!-- User AI Workspace (Logged In) -->
        <section v-if="auth.isAuthenticated" class="max-w-4xl mx-auto mt-16 px-6">
            <div class="flex items-center justify-between mb-8">
                <h2 class="text-2xl font-bold text-gray-800">My AI Analyses</h2>
                <router-link to="/" class="text-blue-600 font-bold hover:underline">Go to My CVs to Run New Analysis</router-link>
            </div>

            <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div class="p-6 border-b bg-gray-50 flex justify-between items-center">
                     <span class="font-bold text-gray-500 uppercase text-xs">Recent Activity</span>
                </div>
                
                <div v-if="mockAnalyses.length > 0">
                    <div v-for="item in mockAnalyses" :key="item.id" class="p-6 border-b last:border-0 hover:bg-gray-50 transition flex justify-between items-center">
                        <div>
                            <h4 class="font-bold text-gray-800">{{ item.cvTitle }}</h4>
                            <p class="text-xs text-gray-500">Last analyzed 2 hours ago</p>
                        </div>
                        <div class="flex items-center gap-4">
                            <div class="text-right">
                                <div class="text-2xl font-bold text-indigo-600">{{ item.score }}</div>
                                <div class="text-[10px] text-gray-400 uppercase">Score</div>
                            </div>
                            <button class="bg-gray-100 text-gray-700 px-4 py-2 rounded-lg text-sm font-bold hover:bg-gray-200">View Report</button>
                        </div>
                    </div>
                </div>
                <div v-else class="p-12 text-center text-gray-500">
                    No analyses yet. Open a CV to start using AI tools.
                </div>
            </div>
            
            <div class="mt-8 bg-blue-50 border border-blue-100 rounded-xl p-6 flex gap-4 items-start">
                <div class="text-2xl">💡</div>
                <div>
                   <h4 class="font-bold text-blue-900">How to use AI?</h4>
                   <p class="text-blue-800 text-sm mt-1">Open any CV in the <strong><router-link to="/" class="underline">Dashboard</router-link></strong> and click the "AI Assistant" tab in the editor sidebar.</p>
                </div>
            </div>
        </section>

        <!-- Guest Call to Action (Logged Out) -->
        <section v-else class="max-w-4xl mx-auto mt-20 px-6 text-center">
            <div class="bg-gradient-to-r from-gray-900 to-gray-800 text-white rounded-2xl p-12 shadow-2xl">
                <h2 class="text-3xl font-bold mb-4">Ready to optimize your CV?</h2>
                <p class="text-gray-300 mb-8 max-w-xl mx-auto">Join thousands of job seekers using SmartCV AI to land their dream jobs faster.</p>
                <button @click="$router.push('/register')" class="bg-indigo-500 text-white px-8 py-4 rounded-full font-bold hover:bg-indigo-600 transition transform hover:scale-105 shadow-xl">
                    Create Your Account Now
                </button>
            </div>
        </section>
    </div>
</template>
