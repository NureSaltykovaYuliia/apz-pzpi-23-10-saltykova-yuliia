import { useTranslation } from 'react-i18next';

export default function Loader() {
  const { t } = useTranslation();
  return (
    <div className="loader-container">
      <div className="loader" role="status" aria-label={t('common.loading')}></div>
    </div>
  );
}
