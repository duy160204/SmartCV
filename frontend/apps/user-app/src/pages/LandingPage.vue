<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import MainLayout from '@/layouts/MainLayout.vue';
import TemplateSection from '@/components/landing/TemplateSection.vue';
import AuthModal from '@/components/auth/AuthModal.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const auth = useAuthStore();

// We need to access the handleToggleAuth from MainLayout, but since MainLayout wraps the page,
// we usually control the state from top-down or via store.
// However, MainLayout manages the global modal state in our design.
// Wait, MainLayout was designed to wrap <slot>.
// But LandingPage is a page component routed by RouterView.
// So App.vue renders RouterView. 
// If we want MainLayout to be the wrapper, we should use it here.

// But wait, the Navbar in MainLayout emits events. The AuthModal is in MainLayout.
// We need a way to trigger AuthModal from INSIDE LandingPage (e.g. "Get Started" hero button).
// We can provide/inject or use a global UI store, or simply expose a ref if possible.
// For now, let's duplication the modal control or refactor MainLayout to expose a slot prop? Or simpler:
// Let's rely on MainLayout providing the layout structure, but maybe we move AuthModal one level up?
// Or, allow LandingPage to emit an event? No, LandingPage is top of route.

// BETTER APPROACH: 
// Use a composable or store for UI state if we want to trigger it from anywhere.
// OR: Just let LandingPage pass a prop to MainLayout? No.
// Let's add specific logic: LandingPage will define the content.
// The "Get Started" button in Hero needs to trigger auth.
// We can use a simple event bus or just props if MainLayout was a parent.
// Since MainLayout is used AS A COMPONENT here:
// <MainLayout ref="layout"> ... </MainLayout>
// We can call methods on it if we expose them.

const layoutRef = ref();

const triggerAuth = (mode: 'login' | 'register' = 'register') => {
    // If user is already logged in, redirect to templates or create flow
    if (auth.isAuthenticated) {
        // Scroll to templates
        document.getElementById('templates')?.scrollIntoView({ behavior: 'smooth' });
    } else {
        // Open Modal via Layout Ref
        // We will modify MainLayout to expose this, or simpler:
        // We can just emit an event from the child components to LandingPage, 
        // and LandingPage passes it to MainLayout via Template Props?
        // Actually, simplest is to bind v-model or expose a method.
        layoutRef.value?.openAuth(mode);
    }
};

const scrollToTemplates = () => {
    document.getElementById('templates')?.scrollIntoView({ behavior: 'smooth' });
};
</script>

