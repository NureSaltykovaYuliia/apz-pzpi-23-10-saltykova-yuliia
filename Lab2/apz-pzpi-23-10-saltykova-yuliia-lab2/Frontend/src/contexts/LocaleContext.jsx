import { createContext, useContext, useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';

const LocaleContext = createContext(null);

export function LocaleProvider({ children }) {
  const { i18n } = useTranslation();
  const [locale, setLocale] = useState(i18n.language || 'uk');

  useEffect(() => {
    document.documentElement.lang = locale;
    // Both uk and en are LTR
    document.documentElement.dir = 'ltr';
  }, [locale]);

  function changeLocale(newLocale) {
    setLocale(newLocale);
    i18n.changeLanguage(newLocale);
    localStorage.setItem('mydogspace_lang', newLocale);
  }

  function toggleLocale() {
    changeLocale(locale === 'uk' ? 'en' : 'uk');
  }

  return (
    <LocaleContext.Provider
      value={{
        locale,
        changeLocale,
        toggleLocale,
      }}
    >
      {children}
    </LocaleContext.Provider>
  );
}

export function useLocale() {
  const context = useContext(LocaleContext);
  if (!context) throw new Error('useLocale must be used within LocaleProvider');
  return context;
}
