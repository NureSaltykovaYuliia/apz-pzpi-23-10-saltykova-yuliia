import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { partnersApi } from '../../api/partnersApi';
import Table from '../../components/ui/Table';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Input from '../../components/ui/Input';
import Loader from '../../components/ui/Loader';

const emptyPartner = { name: '', description: '', address: '', phoneNumber: '', website: '', photoUrl: '', serviceType: 0 };

export default function AdminPartnersPage() {
  const { t } = useTranslation();
  const [partners, setPartners] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(emptyPartner);

  useEffect(() => { loadPartners(); }, []);

  async function loadPartners() {
    setLoading(true);
    try { const res = await partnersApi.getAll(); setPartners(res.data); } catch { /* ignore */ }
    setLoading(false);
  }

  const openCreate = () => { setEditing(null); setForm(emptyPartner); setModalOpen(true); };
  const openEdit = (p) => {
    setEditing(p);
    setForm({ 
      name: p.name, 
      description: p.description || '', 
      address: p.address || '', 
      phoneNumber: p.phoneNumber || '', 
      website: p.website || '', 
      photoUrl: p.photoUrl || '',
      serviceType: p.serviceType || 0 
    });
    setModalOpen(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handlePhotoChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setForm({ ...form, photoUrl: reader.result });
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSave = async () => {
    try {
      if (editing) await partnersApi.update(editing.id, form);
      else await partnersApi.create(form);
      setModalOpen(false);
      loadPartners();
    } catch { /* ignore */ }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('admin.deleteConfirm'))) return;
    try { await partnersApi.delete(id); loadPartners(); } catch { /* ignore */ }
  };

  const columns = [
    { key: 'name', label: t('partners.name') },
    { key: 'address', label: t('partners.address') },
    { key: 'phoneNumber', label: t('partners.phone') },
    {
      key: 'actions', label: t('admin.actions'), sortable: false,
      render: (r) => (
        <div className="flex-gap-sm">
          <Button variant="outline" size="sm" onClick={() => openEdit(r)}>{t('admin.edit')}</Button>
          <Button variant="danger" size="sm" onClick={() => handleDelete(r.id)}>{t('admin.delete')}</Button>
        </div>
      ),
    },
  ];

  if (loading) return <Loader />;

  return (
    <div className="animate-fade-in">
      <div className="flex-between" style={{ marginBottom: 'var(--space-xl)' }}>
        <h1 className="admin-page-title">{t('admin.partnersTitle')}</h1>
        <Button variant="success" size="md" onClick={openCreate}>
          <span className="material-symbols-outlined" style={{ fontSize: 18 }}>add</span>
          {t('admin.addPartner')}
        </Button>
      </div>
      <div className="data-section"><Table columns={columns} data={partners} /></div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? t('admin.editPartner') : t('admin.addPartner')}
        footer={<><Button variant="outline" size="sm" onClick={() => setModalOpen(false)}>{t('admin.cancel')}</Button>
          <Button variant="primary" size="sm" onClick={handleSave}>{t('admin.save')}</Button></>}
      >
        <div className="flex-center" style={{ marginBottom: 'var(--space-md)' }}>
           <div className="photo-upload-container brutal-border" style={{ 
             width: '100%', 
             height: 200, 
             background: 'var(--color-gray-100)', 
             overflow: 'hidden',
             position: 'relative',
             cursor: 'pointer'
           }}>
             {form.photoUrl ? (
               <img src={form.photoUrl} alt="Partner" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
             ) : (
               <div className="flex-col flex-center h-full" style={{ color: 'var(--color-gray-400)' }}>
                 <span className="material-symbols-outlined" style={{ fontSize: 48 }}>storefront</span>
                 <span style={{ fontSize: 12 }}>{t('partners.addPhoto')}</span>
               </div>
             )}
             <input 
               type="file" 
               accept="image/*" 
               onChange={handlePhotoChange}
               style={{ position: 'absolute', inset: 0, opacity: 0, cursor: 'pointer' }}
             />
           </div>
        </div>

        <Input label={t('partners.name')} name="name" value={form.name} onChange={handleChange} required />
        <Input label={t('partners.description')} name="description" type="textarea" value={form.description} onChange={handleChange} />
        <Input label={t('partners.address')} name="address" value={form.address} onChange={handleChange} />
        <Input label={t('partners.phone')} name="phoneNumber" value={form.phoneNumber} onChange={handleChange} />
        <Input label={t('partners.website')} name="website" value={form.website} onChange={handleChange} />
      </Modal>
    </div>
  );
}
