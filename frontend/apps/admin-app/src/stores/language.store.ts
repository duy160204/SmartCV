import { defineStore } from 'pinia';
import { ref } from 'vue';
import { i18n, FEATURE_I18N_ENABLED } from '@/i18n';
import api from '@/api/axios';

export const useLanguageStore = defineStore('language', () => {
    // Single source of truth initialized from localStorage or browser
    const getInitialLocale = (): string => {
        if (!FEATURE_I18N_ENABLED) return 'en';
        const stored = localStorage.getItem('smartcv_locale');
        if (stored && ['en', 'vi'].includes(stored)) return stored;
        const browserLang = navigator.language?.toLowerCase();
        if (browserLang?.startsWith('vi')) return 'vi';
        return 'en';
    };

    const locale = ref<'en' | 'vi'>(getInitialLocale() as 'en' | 'vi');

    // Sync everywhere
    const setLocale = (newLocale: 'en' | 'vi') => {
        if (!FEATURE_I18N_ENABLED) return;
        
        locale.value = newLocale;
        
        // 1. Persist to localStorage
        localStorage.setItem('smartcv_locale', newLocale);
        
        // 2. Update vue-i18n
        i18n.global.locale.value = newLocale;
        
        // 3. Update document language for SEO
        document.documentElement.lang = newLocale;
        
        // 4. Update Axios interceptor
        api.defaults.headers.common['Accept-Language'] = newLocale;
        
        // Emit custom event if needed globally
        window.dispatchEvent(new CustomEvent('locale_changed', { detail: { locale: newLocale } }));
    };

    // Ensure initial state is fully synced
    if (FEATURE_I18N_ENABLED) {
        setLocale(locale.value);
    }

    return { locale, setLocale };
});
