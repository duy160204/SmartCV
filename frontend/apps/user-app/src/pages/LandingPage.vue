<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MainLayout from '@/layouts/MainLayout.vue';
import TemplateSection from '@/components/landing/TemplateSection.vue';
import { useAuthStore } from '@/stores/auth';
import { Star } from 'lucide-vue-next';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const auth = useAuthStore();
const layoutRef = ref<InstanceType<typeof MainLayout> | null>(null);
const templatesRef = ref<HTMLElement | null>(null);

/* HERO */
const showHero = ref(false);
onMounted(() => {
  setTimeout(() => (showHero.value = true), 100);
  if (window.location.hash === '#template-section') {
    setTimeout(() => {
      document.getElementById('template-section')?.scrollIntoView({ behavior: 'smooth' });
    }, 500);
  }
});




/* ACTION */
const triggerAuth = (mode: 'login' | 'register' = 'register') => {
  if (auth.isAuthenticated) {
    templatesRef.value?.scrollIntoView({ behavior: 'smooth' });
  } else {
    (layoutRef.value as any)?.openAuth(mode);
  }
};
</script>

<template>
  <MainLayout ref="layoutRef">

    <!-- HERO -->
    <section class="pt-32 pb-28 px-6 bg-gradient-to-b from-white to-gray-50">
      <div class="max-w-6xl mx-auto text-center">

        <h1
            class="text-5xl md:text-6xl font-extrabold leading-tight transition-all duration-700"
            :class="showHero ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'"
        >
          {{ t('landing.hero_title_1') }} <span class="text-blue-600">{{ t('landing.hero_highlight') }}</span><br/>
          {{ t('landing.hero_title_2') }}
        </h1>

        <p class="text-lg text-gray-600 mt-6 max-w-2xl mx-auto leading-relaxed">
          {{ t('landing.hero_desc') }}
        </p>

        <div class="flex justify-center gap-4 mt-10 flex-wrap">
          <button
              @click="triggerAuth('register')"
              class="px-8 py-4 bg-blue-600 text-white rounded-xl
            hover:bg-blue-700 hover:scale-105 transition shadow-md"
          >
            {{ t('landing.get_started') }}
          </button>

          <button
              @click="templatesRef?.scrollIntoView({ behavior: 'smooth' })"
              class="px-8 py-4 border rounded-xl hover:bg-gray-100 transition"
          >
            {{ t('landing.view_templates') }}
          </button>
        </div>

        <!-- TRUST MINI -->
        <div class="flex justify-center items-center gap-6 mt-8 text-sm text-gray-500 flex-wrap">
          <div class="flex items-center gap-2">
            <div class="flex text-yellow-400">
              <Star v-for="i in 5" :key="i" class="w-4 h-4 fill-yellow-400"/>
            </div>
            <span>{{ t('landing.trust_rating') }}</span>
          </div>

          <div>✔ {{ t('landing.trust_no_card') }}</div>
          <div>✔ {{ t('landing.trust_10min') }}</div>
          <div>✔ {{ t('landing.trust_ats') }}</div>
        </div>

      </div>
    </section>

    <!-- VIDEO (FIX NHẸ + MƯỢT) -->
    <section class="relative h-[300px] md:h-[340px] overflow-hidden">

      <video autoplay muted loop playsinline
             class="absolute inset-0 w-full h-full object-cover scale-105">
        <source src="/video/video111.mp4" type="video/mp4" />
      </video>

      <!-- gradient nối HERO -->
      <div class="absolute top-0 left-0 w-full h-24 bg-gradient-to-b from-white to-transparent"></div>

      <!-- gradient nối xuống dưới -->
      <div class="absolute bottom-0 left-0 w-full h-24 bg-gradient-to-t from-white to-transparent"></div>

      <!-- overlay nhẹ -->
      <div class="absolute inset-0 bg-black/20 backdrop-blur-[1px]"></div>

      <div class="relative z-10 flex items-center justify-center h-full text-center px-6">
        <div>
          <h2 class="text-2xl md:text-3xl font-bold text-white mb-2">
            {{ t('landing.video_title') }}
          </h2>
          <p class="text-gray-200 text-sm md:text-base">
            {{ t('landing.video_desc') }}
          </p>
        </div>
      </div>

    </section>

    <!-- TEMPLATE -->
    <div id="template-section" ref="templatesRef" class="bg-gray-50 py-20">
      <TemplateSection @trigger-auth="triggerAuth" />
    </div>

    <!-- TOOLS -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-6">

        <h2 class="text-4xl font-bold text-center mb-4">
          {{ t('landing.tools_title') }}
        </h2>
        <p class="text-center text-gray-500 mb-16 max-w-2xl mx-auto">
          {{ t('landing.tools_subtitle') }}
        </p>

        <div class="grid md:grid-cols-3 gap-8">

          <!-- 1. JobHunter -->
          <a
            href="https://jobhunter-vn.vercel.app/"
            target="_blank"
            rel="noopener noreferrer"
            class="group bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-100 rounded-2xl p-8 flex flex-col transition-all duration-300 hover:shadow-xl hover:-translate-y-1"
          >
            <div class="w-14 h-14 bg-blue-100 text-blue-600 rounded-2xl flex items-center justify-center text-2xl mb-5 group-hover:scale-110 transition-transform">
              🧭
            </div>
            <h3 class="text-xl font-bold text-gray-800 mb-3">{{ t('landing.tool_jobhunter_title') }}</h3>
            <ul class="text-sm text-gray-600 space-y-2 mb-6 flex-1">
              <li class="flex items-start gap-2"><span class="text-blue-500 mt-0.5">✦</span> {{ t('landing.tool_jobhunter_1') }}</li>
              <li class="flex items-start gap-2"><span class="text-blue-500 mt-0.5">✦</span> {{ t('landing.tool_jobhunter_2') }}</li>
              <li class="flex items-start gap-2"><span class="text-blue-500 mt-0.5">✦</span> {{ t('landing.tool_jobhunter_3') }}</li>
            </ul>
            <div class="text-sm font-semibold text-blue-600 group-hover:underline flex items-center gap-1">
              {{ t('landing.tool_jobhunter_cta') }} <span class="group-hover:translate-x-1 transition-transform">→</span>
            </div>
          </a>

          <!-- 2. Yoodli -->
          <a
            href="https://yoodli.ai/"
            target="_blank"
            rel="noopener noreferrer"
            class="group bg-gradient-to-br from-green-50 to-emerald-50 border border-green-100 rounded-2xl p-8 flex flex-col transition-all duration-300 hover:shadow-xl hover:-translate-y-1"
          >
            <div class="w-14 h-14 bg-green-100 text-green-600 rounded-2xl flex items-center justify-center text-2xl mb-5 group-hover:scale-110 transition-transform">
              🎤
            </div>
            <h3 class="text-xl font-bold text-gray-800 mb-3">{{ t('landing.tool_yoodli_title') }}</h3>
            <ul class="text-sm text-gray-600 space-y-2 mb-6 flex-1">
              <li class="flex items-start gap-2"><span class="text-green-500 mt-0.5">✦</span> {{ t('landing.tool_yoodli_1') }}</li>
              <li class="flex items-start gap-2"><span class="text-green-500 mt-0.5">✦</span> {{ t('landing.tool_yoodli_2') }}</li>
              <li class="flex items-start gap-2"><span class="text-green-500 mt-0.5">✦</span> {{ t('landing.tool_yoodli_3') }}</li>
            </ul>
            <div class="text-sm font-semibold text-green-600 group-hover:underline flex items-center gap-1">
              {{ t('landing.tool_yoodli_cta') }} <span class="group-hover:translate-x-1 transition-transform">→</span>
            </div>
          </a>

          <!-- 3. HiringLab -->
          <a
            href="https://www.hiringlab.org/"
            target="_blank"
            rel="noopener noreferrer"
            class="group bg-gradient-to-br from-purple-50 to-violet-50 border border-purple-100 rounded-2xl p-8 flex flex-col transition-all duration-300 hover:shadow-xl hover:-translate-y-1"
          >
            <div class="w-14 h-14 bg-purple-100 text-purple-600 rounded-2xl flex items-center justify-center text-2xl mb-5 group-hover:scale-110 transition-transform">
              📊
            </div>
            <h3 class="text-xl font-bold text-gray-800 mb-3">{{ t('landing.tool_hiringlab_title') }}</h3>
            <ul class="text-sm text-gray-600 space-y-2 mb-6 flex-1">
              <li class="flex items-start gap-2"><span class="text-purple-500 mt-0.5">✦</span> {{ t('landing.tool_hiringlab_1') }}</li>
              <li class="flex items-start gap-2"><span class="text-purple-500 mt-0.5">✦</span> {{ t('landing.tool_hiringlab_2') }}</li>
              <li class="flex items-start gap-2"><span class="text-purple-500 mt-0.5">✦</span> {{ t('landing.tool_hiringlab_3') }}</li>
            </ul>
            <div class="text-sm font-semibold text-purple-600 group-hover:underline flex items-center gap-1">
              {{ t('landing.tool_hiringlab_cta') }} <span class="group-hover:translate-x-1 transition-transform">→</span>
            </div>
          </a>

        </div>
      </div>
    </section>

    <!-- TRUST -->
    <section class="py-16 bg-gray-50 text-center">
      <p class="text-gray-500 mb-6">{{ t('landing.trust_from') }}</p>
      <div class="flex justify-center gap-10 opacity-70">
        <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/google.svg" class="h-6"/>
        <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/amazon.svg" class="h-6"/>
        <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/meta.svg" class="h-6"/>
        <img src="https://cdn.jsdelivr.net/gh/simple-icons/simple-icons/icons/microsoft.svg" class="h-6"/>
      </div>
    </section>



    <!-- CTA (FIX KHÔNG BỊ KHỐI CỨNG) -->
    <section class="py-28 text-center bg-gradient-to-b from-white to-blue-600 text-white">
      <div class="max-w-3xl mx-auto">

        <h2 class="text-4xl font-bold mb-6 text-gray-900">
          {{ t('landing.cta_title') }}
        </h2>

        <button
            @click="triggerAuth('register')"
            class="px-10 py-4 bg-blue-600 text-white rounded-xl
          hover:bg-blue-700 hover:scale-105 transition shadow-lg"
        >
          {{ t('landing.start_free') }}
        </button>

      </div>
    </section>

  </MainLayout>
</template>