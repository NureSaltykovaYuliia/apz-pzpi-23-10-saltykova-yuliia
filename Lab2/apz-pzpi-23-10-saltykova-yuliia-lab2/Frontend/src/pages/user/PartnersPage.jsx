import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { partnersApi } from '../../api/partnersApi';
import { useNavigate } from 'react-router-dom';
import Card from '../../components/ui/Card';
import Loader from '../../components/ui/Loader';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

export default function PartnersPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [partners, setPartners] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);

  const loadPartners = async (searchQuery = '') => {
    try {
      let params = { query: searchQuery };
      const res = await partnersApi.search(params);
      setPartners(res.data);
    } catch { /* ignore */ }
    setLoading(false);
    setSearching(false);
  };

  useEffect(() => {
    loadPartners();
  }, []);

  const handleSearch = (e) => {
    e?.preventDefault();
    setSearching(true);
    loadPartners(query);
  };

  if (loading) return <Loader />;

  return (
    <div className="page-container animate-fade-in">
      <div className="flex-row flex-between flex-center" style={{ marginBottom: 'var(--space-xl)', gap: 'var(--space-md)', flexWrap: 'wrap' }}>
        <h1 className="section-title" style={{ marginBottom: 0 }}>{t('partners.title')}</h1>
        
        <form onSubmit={handleSearch} className="flex-row flex-center" style={{ gap: 'var(--space-sm)', flex: '1', maxWidth: '600px' }}>
          <div style={{ flex: 1 }}>
            <Input 
              placeholder={t('partners.searchPlaceholder')}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              style={{ marginBottom: 0 }}
            />
          </div>
          <Button variant="primary" type="submit" disabled={searching}>
            {searching ? t('common.loading') : t('search.button')}
          </Button>
        </form>
      </div>

      {partners.length === 0 ? (
        <div className="empty-state">
          <span className="material-symbols-outlined filled">handshake</span>
          <p>{t('partners.noPartners')}</p>
        </div>
      ) : (
        <div className="grid-cols-3">
          {partners.map((p) => (
            <Card 
              key={p.id} 
              style={{ boxShadow: 'var(--shadow-lg)', cursor: 'pointer', transition: 'transform 0.2s', padding: 0, overflow: 'hidden' }}
              onClick={() => navigate(`/partners/${p.id}`)}
              className="hover-scale"
            >
              <div style={{ height: 180, overflow: 'hidden', borderBottom: '2px solid var(--color-black)' }}>
                {p.photoUrl ? (
                  <img src={p.photoUrl} alt={p.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                ) : (
                  <div style={{ width: '100%', height: '100%', backgroundColor: 'var(--color-gray-100)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-gray-400)' }}>
                    <span className="material-symbols-outlined filled" style={{ fontSize: 48 }}>storefront</span>
                  </div>
                )}
              </div>
              <div style={{ padding: 'var(--space-md)' }}>
                <h3 className="text-headline-sm" style={{ marginBottom: 'var(--space-xs)', color: 'var(--color-primary)' }}>{p.name}</h3>
                <p className="text-body-sm line-clamp-2" style={{ marginBottom: 'var(--space-md)', height: '2.8em', overflow: 'hidden' }}>{p.description}</p>
                <div className="flex-col" style={{ gap: 'var(--space-xs)', fontSize: 'var(--fs-xs)', color: 'var(--color-gray-500)' }}>
                  <p className="flex-row items-center" style={{ gap: 4 }}>
                    <span className="material-symbols-outlined" style={{ fontSize: 14 }}>location_on</span>
                    {p.address}
                  </p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
