<script setup lang="ts">
import { onMounted } from 'vue';
import { Line, Pie } from 'vue-chartjs';
import { 
  Chart as ChartJS, Title, Tooltip, Legend, 
  BarElement, CategoryScale, LinearScale,
  LineElement, PointElement, ArcElement, Filler
} from 'chart.js';

import { useDashboard } from '@/composables/useDashboard';
import { useAnalytics } from '@/composables/useAnalytics';
import { useI18n } from 'vue-i18n';

ChartJS.register(
  Title, Tooltip, Legend, 
  BarElement, CategoryScale, LinearScale,
  LineElement, PointElement, ArcElement, Filler
);

const { t } = useI18n();
const { stats, isLoading: isDashboardLoading, error: dashboardError, loadDashboard } = useDashboard();
const { 
  recentPayments, recentSubscriptions, isLoading: isAnalyticsLoading, 
  error: analyticsError, loadAnalytics, revenueChartData, subscriptionChartData 
} = useAnalytics();

const formatCurrency = (val: number) => {
    if (val === undefined || val === null) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
};

const formatDate = (dateString: string) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('vi-VN', {
        year: 'numeric', month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit'
    });
};

const reloadAll = () => {
    loadDashboard();
    loadAnalytics();
};

onMounted(() => {
   reloadAll();
});
</script>

