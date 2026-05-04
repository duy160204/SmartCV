<script setup lang="ts">
import { ref, onMounted } from 'vue';
import MainLayout from '@/layouts/MainLayout.vue';
import TemplateSection from '@/components/landing/TemplateSection.vue';
import { useAuthStore } from '@/stores/auth';
import { Star } from 'lucide-vue-next';

const auth = useAuthStore();
const layoutRef = ref<InstanceType<typeof MainLayout> | null>(null);
const templatesRef = ref<HTMLElement | null>(null);

/* HERO */
const showHero = ref(false);
onMounted(() => setTimeout(() => (showHero.value = true), 100));

/* FEATURE */
const activeFeature = ref(0);
const features = ['Get Noticed','Get Hired','Get Paid More','Get Promoted'];

/* FAQ */
const faqs = ref([
  { q: 'What is a resume?', a: 'A resume summarizes your skills and experience.', open: false },
  { q: 'CV vs Resume?', a: 'Resume is shorter, CV is detailed.', open: false },
  { q: 'ATS-friendly?', a: 'Optimized for hiring systems.', open: false },
  { q: 'How long?', a: '1–2 pages.', open: false },
  { q: 'Edit later?', a: 'Yes anytime.', open: false }
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
          Create a <span class="text-blue-600">resume that gets interviews</span><br/>
          and stands out from the crowd
        </h1>

        <p class="text-lg text-gray-600 mt-6 max-w-2xl mx-auto leading-relaxed">
          Build a job-winning resume with professional templates, optimized structure,
          and real-time preview — everything you need to land more interviews faster.
        </p>

        <div class="flex justify-center gap-4 mt-10 flex-wrap">
          <button
              @click="triggerAuth('register')"
              class="px-8 py-4 bg-blue-600 text-white rounded-xl
            hover:bg-blue-700 hover:scale-105 transition shadow-md"
          >
            Get Started
          </button>

          <button
              @click="templatesRef?.scrollIntoView({ behavior: 'smooth' })"
              class="px-8 py-4 border rounded-xl hover:bg-gray-100 transition"
          >
            View Templates
          </button>
        </div>

        <!-- TRUST MINI -->
        <div class="flex justify-center items-center gap-6 mt-8 text-sm text-gray-500 flex-wrap">
          <div class="flex items-center gap-2">
            <div class="flex text-yellow-400">
              <Star v-for="i in 5" :key="i" class="w-4 h-4 fill-yellow-400"/>
            </div>
            <span>4.8/5 rating</span>
          </div>

          <div>✔ No credit card</div>
          <div>✔ 10 min build</div>
          <div>✔ ATS-friendly</div>
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
            Build faster. Apply smarter.
          </h2>
          <p class="text-gray-200 text-sm md:text-base">
            Professional resumes in minutes
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
          Every tool you need is here...
        </h2>

        <div class="grid md:grid-cols-3 gap-10">

          <div class="bg-gray-50 border rounded-2xl p-4 space-y-2">
            <div v-for="(item, i) in features"
                 :key="i"
                 @click="activeFeature = i"
                 class="px-4 py-4 rounded-xl cursor-pointer transition"
                 :class="activeFeature === i
              ? 'bg-blue-100 text-blue-600 font-semibold'
              : 'hover:bg-gray-100 text-gray-700'">
              {{ i + 1 }}. {{ item }}
            </div>
          </div>

          <div class="md:col-span-2 grid md:grid-cols-2 gap-6">

            <div class="bg-green-100/60 p-6 rounded-2xl hover:shadow-lg transition">
              <h3 class="font-semibold text-lg mb-2">Interview Prep</h3>
              <p class="text-sm text-gray-600 mb-4">
                Practice interviews with real questions.
              </p>
              <div class="bg-white p-4 rounded-xl shadow text-sm">
                🎤 AI mock interview
              </div>
            </div>

            <div class="bg-purple-100/60 p-6 rounded-2xl hover:shadow-lg transition">
              <h3 class="font-semibold text-lg mb-2">Salary Analyzer</h3>
              <p class="text-sm text-gray-600 mb-4">
                Know your market value.
              </p>
              <div class="bg-white p-4 rounded-xl shadow text-center">
                <p class="text-2xl font-bold text-blue-600">+11.5%</p>
                <p class="text-xs text-gray-500">above average</p>
              </div>
            </div>

          </div>

        </div>
      </div>
    </section>

    <!-- TRUST -->
    <section class="py-16 bg-gray-50 text-center">
      <p class="text-gray-500 mb-6">Trusted by candidates from</p>
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
          Frequently Asked Questions
        </h2>

        <div class="divide-y">
          <div v-for="(item, i) in faqs" :key="i" class="py-6">
            <button @click="toggleFAQ(i)"
                    class="w-full flex justify-between text-left">
              <span class="text-lg font-medium">{{ item.q }}</span>
              <span>{{ item.open ? '−' : '+' }}</span>
            </button>

            <div v-if="item.open" class="mt-4 text-gray-600 text-sm">
              {{ item.a }}
            </div>
          </div>
        </div>

      </div>
    </section>

    <!-- CTA (FIX KHÔNG BỊ KHỐI CỨNG) -->
    <section class="py-28 text-center bg-gradient-to-b from-white to-blue-600 text-white">
      <div class="max-w-3xl mx-auto">

        <h2 class="text-4xl font-bold mb-6 text-gray-900">
          Create your CV now
        </h2>

        <button
            @click="triggerAuth('register')"
            class="px-10 py-4 bg-blue-600 text-white rounded-xl
          hover:bg-blue-700 hover:scale-105 transition shadow-lg"
        >
          Start Free
        </button>

      </div>
    </section>

  </MainLayout>
</template>