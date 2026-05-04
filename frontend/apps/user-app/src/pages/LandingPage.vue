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
onMounted(() => setTimeout(() => (showHero.value = true), 100));

/* FEATURE */
const activeFeature = ref(0);

/* FAQ */
const faqs = ref([
  { qKey: 'landing.faq_q1', aKey: 'landing.faq_a1', open: false },
  { qKey: 'landing.faq_q2', aKey: 'landing.faq_a2', open: false },
  { qKey: 'landing.faq_q3', aKey: 'landing.faq_a3', open: false },
  { qKey: 'landing.faq_q4', aKey: 'landing.faq_a4', open: false },
  { qKey: 'landing.faq_q5', aKey: 'landing.faq_a5', open: false }
]);

const toggleFAQ = (i: number) => faqs.value[i].open = !faqs.value[i].open;

/* ACTION */
const triggerAuth = (mode: 'login' | 'register' = 'register') => {
  if (auth.isAuthenticated) {
    templatesRef.value?.scrollIntoView({ behavior: 'smooth' });
  } else {
    layoutRef.value?.openAuth(mode);
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
    <div ref="templatesRef" class="bg-gray-50 py-20">
      <TemplateSection @trigger-auth="triggerAuth" />
    </div>

    <!-- EVERYTHING -->
    <section class="py-24 bg-white">
      <div class="max-w-7xl mx-auto px-6">

        <h2 class="text-4xl font-bold text-center mb-16">
          {{ t('landing.tools_title') }}
        </h2>

        <div class="grid md:grid-cols-3 gap-10">

          <div class="bg-gray-50 border rounded-2xl p-4 space-y-2">
            <div v-for="(itemKey, i) in ['landing.feature_get_noticed', 'landing.feature_get_hired', 'landing.feature_get_paid', 'landing.feature_get_promoted']"
                 :key="i"
                 @click="activeFeature = i"
                 class="px-4 py-4 rounded-xl cursor-pointer transition"
                 :class="activeFeature === i
              ? 'bg-blue-100 text-blue-600 font-semibold'
              : 'hover:bg-gray-100 text-gray-700'">
                {{ i + 1 }}. {{ t(itemKey) }}
            </div>
          </div>

          <div class="md:col-span-2 grid md:grid-cols-2 gap-6">

            <div class="bg-green-100/60 p-6 rounded-2xl hover:shadow-lg transition">
              <h3 class="font-semibold text-lg mb-2">{{ t('landing.interview_prep') }}</h3>
              <p class="text-sm text-gray-600 mb-4">
                {{ t('landing.interview_desc') }}
              </p>
              <div class="bg-white p-4 rounded-xl shadow text-sm">
                🎤 {{ t('landing.ai_mock') }}
              </div>
            </div>

            <div class="bg-purple-100/60 p-6 rounded-2xl hover:shadow-lg transition">
              <h3 class="font-semibold text-lg mb-2">{{ t('landing.salary_analyzer') }}</h3>
              <p class="text-sm text-gray-600 mb-4">
                {{ t('landing.salary_desc') }}
              </p>
              <div class="bg-white p-4 rounded-xl shadow text-center">
                <p class="text-2xl font-bold text-blue-600">+11.5%</p>
                <p class="text-xs text-gray-500">{{ t('landing.salary_above') }}</p>
              </div>
            </div>

          </div>

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

    <!-- FAQ -->
    <section class="py-28 bg-white">
      <div class="max-w-4xl mx-auto px-6">

        <h2 class="text-4xl font-bold mb-12 text-center">
          {{ t('landing.faq_title') }}
        </h2>

        <div class="divide-y">
          <div v-for="(item, i) in faqs" :key="i" class="py-6">
            <button @click="toggleFAQ(i)"
                    class="w-full flex justify-between text-left">
              <span class="text-lg font-medium">{{ t(item.qKey) }}</span>
              <span>{{ item.open ? '−' : '+' }}</span>
            </button>

            <div v-if="item.open" class="mt-4 text-gray-600 text-sm">
              {{ t(item.aKey) }}
            </div>
          </div>
        </div>

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