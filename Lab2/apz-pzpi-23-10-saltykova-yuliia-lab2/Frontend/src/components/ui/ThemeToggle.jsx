import { useTheme } from '../../contexts/ThemeContext';
import { useTranslation } from 'react-i18next';

export default function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();
  const { t } = useTranslation();

  return (
    <button
      className="btn-sm brutal-border-thin brutal-shadow-sm"
      style={{ 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        padding: '6px',
        background: 'var(--color-white)',
        color: 'var(--color-black)'
      }}
      onClick={toggleTheme}
      title={theme === 'light' ? t('theme.switchToDark') : t('theme.switchToLight')}
      aria-label="Toggle theme"
    >
      <span className="material-symbols-outlined" style={{ fontSize: 20 }}>
        {theme === 'light' ? 'dark_mode' : 'light_mode'}
      </span>
    </button>
  );
}
