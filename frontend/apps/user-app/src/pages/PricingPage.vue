<script setup lang="ts">
import { useAuthStore } from '@/stores/auth';
import { useUserPlanStore } from '@/stores/user-plan.store';
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const auth = useAuthStore();
const planStore = useUserPlanStore();
const router = useRouter();

onMounted(() => {
  planStore.fetchPlans();
});

const handlePlanAction = async (plan: any) => {
  if (!auth.isAuthenticated) {
    window.location.href = '/register';
    return;
  }

  try {
    if (plan.price === 0) {
      await planStore.upgradePlan(plan.code, 'VNPAY');
    } else {
      router.push(`/payment-method?planId=${plan.code}`);
    }
  } catch (e) {
    console.error("Upgrade routing failed", e);
  }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 font-sans">

    <!-- HERO -->
    <div class="bg-gradient-to-b from-blue-600 to-blue-500 text-white py-24 px-6 text-center">
      <h1 class="text-4xl md:text-5xl font-extrabold mb-6">
        Simple, transparent pricing
      </h1>

      <p class="text-lg md:text-xl text-blue-100 max-w-2xl mx-auto">
        Choose the plan that fits your career goals. Upgrade anytime.
      </p>

      <!-- TRUST -->
      <div class="flex justify-center items-center gap-6 mt-8 text-sm text-blue-100 flex-wrap">
        <div>⭐ 4.8/5 from 12,000+ users</div>
        <div>✔ No hidden fees</div>
        <div>✔ Cancel anytime</div>
      </div>
    </div>

    <!-- PLANS -->
    <div class="max-w-7xl mx-auto px-6 -mt-16 pb-24">

      <!-- LOADING -->
      <div v-if="planStore.isLoading" class="text-center py-20">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>

      <!-- GRID -->
      <div v-else class="grid md:grid-cols-3 gap-8 items-stretch">

        <div
            v-for="(plan, index) in planStore.plans"
            :key="plan.code"
            class="relative rounded-2xl border transition-all duration-300 flex flex-col"

            :class="[
            index === 1
              ? 'bg-white shadow-2xl scale-105 border-blue-600 z-10'
              : 'bg-white shadow-lg border-gray-100 hover:shadow-xl'
          ]"
        >

          <!-- BADGE -->
          <div v-if="index === 1"
               class="absolute -top-4 left-1/2 -translate-x-1/2
               bg-blue-600 text-white text-xs px-4 py-1 rounded-full shadow">
            Most Popular
          </div>

          <!-- HEADER -->
          <div class="p-8 border-b border-gray-100">
            <h3 class="text-xl font-bold text-gray-800 mb-2">
              {{ plan.name }}
            </h3>

            <div class="flex items-baseline gap-1">
              <span class="text-4xl font-extrabold text-blue-600">
                {{ new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: plan.currency
              }).format(plan.price) }}
              </span>
              <span class="text-gray-500 text-sm">
                / {{ plan.durationMonths }} mo
              </span>
            </div>

            <p class="text-gray-500 mt-4 text-sm">
              {{ plan.description }}
            </p>
          </div>

          <!-- FEATURES -->
          <div class="p-8 flex-1 flex flex-col">
            <ul class="space-y-4 mb-8 flex-1">
              <li
                  v-for="(feature, i) in plan.features"
                  :key="i"
                  class="flex items-center gap-3 text-sm text-gray-700"
              >
                <span class="text-green-500 font-bold">✓</span>
                <span>{{ feature }}</span>
              </li>
            </ul>

            <!-- CTA -->
            <button
                @click="handlePlanAction(plan)"
                class="w-full py-3 rounded-xl font-semibold transition transform active:scale-95 shadow-md"

                :class="[
                index === 1
                  ? 'bg-blue-600 text-white hover:bg-blue-700'
                  : plan.price === 0
                    ? 'bg-gray-200 text-gray-800 hover:bg-gray-300'
                    : 'bg-white border hover:bg-gray-100'
              ]"
            >
              {{
                auth.isAuthenticated
                    ? 'Upgrade Now'
                    : (plan.price === 0
                        ? 'Get Started Free'
                        : 'Choose ' + plan.name)
              }}
            </button>
          </div>

        </div>

      </div>

    </div>

    <!-- BOTTOM TRUST -->
    <div class="text-center text-gray-500 text-sm pb-12">
      Secure payments powered by VNPAY • Trusted by thousands of job seekers
    </div>

  </div>
</template>