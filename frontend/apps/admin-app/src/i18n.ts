import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import vi from './locales/vi.json';

// ============================================================
// SmartCV Admin i18n Configuration
// ============================================================
export const FEATURE_I18N_ENABLED = true;

function getInitialLocale(): string {
    if (!FEATURE_I18N_ENABLED) return 'en';

    const stored = localStorage.getItem('smartcv_locale');
    if (stored && ['en', 'vi'].includes(stored)) return stored;

    const browserLang = navigator.language?.toLowerCase();
    if (browserLang?.startsWith('vi')) return 'vi';

    return 'en';
}

export const i18n = createI18n({
    legacy: false,
    locale: getInitialLocale(),
    fallbackLocale: 'en',
    missingWarn: false,
    fallbackWarn: false,
    messages: { en, vi }
});

export function setLocale(locale: 'en' | 'vi') {
    i18n.global.locale.value = locale;
    localStorage.setItem('smartcv_locale', locale);
    document.documentElement.lang = locale;
}
