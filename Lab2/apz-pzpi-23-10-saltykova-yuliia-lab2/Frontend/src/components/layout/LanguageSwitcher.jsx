import { useLocale } from '../../contexts/LocaleContext';

export default function LanguageSwitcher() {
  const { locale, changeLocale } = useLocale();

  return (
    <div className="lang-switcher">
      <button
        className={`lang-btn ${locale === 'uk' ? 'active' : ''}`}
        onClick={() => changeLocale('uk')}
        aria-label="Українська"
      >
        UA
      </button>
      <button
        className={`lang-btn ${locale === 'en' ? 'active' : ''}`}
        onClick={() => changeLocale('en')}
        aria-label="English"
      >
        EN
      </button>
    </div>
  );
}
