import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { dogsApi } from '../../api/dogsApi';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Modal from '../../components/ui/Modal';
import Input from '../../components/ui/Input';
import Loader from '../../components/ui/Loader';
import LocaleDate from '../../components/common/LocaleDate';

const emptyDog = { name: '', breed: '', dateOfBirth: '', description: '', photoUrl: '' };

export default function DogsPage() {
  const { t } = useTranslation();
  const [dogs, setDogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingDog, setEditingDog] = useState(null);
  const [form, setForm] = useState(emptyDog);

  useEffect(() => { loadDogs(); }, []);

  async function loadDogs() {
    setLoading(true);
    try {
      const res = await dogsApi.getMyDogs();
      setDogs(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  }

  const openCreate = () => { setEditingDog(null); setForm(emptyDog); setModalOpen(true); };
  const openEdit = (dog) => {
    setEditingDog(dog);
    setForm({
      name: dog.name,
      breed: dog.breed || '',
      dateOfBirth: dog.dateOfBirth?.split('T')[0] || '',
      description: dog.description || '',
      photoUrl: dog.photoUrl || '',
    });
    setModalOpen(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleFileChange = (e) => {
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
      if (editingDog) {
        await dogsApi.updateDog(editingDog.id, form);
      } else {
        await dogsApi.createDog(form);
      }
      setModalOpen(false);
      loadDogs();
    } catch { /* ignore */ }
  };

  const handleDelete = async (id) => {
    if (!window.confirm(t('dogs.deleteConfirm'))) return;
    try {
      await dogsApi.deleteDog(id);
      loadDogs();
    } catch { /* ignore */ }
  };

  if (loading) return <Loader />;

  return (
    <div className="page-container animate-fade-in">
      <div className="flex-between" style={{ marginBottom: 'var(--space-xl)' }}>
        <h1 className="section-title">{t('dogs.title')}</h1>
        <Button variant="success" size="md" onClick={openCreate}>
          <span className="material-symbols-outlined" style={{ fontSize: 18 }}>add</span>
          {t('dogs.addDog')}
        </Button>
      </div>

      {dogs.length === 0 ? (
        <div className="empty-state">
          <span className="material-symbols-outlined filled">pets</span>
          <p>{t('dogs.noDogs')}</p>
          <Button variant="primary" size="lg" onClick={openCreate}>{t('dogs.addDog')}</Button>
        </div>
      ) : (
        <div className="grid-cols-3">
          {dogs.map((dog) => (
            <Card key={dog.id} style={{ boxShadow: 'var(--shadow-lg)' }}>
              {dog.photoUrl && (
                <img
                  src={dog.photoUrl}
                  alt={dog.name}
                  style={{ width: '100%', height: 200, objectFit: 'cover', border: '2px solid var(--color-black)', marginBottom: 'var(--space-md)' }}
                />
              )}
              <h3 className="text-headline-md" style={{ marginBottom: 'var(--space-sm)' }}>
                <Link to={`/dogs/${dog.id}`}>{dog.name}</Link>
              </h3>
              <p style={{ color: 'var(--color-gray-500)', marginBottom: 'var(--space-xs)' }}>
                {dog.breed || '—'}
              </p>
              <p style={{ fontSize: 'var(--fs-sm)', marginBottom: 'var(--space-md)' }}>
                <LocaleDate date={dog.dateOfBirth} />
              </p>
              <div className="flex-gap-sm">
                <Button variant="outline" size="sm" onClick={() => openEdit(dog)}>{t('dogs.edit')}</Button>
                <Button variant="danger" size="sm" onClick={() => handleDelete(dog.id)}>{t('dogs.delete')}</Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingDog ? t('dogs.edit') : t('dogs.addDog')}
        footer={
          <>
            <Button variant="outline" size="sm" onClick={() => setModalOpen(false)}>{t('dogs.cancel')}</Button>
            <Button variant="primary" size="sm" onClick={handleSave}>{t('dogs.save')}</Button>
          </>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-sm)' }}>
          <Input label={t('dogs.name')} name="name" value={form.name} onChange={handleChange} required />
          <Input label={t('dogs.breed')} name="breed" value={form.breed} onChange={handleChange} />
          <Input label={t('dogs.dateOfBirth')} name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={handleChange} />
          <Input label={t('dogs.description')} name="description" type="textarea" value={form.description} onChange={handleChange} />
          
          <div className="photo-upload-section">
            <Input 
              label={t('dogs.photo')} 
              name="photoFile" 
              type="file" 
              onChange={handleFileChange} 
              accept="image/*"
            />
            {form.photoUrl && (
              <div style={{ marginTop: '10px', textAlign: 'center' }}>
                <img 
                  src={form.photoUrl} 
                  alt="Preview" 
                  style={{ maxWidth: '100%', maxHeight: '150px', border: '1px solid var(--color-black)', borderRadius: '4px' }} 
                />
              </div>
            )}
          </div>
        </div>
      </Modal>
    </div>
  );
}