<template>
  <MainLayout ref="layoutRef">
      <!-- Hero Section -->
      <header class="relative px-6 pt-16 pb-24 text-center overflow-hidden">
          <div class="absolute inset-0 -z-10 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-blue-50 via-white to-white"></div>
          
          <div class="max-w-4xl mx-auto mb-12">
              <span class="inline-block py-1.5 px-4 rounded-full bg-blue-100 text-blue-700 text-sm font-bold mb-6 animate-pulse">
                ✨ AI-Powered Resume Builder
              </span>
              <h1 class="text-5xl md:text-7xl font-extrabold tracking-tight mb-8 leading-tight text-gray-900">
                  Build a <span class="text-blue-600">Pro Resume</span><br class="hidden md:block" /> in Minutes.
              </h1>
              <p class="text-xl text-gray-600 mb-10 max-w-2xl mx-auto leading-relaxed">
                  Stop struggling with formatting. Let our AI write and design your perfect CV so you can get hired 2x faster.
              </p>
              
              <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
                  <button 
                    @click="triggerAuth('register')" 
                    class="w-full sm:w-auto px-8 py-4 bg-blue-600 text-white text-lg font-bold rounded-xl shadow-xl hover:bg-blue-700 hover:scale-105 transition transform shadow-blue-500/20"
                  >
                      Create My CV Now
                  </button>
                  <button 
                    @click="scrollToTemplates" 
                    class="w-full sm:w-auto px-8 py-4 bg-white text-gray-700 border border-gray-200 text-lg font-bold rounded-xl hover:bg-gray-50 transition"
                  >
                      View Templates
                  </button>
              </div>
          </div>

          <!-- Hero Visual / abstract representation -->
          <div class="max-w-5xl mx-auto mt-8 transform hover:scale-[1.01] transition duration-700">
               <div class="rounded-2xl shadow-2xl border border-gray-200 overflow-hidden bg-white">
                   <div class="h-12 bg-gray-50 border-b flex items-center px-4 gap-2">
                       <div class="w-3 h-3 rounded-full bg-red-400"></div>
                       <div class="w-3 h-3 rounded-full bg-yellow-400"></div>
                       <div class="w-3 h-3 rounded-full bg-green-400"></div>
                   </div>
                   <!-- Visual Content can stay same as before or simplified -->
                   <div class="bg-gray-100 p-8 grid grid-cols-1 md:grid-cols-2 gap-8 h-[400px] items-center justify-items-center relative overflow-hidden">
                        <!-- Left: Editor Mock -->
                        <div class="bg-white p-6 shadow-md rounded-lg w-full max-w-sm space-y-4 z-10">
                            <div class="flex gap-4 mb-4">
                                <div class="w-12 h-12 bg-gray-200 rounded-full"></div>
                                <div class="space-y-2 flex-1">
                                    <div class="h-4 w-1/2 bg-gray-200 rounded"></div>
                                    <div class="h-3 w-1/3 bg-gray-100 rounded"></div>
                                </div>
                            </div>
                            <div class="h-2 bg-gray-100 rounded w-full"></div>
                            <div class="h-2 bg-gray-100 rounded w-5/6"></div>
                            <div class="h-2 bg-gray-100 rounded w-4/6"></div>
                            
                            <div class="mt-4 p-3 bg-blue-50 border border-blue-100 rounded flex gap-2">
                                <span class="text-xl">✨</span>
                                <div class="text-xs text-blue-800">
                                    <strong>AI Suggestion:</strong> Use active verbs like "Spearheaded" instead of "Led".
                                </div>
                            </div>
                        </div>

                        <!-- Right: Template Result -->
                        <div class="hidden md:block absolute right-0 top-10 md:static md:w-full md:max-w-xs bg-white shadow-2xl transform rotate-3 hover:rotate-0 transition duration-500 border rounded-lg h-[450px] overflow-hidden">
                             <div class="h-full w-full bg-gray-50 p-4 space-y-2">
                                 <div class="h-6 w-3/4 bg-gray-800 rounded mb-4"></div> 
                                 <div class="h-2 w-full bg-gray-300 rounded"></div>
                                 <div class="h-2 w-full bg-gray-300 rounded"></div>
                                 <div class="grid grid-cols-3 gap-2 mt-4">
                                     <div class="col-span-1 h-32 bg-gray-200 rounded"></div>
                                     <div class="col-span-2 space-y-2">
                                          <div class="h-2 w-full bg-gray-300 rounded"></div>
                                          <div class="h-2 w-11/12 bg-gray-300 rounded"></div>
                                     </div>
                                 </div>
                             </div>
                        </div>
                   </div>
               </div>
          </div>
      </header>

      <!-- Template Section (Dynamic) -->
      <TemplateSection @trigger-auth="triggerAuth" />

      <!-- Features / Value Prop -->
      <section class="py-24 bg-white">
          <div class="max-w-6xl mx-auto px-8">
              <div class="text-center mb-16">
                  <h2 class="text-3xl font-bold mb-4">Why SmartCV?</h2>
                  <p class="text-gray-600 max-w-2xl mx-auto">We combine professional design with powerful AI to help you stand out from the crowd.</p>
              </div>
              
              <div class="grid md:grid-cols-3 gap-12">
                   <div class="text-center p-6">
                       <div class="w-16 h-16 mx-auto bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center text-3xl mb-6">🤖</div>
                       <h3 class="text-xl font-bold mb-3">AI Writer</h3>
                       <p class="text-gray-600">Generate bullet points and summaries instantly tailored to your role.</p>
                   </div>
                   <div class="text-center p-6">
                       <div class="w-16 h-16 mx-auto bg-purple-100 text-purple-600 rounded-2xl flex items-center justify-center text-3xl mb-6">🎨</div>
                       <h3 class="text-xl font-bold mb-3">Live Preview</h3>
                       <p class="text-gray-600">See changes in real-time. Switch templates without losing your data.</p>
                   </div>
                   <div class="text-center p-6">
                       <div class="w-16 h-16 mx-auto bg-green-100 text-green-600 rounded-2xl flex items-center justify-center text-3xl mb-6">🔒</div>
                       <h3 class="text-xl font-bold mb-3">Privacy First</h3>
                       <p class="text-gray-600">Your data is secure. We don't sell your information to third parties.</p>
                   </div>
              </div>
          </div>
      </section>

  </MainLayout>
</template>
