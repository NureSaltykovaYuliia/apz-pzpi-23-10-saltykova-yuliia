import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { dogsApi } from '../../api/dogsApi';
import Table from '../../components/ui/Table';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Loader from '../../components/ui/Loader';
import LocaleDate from '../../components/common/LocaleDate';

export default function AdminDogsPage() {
  const { t } = useTranslation();
  const [dogs, setDogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDog, setSelectedDog] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => { loadDogs(); }, []);

  async function loadDogs() {
    setLoading(true);
    try { const res = await dogsApi.getMyDogs(); setDogs(res.data); } catch { /* ignore */ }
    setLoading(false);
  }

  const handleDelete = async (id) => {
    if (!window.confirm(t('admin.deleteConfirm'))) return;
    try { await dogsApi.deleteDog(id); loadDogs(); } catch { /* ignore */ }
  };

  const openView = (dog) => {
    setSelectedDog(dog);
    setIsModalOpen(true);
  };

  const columns = [
    { key: 'name', label: t('dogs.name') },
    { key: 'breed', label: t('dogs.breed') },
    { key: 'dateOfBirth', label: t('dogs.dateOfBirth'), render: (r) => <LocaleDate date={r.dateOfBirth} /> },
    {
      key: 'actions', label: t('admin.actions'), sortable: false,
      render: (r) => (
        <div className="flex-gap-sm">
          <Button variant="outline" size="sm" onClick={() => openView(r)}>{t('dogs.details')}</Button>
          <Button variant="danger" size="sm" onClick={() => handleDelete(r.id)}>{t('admin.delete')}</Button>
        </div>
      ),
    },
  ];

  if (loading) return <Loader />;

  return (
    <div className="animate-fade-in">
      <h1 className="admin-page-title">{t('admin.dogsTitle')}</h1>
      <div className="data-section"><Table columns={columns} data={dogs} /></div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={selectedDog?.name || t('dogs.details')}
      >
        {selectedDog && (
          <div className="flex-col" style={{ gap: 'var(--space-md)' }}>
            {selectedDog.photoUrl && (
              <img src={selectedDog.photoUrl} alt={selectedDog.name} 
                style={{ width: '100%', height: 250, objectFit: 'cover', border: '2px solid var(--color-black)', borderRadius: 'var(--radius-md)' }} 
              />
            )}
            <div className="flex-col" style={{ gap: 'var(--space-xs)' }}>
              <p><strong>{t('dogs.breed')}:</strong> {selectedDog.breed || '—'}</p>
              <p><strong>{t('dogs.dateOfBirth')}:</strong> <LocaleDate date={selectedDog.dateOfBirth} /></p>
              <p><strong>{t('dogs.description')}:</strong> {selectedDog.description || '—'}</p>
            </div>
            <Button variant="dark" style={{ marginTop: 'var(--space-md)' }} onClick={() => setIsModalOpen(false)}>
              {t('common.close')}
            </Button>
          </div>
        )}
      </Modal>
    </div>
  );
}
