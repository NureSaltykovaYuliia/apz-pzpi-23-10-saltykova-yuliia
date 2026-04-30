import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { eventsApi } from '../../api/eventsApi';
import Table from '../../components/ui/Table';
import Button from '../../components/ui/Button';
import Loader from '../../components/ui/Loader';
import LocaleDate from '../../components/common/LocaleDate';

export default function AdminEventsPage() {
  const { t } = useTranslation();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadEvents(); }, []);

  async function loadEvents() {
    setLoading(true);
    try {
      const res = await eventsApi.getAll();
      setEvents(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  }

  const handleDelete = async (id) => {
    if (!window.confirm(t('admin.deleteConfirm'))) return;
    try { await eventsApi.delete(id); loadEvents(); } catch { /* ignore */ }
  };

  const columns = [
    { key: 'name', label: t('events.name') },
    { key: 'startTime', label: t('events.startTime'), render: (r) => <LocaleDate date={r.startTime} showTime /> },
    { key: 'endTime', label: t('events.endTime'), render: (r) => <LocaleDate date={r.endTime} showTime /> },
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
      <h1 className="admin-page-title">{t('admin.eventsTitle')}</h1>
      <div className="data-section"><Table columns={columns} data={events} /></div>
    </div>
  );
}
