import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { devicesApi } from '../../api/devicesApi';
import Table from '../../components/ui/Table';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Loader from '../../components/ui/Loader';

export default function AdminDevicesPage() {
  const { t } = useTranslation();
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadDevices(); }, []);

  async function loadDevices() {
    setLoading(true);
    try { const res = await devicesApi.getAll(); setDevices(res.data); } catch { /* ignore */ }
    setLoading(false);
  }

  const handleDelete = async (id) => {
    if (!window.confirm(t('admin.deleteConfirm'))) return;
    try { await devicesApi.delete(id); loadDevices(); } catch { /* ignore */ }
  };

  const columns = [
    { key: 'deviceGuid', label: 'GUID' },
    { key: 'dogName', label: t('dogs.name'), render: (r) => r.dog?.name || '—' },
    {
      key: 'batteryLevel', label: t('admin.battery'),
      render: (r) => (
        <Badge variant={r.batteryLevel > 20 ? 'success' : 'danger'}>
          {Math.round(r.batteryLevel)}%
        </Badge>
      ),
    },
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
      <h1 className="admin-page-title">{t('admin.devicesTitle')}</h1>
      <div className="data-section"><Table columns={columns} data={devices} /></div>
    </div>
  );
}
