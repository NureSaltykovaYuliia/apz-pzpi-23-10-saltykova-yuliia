import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';

export default function Footer() {
  const { t } = useTranslation();

  return (
    <footer className="footer">
      <div className="footer-inner">
        <div className="footer-brand">
          <div>MYDOGSPACE</div>
          <p className="footer-copyright">{t('footer.copyright')}</p>
        </div>
        <div className="footer-links">
          <Link to="/" className="footer-link">{t('footer.about')}</Link>
          <Link to="/" className="footer-link">{t('footer.privacy')}</Link>
          <Link to="/" className="footer-link">{t('footer.terms')}</Link>
          <Link to="/" className="footer-link">{t('footer.support')}</Link>
          <Link to="/" className="footer-link">{t('footer.contact')}</Link>
        </div>
      </div>
    </footer>
  );
}