<template>
  <div class="p-8 min-h-screen bg-gray-50">
      
      <div class="flex justify-between items-center mb-8">
          <h1 class="text-3xl font-bold text-gray-800">{{ t('dashboard.title') }}</h1>
          <button @click="reloadAll" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg shadow transition-colors flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M4 2a1 1 0 011 1v2.101a7.002 7.002 0 0111.601 2.566 1 1 0 11-1.885.666A5.002 5.002 0 005.999 7H9a1 1 0 010 2H4a1 1 0 01-1-1V3a1 1 0 011-1zm.008 9.057a1 1 0 011.276.61A5.002 5.002 0 0014.001 13H11a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0v-2.101a7.002 7.002 0 01-11.601-2.566 1 1 0 01.61-1.276z" clip-rule="evenodd" />
              </svg>
              {{ t('common.refresh') }}
          </button>
      </div>

      <!-- Error State -->
      <div v-if="dashboardError || analyticsError" class="mb-8 p-4 bg-red-100 border-l-4 border-red-500 text-red-700 rounded shadow-sm flex items-center justify-between">
          <div>
              <p class="font-bold">{{ t('common.error_title') }}</p>
              <p class="text-sm">{{ dashboardError || analyticsError }}</p>
          </div>
          <button @click="reloadAll" class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition">{{ t('common.retry') }}</button>
      </div>
      
      <!-- KPI Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <!-- Total Users -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 relative overflow-hidden">
              <div v-if="isDashboardLoading" class="absolute inset-0 bg-gray-100 animate-pulse"></div>
              <template v-else>
                  <div class="text-gray-500 text-sm font-medium">{{ t('dashboard.total_users') }}</div>
                  <div class="text-3xl font-bold mt-2">{{ stats?.totalUsers || 0 }}</div>
                  <div class="flex gap-3 text-xs text-gray-500 mt-2">
                      <span class="text-green-600">{{ t('dashboard.verified') }}: {{ stats?.verifiedUsers || 0 }}</span>
                      <span class="text-red-500">{{ t('dashboard.locked') }}: {{ stats?.lockedUsers || 0 }}</span>
                  </div>
                  <div class="text-xs text-blue-600 mt-1 font-medium">+{{ stats?.newUsers7Days || 0 }} {{ t('dashboard.new_7days') }}</div>
              </template>
          </div>

          <!-- Total Revenue -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 relative overflow-hidden">
              <div v-if="isDashboardLoading" class="absolute inset-0 bg-gray-100 animate-pulse"></div>
              <template v-else>
                  <div class="text-gray-500 text-sm font-medium">{{ t('dashboard.total_revenue') }}</div>
                  <div class="text-3xl font-bold text-green-600 mt-2">{{ formatCurrency(stats?.totalRevenue || 0) }}</div>
                  <div class="text-xs text-gray-500 mt-2">{{ t('dashboard.from_paid', { count: stats?.paidUsers || 0 }) }}</div>
              </template>
          </div>

          <!-- CV & Templates -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 relative overflow-hidden">
              <div v-if="isDashboardLoading" class="absolute inset-0 bg-gray-100 animate-pulse"></div>
              <template v-else>
                  <div class="text-gray-500 text-sm font-medium">{{ t('dashboard.active_content') }}</div>
                  <div class="flex justify-between items-end mt-2">
                      <div>
                          <div class="text-2xl font-bold text-blue-600">{{ stats?.totalCVs || 0 }}</div>
                          <div class="text-xs text-gray-500 mt-1 cursor-pointer hover:underline" @click="$router.push('/cv')">{{ t('dashboard.total_cvs') }} →</div>
                      </div>
                      <div class="h-8 w-px bg-gray-200"></div>
                      <div>
                          <div class="text-2xl font-bold text-indigo-600">{{ stats?.totalTemplates || 0 }}</div>
                          <div class="text-xs text-gray-500 mt-1 cursor-pointer hover:underline" @click="$router.push('/templates')">{{ t('dashboard.templates') }} →</div>
                      </div>
                  </div>
              </template>
          </div>

          <!-- Successful Payments -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 relative overflow-hidden">
              <div v-if="isDashboardLoading" class="absolute inset-0 bg-gray-100 animate-pulse"></div>
              <template v-else>
                  <div class="text-gray-500 text-sm font-medium">{{ t('dashboard.success_payments') }}</div>
                  <div class="text-3xl font-bold text-indigo-500 mt-2">{{ stats?.successPayments || 0 }}</div>
                  <div class="text-xs text-gray-500 mt-2">{{ t('dashboard.total_transactions', { count: stats?.totalPayments || 0 }) }}</div>
              </template>
          </div>
      </div>

      <!-- Charts Section -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <!-- Revenue Trend (Line Chart) -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 relative min-h-[350px]">
              <h3 class="font-bold text-gray-800 mb-4">{{ t('dashboard.revenue_trend') }}</h3>
              <div v-if="isAnalyticsLoading" class="absolute inset-0 flex items-center justify-center bg-white/80 z-10">
                  <div class="w-full h-full p-6"><div class="w-full h-full bg-gray-100 animate-pulse rounded-lg"></div></div>
              </div>
              <div v-else-if="!revenueChartData.labels.length" class="absolute inset-0 flex items-center justify-center z-10">
                  <p class="text-gray-400 font-medium bg-gray-50 px-4 py-2 rounded-full">{{ t('common.noData') }}</p>
              </div>
              <div class="h-[300px]" v-show="revenueChartData.labels.length">
                  <Line :data="revenueChartData" :options="{ responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }" />
              </div>
          </div>

          <!-- Subscription Distribution (Pie Chart) -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 relative min-h-[350px]">
              <h3 class="font-bold text-gray-800 mb-4">{{ t('dashboard.subscription_dist') }}</h3>
              <div v-if="isAnalyticsLoading" class="absolute inset-0 flex items-center justify-center bg-white/80 z-10">
                  <div class="w-full h-full p-6 flex justify-center items-center"><div class="w-48 h-48 bg-gray-100 animate-pulse rounded-full"></div></div>
              </div>
              <div v-else-if="!subscriptionChartData.datasets[0].data.some(v => v > 0)" class="absolute inset-0 flex items-center justify-center z-10">
                  <p class="text-gray-400 font-medium bg-gray-50 px-4 py-2 rounded-full">{{ t('common.noData') }}</p>
              </div>
              <div class="h-[300px] flex justify-center" v-show="subscriptionChartData.datasets[0].data.some(v => v > 0)">
                  <Pie :data="subscriptionChartData" :options="{ responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'right' } } }" />
              </div>
          </div>
      </div>

      <!-- Recent Tables Section -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- Recent Payments -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
              <h3 class="font-bold text-gray-800 mb-4">{{ t('dashboard.recent_payments') }}</h3>
              <div v-if="isAnalyticsLoading" class="space-y-4">
                  <div v-for="i in 5" :key="i" class="h-12 bg-gray-100 animate-pulse rounded"></div>
              </div>
              <div v-else-if="!recentPayments.length" class="text-center py-8 text-gray-500">
                  {{ t('dashboard.no_payments') }}
              </div>
              <div v-else class="overflow-x-auto">
                  <table class="min-w-full divide-y divide-gray-200">
                      <thead>
                          <tr>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_amount') }}</th>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_method') }}</th>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_status') }}</th>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_date') }}</th>
                          </tr>
                      </thead>
                      <tbody class="divide-y divide-gray-200">
                          <tr v-for="payment in recentPayments" :key="payment.id" class="hover:bg-gray-50">
                              <td class="px-4 py-3 text-sm font-medium text-green-600">{{ formatCurrency(payment.amount) }}</td>
                              <td class="px-4 py-3 text-sm text-gray-500">
                                  <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                                      {{ payment.provider }}
                                  </span>
                              </td>
                              <td class="px-4 py-3 text-sm">
                                  <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                                      {{ payment.status }}
                                  </span>
                              </td>
                              <td class="px-4 py-3 text-sm text-gray-500">{{ formatDate(payment.createdAt) }}</td>
                          </tr>
                      </tbody>
                  </table>
              </div>
          </div>

          <!-- Recent Subscriptions -->
          <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
              <h3 class="font-bold text-gray-800 mb-4">{{ t('dashboard.recent_subs') }}</h3>
              <div v-if="isAnalyticsLoading" class="space-y-4">
                  <div v-for="i in 5" :key="i" class="h-12 bg-gray-100 animate-pulse rounded"></div>
              </div>
              <div v-else-if="!recentSubscriptions.length" class="text-center py-8 text-gray-500">
                  {{ t('dashboard.no_subs') }}
              </div>
              <div v-else class="overflow-x-auto">
                  <table class="min-w-full divide-y divide-gray-200">
                      <thead>
                          <tr>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_user_id') }}</th>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_upgrade') }}</th>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_reason') }}</th>
                              <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('table.col_date') }}</th>
                          </tr>
                      </thead>
                      <tbody class="divide-y divide-gray-200">
                          <tr v-for="sub in recentSubscriptions" :key="sub.id" class="hover:bg-gray-50">
                              <td class="px-4 py-3 text-sm text-gray-900">#{{ sub.userId }}</td>
                              <td class="px-4 py-3 text-sm">
                                  <div class="flex items-center space-x-2">
                                      <span class="text-gray-500">{{ sub.oldPlan || 'NONE' }}</span>
                                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3" />
                                      </svg>
                                      <span class="font-medium text-indigo-600">{{ sub.newPlan }}</span>
                                  </div>
                              </td>
                              <td class="px-4 py-3 text-sm">
                                  <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">
                                      {{ sub.reason }}
                                  </span>
                              </td>
                              <td class="px-4 py-3 text-sm text-gray-500">{{ formatDate(sub.changedAt) }}</td>
                          </tr>
                      </tbody>
                  </table>
              </div>
          </div>
      </div>
      
  </div>
</template>
