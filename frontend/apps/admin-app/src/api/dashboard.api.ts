import api from './axios';

export const dashboardApi = {
  getOverview: async () => {
    const res = await api.get('/admin/dashboard');
    return res.data;
  },
  
  getRevenueTrend: async () => {
    const res = await api.get('/admin/analytics/payments/revenue-trend');
    return res.data;
  },
  
  getRecentPayments: async () => {
    const res = await api.get('/admin/analytics/payments/recent');
    return res.data;
  },
  
  getSubscriptionDistribution: async () => {
    const res = await api.get('/admin/analytics/subscriptions/distribution');
    return res.data;
  },
  
  getRecentSubscriptions: async () => {
    const res = await api.get('/admin/analytics/subscriptions/recent');
    return res.data;
  }
};
