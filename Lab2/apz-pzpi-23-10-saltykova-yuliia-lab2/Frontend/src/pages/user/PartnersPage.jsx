import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { partnersApi } from '../../api/partnersApi';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import Card from '../../components/ui/Card';
import Loader from '../../components/ui/Loader';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import LocationPicker from '../../components/ui/LocationPicker';

const emptyPartner = { name: '', description: '', address: '', website: '', photoUrl: '', latitude: 49.9935, longitude: 36.2304 };

export default function PartnersPage() {
  const { t } = useTranslation();
  const { isAdmin } = useAuth();
  const navigate = useNavigate();
  const [partners, setPartners] = useState([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyPartner);
  const [isSubmitting, setIsSubmitting] = useState(false);

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

  const handleCreate = async () => {
    setIsSubmitting(true);
    try {
      await partnersApi.create(form);
      setModalOpen(false);
      setForm(emptyPartner);
      loadPartners();
    } catch (err) {
      alert(err.response?.data?.message || 'Error creating partner');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (!window.confirm(t('common.confirmDelete') || 'Are you sure?')) return;
    try {
      await partnersApi.delete(id);
      loadPartners();
    } catch (err) {
      alert(err.response?.data?.message || 'Error deleting partner');
    }
  };

  const openWebsite = (e, url) => {
    e.stopPropagation();
    if (!url) return;
    const webUrl = url.startsWith('http') ? url : `https://${url}`;
    window.open(webUrl, '_blank');
  };

  if (loading) return <Loader />;

  return (
    <div className="page-container animate-fade-in">
      <div className="flex-row flex-between flex-center" style={{ marginBottom: 'var(--space-xl)', gap: 'var(--space-md)', flexWrap: 'wrap' }}>
        <h1 className="section-title" style={{ marginBottom: 0 }}>{t('partners.title')}</h1>
        
        <div className="flex-row flex-center" style={{ gap: 'var(--space-md)', flex: '1', maxWidth: '800px' }}>
          <form onSubmit={handleSearch} className="flex-row flex-center" style={{ gap: 'var(--space-sm)', flex: 1 }}>
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

          {isAdmin && (
            <Button variant="success" onClick={() => setModalOpen(true)}>
              <span className="material-symbols-outlined" style={{ fontSize: 18 }}>add</span>
              {t('common.add') || 'Add'}
            </Button>
          )}
        </div>
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
              style={{ boxShadow: 'var(--shadow-lg)', cursor: 'pointer', transition: 'transform 0.2s', padding: 0, position: 'relative', overflow: 'hidden' }}
              onClick={() => navigate(`/partners/${p.id}`)}
              className="hover-scale"
            >
              {isAdmin && (
                <button 
                  onClick={(e) => handleDelete(e, p.id)}
                  style={{
                    position: 'absolute', top: 8, right: 8, zIndex: 10,
                    background: 'var(--color-primary)', color: 'white', border: '2px solid black',
                    borderRadius: '4px', width: 28, height: 28, display: 'flex', alignItems: 'center', justifyContent: 'center',
                    cursor: 'pointer', boxShadow: '2px 2px 0px black'
                  }}
                >
                  <span className="material-symbols-outlined" style={{ fontSize: 16 }}>close</span>
                </button>
              )}

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
                
                <div className="flex-col" style={{ gap: 'var(--space-sm)' }}>
                  <p className="flex-row items-center" style={{ gap: 4, fontSize: 'var(--fs-xs)', color: 'var(--color-gray-500)' }}>
                    <span className="material-symbols-outlined" style={{ fontSize: 14 }}>location_on</span>
                    {p.address}
                  </p>
                  
                  <Button variant="outline" size="sm" fullWidth onClick={(e) => openWebsite(e, p.website)}>
                    {t('partners.contact') || 'Contact'}
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={t('partners.create') || 'New Partner'}
        footer={
          <>
            <Button variant="outline" size="sm" onClick={() => setModalOpen(false)}>{t('common.cancel')}</Button>
            <Button variant="primary" size="sm" onClick={handleCreate} loading={isSubmitting}>{t('common.save')}</Button>
          </>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-sm)' }}>
          <Input label={t('partners.name')} value={form.name} onChange={e => setForm({...form, name: e.target.value})} required />
          <Input label={t('partners.description')} type="textarea" value={form.description} onChange={e => setForm({...form, description: e.target.value})} />
          <Input label={t('partners.address')} value={form.address} onChange={e => setForm({...form, address: e.target.value})} required />
          <Input label={t('partners.website')} value={form.website} onChange={e => setForm({...form, website: e.target.value})} placeholder="https://..." />
          <div className="form-group">
            <label className="text-caption" style={{ marginBottom: 4, display: 'block' }}>Location</label>
            <LocationPicker 
              lat={form.latitude} 
              lng={form.longitude} 
              onChange={(lat, lng) => setForm({ ...form, latitude: lat, longitude: lng })} 
            />
            <div className="grid-cols-2 gap-sm" style={{ marginTop: 'var(--space-sm)' }}>
              <Input label="Latitude" type="number" value={form.latitude} readOnly />
              <Input label="Longitude" type="number" value={form.longitude} readOnly />
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
}
