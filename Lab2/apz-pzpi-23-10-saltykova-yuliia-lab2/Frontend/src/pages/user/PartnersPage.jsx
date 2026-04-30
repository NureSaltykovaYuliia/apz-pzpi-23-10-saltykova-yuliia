import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { partnersApi } from '../../api/partnersApi';
import Card from '../../components/ui/Card';
import Loader from '../../components/ui/Loader';

export default function PartnersPage() {
  const { t } = useTranslation();
  const [partners, setPartners] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res = await partnersApi.getAll();
        setPartners(res.data);
      } catch { /* ignore */ }
      setLoading(false);
    }
    load();
  }, []);

  if (loading) return <Loader />;

  return (
    <div className="page-container animate-fade-in">
      <h1 className="section-title" style={{ marginBottom: 'var(--space-xl)' }}>{t('partners.title')}</h1>

      {partners.length === 0 ? (
        <div className="empty-state">
          <span className="material-symbols-outlined filled">handshake</span>
          <p>{t('partners.noPartners')}</p>
        </div>
      ) : (
        <div className="grid-cols-3">
          {partners.map((p) => (
            <Card key={p.id} style={{ boxShadow: 'var(--shadow-lg)' }}>
              <h3 className="text-headline-md" style={{ marginBottom: 'var(--space-sm)' }}>{p.name}</h3>
              <p className="text-body-md" style={{ marginBottom: 'var(--space-md)' }}>{p.description}</p>
              <div className="flex-col" style={{ gap: 'var(--space-xs)', fontSize: 'var(--fs-sm)', color: 'var(--color-gray-500)' }}>
                <p><span className="material-symbols-outlined" style={{ fontSize: 16, marginRight: 4 }}>location_on</span>{p.address}</p>
                <p><span className="material-symbols-outlined" style={{ fontSize: 16, marginRight: 4 }}>phone</span>{p.phoneNumber}</p>
                {p.website && (
                  <p>
                    <span className="material-symbols-outlined" style={{ fontSize: 16, marginRight: 4 }}>language</span>
                    <a href={p.website} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'underline' }}>{p.website}</a>
                  </p>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
