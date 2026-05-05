<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import MainLayout from '@/layouts/MainLayout.vue';

const { t } = useI18n();
</script>

<template>
  <MainLayout>
    <div class="min-h-screen bg-gradient-to-b from-white to-gray-50">
      <div class="max-w-4xl mx-auto px-6 py-20">

        <!-- Page Header -->
        <div class="text-center mb-16">
          <h1 class="text-4xl md:text-5xl font-extrabold text-gray-900 mb-4">
            {{ t('about.page_title') }}
          </h1>
          <p class="text-lg text-gray-500 max-w-2xl mx-auto">
            {{ t('about.page_subtitle') }}
          </p>
        </div>

        <!-- SECTION 1: About SmartCV -->
        <section class="mb-16">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center text-lg font-bold">1</div>
            <h2 class="text-2xl font-bold text-gray-800">{{ t('about.about_title') }}</h2>
          </div>
          <div class="bg-white border border-gray-200 rounded-2xl p-8 space-y-4 shadow-sm">
            <p class="text-gray-600 leading-relaxed">{{ t('about.about_p1') }}</p>
            <p class="text-gray-600 leading-relaxed">{{ t('about.about_p2') }}</p>
            <div class="bg-blue-50 border border-blue-100 rounded-xl p-5 mt-4">
              <p class="text-sm font-semibold text-blue-700 mb-2">{{ t('about.about_note_title') }}</p>
              <p class="text-sm text-blue-600">{{ t('about.about_note_body') }}</p>
            </div>
          </div>
        </section>

        <!-- SECTION 2: Terms of Service -->
        <section class="mb-16">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 bg-indigo-100 text-indigo-600 rounded-xl flex items-center justify-center text-lg font-bold">2</div>
            <h2 class="text-2xl font-bold text-gray-800">{{ t('about.terms_title') }}</h2>
          </div>
          <div class="bg-white border border-gray-200 rounded-2xl p-8 shadow-sm">
            <ul class="space-y-4">
              <li v-for="i in 5" :key="i" class="flex items-start gap-3">
                <span class="flex-shrink-0 w-6 h-6 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-xs font-bold mt-0.5">{{ i }}</span>
                <p class="text-gray-600 leading-relaxed">{{ t(`about.terms_${i}`) }}</p>
              </li>
            </ul>
          </div>
        </section>

        <!-- SECTION 3: Privacy Policy -->
        <section class="mb-16">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 bg-green-100 text-green-600 rounded-xl flex items-center justify-center text-lg font-bold">3</div>
            <h2 class="text-2xl font-bold text-gray-800">{{ t('about.privacy_title') }}</h2>
          </div>
          <div class="bg-white border border-gray-200 rounded-2xl p-8 shadow-sm">
            <ul class="space-y-4">
              <li v-for="i in 4" :key="i" class="flex items-start gap-3">
                <span class="flex-shrink-0 w-6 h-6 bg-green-100 text-green-600 rounded-full flex items-center justify-center text-xs font-bold mt-0.5">✓</span>
                <p class="text-gray-600 leading-relaxed">{{ t(`about.privacy_${i}`) }}</p>
              </li>
            </ul>
          </div>
        </section>

        <!-- FAQ Section -->
        <section class="mb-16">
          <div class="flex items-center gap-3 mb-6">
            <div class="w-10 h-10 bg-amber-100 text-amber-600 rounded-xl flex items-center justify-center text-lg font-bold">?</div>
            <h2 class="text-2xl font-bold text-gray-800">{{ t('about.faq_title') }}</h2>
          </div>
          <div class="space-y-4">
            <details
              v-for="i in 9"
              :key="i"
              class="group bg-white border border-gray-200 rounded-xl overflow-hidden shadow-sm"
            >
              <summary class="px-6 py-5 cursor-pointer flex items-center justify-between hover:bg-gray-50 transition font-medium text-gray-800">
                {{ t(`about.faq_q${i}`) }}
                <span class="text-gray-400 group-open:rotate-45 transition-transform text-xl">+</span>
              </summary>
              <div class="px-6 pb-5 text-sm text-gray-600 leading-relaxed border-t border-gray-100 pt-4">
                {{ t(`about.faq_a${i}`) }}
              </div>
            </details>
          </div>
        </section>

        <!-- Footer note -->
        <div class="text-center text-sm text-gray-400 pt-8 border-t border-gray-100">
          {{ t('about.footer_note') }}
        </div>

      </div>
    </div>
  </MainLayout>
</template>
