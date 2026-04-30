import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { dogsApi } from '../../api/dogsApi';
import Table from '../../components/ui/Table';
import Button from '../../components/ui/Button';
import Loader from '../../components/ui/Loader';
import LocaleDate from '../../components/common/LocaleDate';

export default function AdminDogsPage() {
  const { t } = useTranslation();
  const [dogs, setDogs] = useState([]);
  const [loading, setLoading] = useState(true);

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

  const columns = [
    { key: 'name', label: t('dogs.name') },
    { key: 'breed', label: t('dogs.breed') },
    { key: 'dateOfBirth', label: t('dogs.dateOfBirth'), render: (r) => <LocaleDate date={r.dateOfBirth} /> },
    {
      key: 'actions', label: t('admin.actions'), sortable: false,
      render: (r) => (
        <Button variant="danger" size="sm" onClick={() => handleDelete(r.id)}>{t('admin.delete')}</Button>
      ),
    },
  ];

  if (loading) return <Loader />;

  return (
    <div className="animate-fade-in">
      <h1 className="admin-page-title">{t('admin.dogsTitle')}</h1>
      <div className="data-section"><Table columns={columns} data={dogs} /></div>
    </div>
  );
}
