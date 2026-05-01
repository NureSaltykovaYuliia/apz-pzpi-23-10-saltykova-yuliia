import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { partnersApi } from '../../api/partnersApi';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Loader from '../../components/ui/Loader';

export default function PartnerDetailPage() {
  const { id } = useParams();
  const { t } = useTranslation();
  const [partner, setPartner] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res = await partnersApi.getById(id);
        setPartner(res.data);
      } catch { /* ignore */ }
      setLoading(false);
    }
    load();
  }, [id]);

  if (loading) return <Loader />;
  if (!partner) return (
    <div className="page-container">
      <Link to="/partners" className="flex-gap-sm items-center" style={{ marginBottom: 'var(--space-lg)', fontWeight: 700 }}>
        <span className="material-symbols-outlined">arrow_back</span>
        {t('common.back')}
      </Link>
      <p>{t('common.error')}</p>
    </div>
  );

  return (
    <div className="page-container animate-fade-in" style={{ maxWidth: 900 }}>
      <Link to="/partners" className="flex-gap-sm items-center" style={{ marginBottom: 'var(--space-lg)', fontWeight: 700 }}>
        <span className="material-symbols-outlined">arrow_back</span>
        {t('common.back')}
      </Link>

      <Card style={{ boxShadow: 'var(--shadow-xl)', padding: 0, overflow: 'hidden' }}>
        <div className="flex-row" style={{ flexWrap: 'wrap' }}>
          <div style={{ flex: '1 1 400px', minHeight: 300 }}>
            {partner.photoUrl ? (
              <img 
                src={partner.photoUrl} 
                alt={partner.name}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            ) : (
              <div style={{ 
                width: '100%', 
                height: '100%', 
                backgroundColor: 'var(--color-gray-100)', 
                display: 'flex', 
                flexDirection: 'column',
                alignItems: 'center', 
                justifyContent: 'center',
                color: 'var(--color-gray-400)',
                gap: 'var(--space-md)'
              }}>
                <span className="material-symbols-outlined filled" style={{ fontSize: 64 }}>storefront</span>
                <p>{t('partners.noPhoto') || 'Немає фото'}</p>
              </div>
            )}
          </div>
          
          <div style={{ flex: '1 1 400px', padding: 'var(--space-xl)' }}>
            <h1 className="text-headline-lg" style={{ marginBottom: 'var(--space-md)' }}>{partner.name}</h1>
            <p className="text-body-lg" style={{ marginBottom: 'var(--space-xl)', color: 'var(--color-gray-700)', lineHeight: 1.6 }}>
              {partner.description}
            </p>
            
            <div className="flex-col" style={{ gap: 'var(--space-md)', borderTop: '2px solid var(--color-black)', paddingTop: 'var(--space-xl)' }}>
              <div className="flex-row flex-center" style={{ gap: 'var(--space-md)' }}>
                <div className="icon-circle" style={{ backgroundColor: 'var(--color-yellow-100)' }}>
                  <span className="material-symbols-outlined">location_on</span>
                </div>
                <div>
                  <p className="text-fs-xs" style={{ color: 'var(--color-gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>{t('partners.address')}</p>
                  <p className="text-body-md" style={{ fontWeight: 600 }}>{partner.address}</p>
                </div>
              </div>

              <div className="flex-row flex-center" style={{ gap: 'var(--space-md)' }}>
                <div className="icon-circle" style={{ backgroundColor: 'var(--color-blue-100)' }}>
                  <span className="material-symbols-outlined">phone</span>
                </div>
                <div>
                  <p className="text-fs-xs" style={{ color: 'var(--color-gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>{t('partners.phone')}</p>
                  <p className="text-body-md" style={{ fontWeight: 600 }}>{partner.phoneNumber}</p>
                </div>
              </div>

              {partner.website && (
                <div className="flex-row flex-center" style={{ gap: 'var(--space-md)' }}>
                  <div className="icon-circle" style={{ backgroundColor: 'var(--color-green-100)' }}>
                    <span className="material-symbols-outlined">language</span>
                  </div>
                  <div>
                    <p className="text-fs-xs" style={{ color: 'var(--color-gray-500)', fontWeight: 700, textTransform: 'uppercase' }}>{t('partners.website')}</p>
                    <a href={partner.website} target="_blank" rel="noopener noreferrer" className="text-body-md" style={{ fontWeight: 600, color: 'var(--color-primary)', textDecoration: 'underline' }}>
                      {partner.website.replace(/^https?:\/\//, '')}
                    </a>
                  </div>
                </div>
              )}
            </div>
            
            {partner.website && (
              <div style={{ marginTop: 'var(--space-xl)' }}>
                <Button variant="primary" onClick={() => window.open(partner.website, '_blank')} style={{ width: '100%' }}>
                  {t('partners.visitWebsite') || 'Перейти на сайт'}
                </Button>
              </div>
            )}
          </div>
        </div>
      </Card>
    </div>
  );
}
