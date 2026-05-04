import { ref } from 'vue';
import { dashboardApi } from '@/api/dashboard.api';

export function useDashboard() {
  const stats = ref<any>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  const loadDashboard = async () => {
    try {
      isLoading.value = true;
      error.value = null;
      stats.value = await dashboardApi.getOverview();
    } catch (e: any) {
      console.error("Dashboard error", e);
      error.value = e.response?.data?.message || e.message || 'Failed to load dashboard';
    } finally {
      isLoading.value = false;
    }
  };

  return {
    stats,
    isLoading,
    error,
    loadDashboard
  };
}
