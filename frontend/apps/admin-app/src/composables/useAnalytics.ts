import { ref, computed } from 'vue';
import { dashboardApi } from '@/api/dashboard.api';

export function useAnalytics() {
  const revenueTrend = ref<any[]>([]);
  const recentPayments = ref<any[]>([]);
  const subscriptionDistribution = ref<any>({});
  const recentSubscriptions = ref<any[]>([]);
  
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  const loadAnalytics = async () => {
    try {
      isLoading.value = true;
      error.value = null;
      
      const [trend, payments, distribution, subscriptions] = await Promise.all([
        dashboardApi.getRevenueTrend().catch(() => []),
        dashboardApi.getRecentPayments().catch(() => []),
        dashboardApi.getSubscriptionDistribution().catch(() => ({})),
        dashboardApi.getRecentSubscriptions().catch(() => [])
      ]);
      
      revenueTrend.value = trend;
      recentPayments.value = payments;
      subscriptionDistribution.value = distribution;
      recentSubscriptions.value = subscriptions;
      
    } catch (e: any) {
      console.error("Analytics error", e);
      error.value = e.message || 'Failed to load analytics';
    } finally {
      isLoading.value = false;
    }
  };

  const revenueChartData = computed(() => {
    return {
      labels: revenueTrend.value.map(item => item.date),
      datasets: [
        {
          label: 'Revenue (VND)',
          data: revenueTrend.value.map(item => item.value),
          borderColor: '#4ade80',
          backgroundColor: 'rgba(74, 222, 128, 0.2)',
          tension: 0.4,
          fill: true
        }
      ]
    };
  });

  const subscriptionChartData = computed(() => {
    return {
      labels: ['Free', 'Pro', 'Premium'],
      datasets: [
        {
          data: [
            subscriptionDistribution.value.free || 0,
            subscriptionDistribution.value.pro || 0,
            subscriptionDistribution.value.premium || 0
          ],
          backgroundColor: ['#9ca3af', '#60a5fa', '#a78bfa'],
          hoverOffset: 4
        }
      ]
    };
  });

  return {
    revenueTrend,
    recentPayments,
    subscriptionDistribution,
    recentSubscriptions,
    isLoading,
    error,
    loadAnalytics,
    revenueChartData,
    subscriptionChartData
  };
}
