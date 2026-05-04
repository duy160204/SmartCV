import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import vi from './locales/vi.json';

// ============================================================
// SmartCV i18n Configuration
// ============================================================
// FEATURE FLAG: Toggle i18n on/off without code changes.
// When false, vue-i18n still works but defaults to 'en',
// effectively acting as a passthrough for existing hardcoded text.
// ============================================================
export const FEATURE_I18N_ENABLED = true;

// Determine initial locale:
// 1. Check localStorage (user preference persisted across sessions)
// 2. Check browser language
// 3. Fallback to 'en'
function getInitialLocale(): string {
    if (!FEATURE_I18N_ENABLED) return 'en';

    const stored = localStorage.getItem('smartcv_locale');
    if (stored && ['en', 'vi'].includes(stored)) return stored;

    const browserLang = navigator.language?.toLowerCase();
    if (browserLang?.startsWith('vi')) return 'vi';

    return 'en';
}

export const i18n = createI18n({
    legacy: false,          // Use Composition API mode
    locale: getInitialLocale(),
    fallbackLocale: 'en',   // If a key is missing in VI, show EN text (never raw key)
    missingWarn: false,      // Don't spam console in production
    fallbackWarn: false,
    messages: { en, vi }
});

// Helper: persist locale change to localStorage
export function setLocale(locale: 'en' | 'vi') {
    i18n.global.locale.value = locale;
    localStorage.setItem('smartcv_locale', locale);
    // Set HTML lang attribute for accessibility & SEO
    document.documentElement.lang = locale;
}

// Helper: get current locale
export function getLocale(): string {
    return i18n.global.locale.value;
}
