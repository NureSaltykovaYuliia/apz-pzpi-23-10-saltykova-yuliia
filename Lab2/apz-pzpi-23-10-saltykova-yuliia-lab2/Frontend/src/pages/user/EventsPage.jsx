import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { eventsApi } from '../../api/eventsApi';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Input from '../../components/ui/Input';
import Loader from '../../components/ui/Loader';
import LocaleDate from '../../components/common/LocaleDate';

const emptyEvent = { name: '', description: '', startTime: '', endTime: '', type: 0, latitude: 0, longitude: 0 };

export default function EventsPage() {
  const { t } = useTranslation();
  const [events, setEvents] = useState([]);
  const [filter, setFilter] = useState('upcoming');
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(emptyEvent);

  useEffect(() => { loadEvents(); }, [filter]);

  async function loadEvents() {
    setLoading(true);
    try {
      let res;
      if (filter === 'upcoming') res = await eventsApi.getUpcoming();
      else if (filter === 'my') res = await eventsApi.getMy();
      else res = await eventsApi.getAll();
      setEvents(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  }

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleCreate = async () => {
    try {
      await eventsApi.create(form);
      setModalOpen(false);
      setForm(emptyEvent);
      loadEvents();
    } catch { /* ignore */ }
  };

  return (
    <div className="page-container animate-fade-in">
      <div className="flex-between" style={{ marginBottom: 'var(--space-xl)' }}>
        <h1 className="section-title">{t('events.title')}</h1>
        <Button variant="success" size="md" onClick={() => setModalOpen(true)}>
          <span className="material-symbols-outlined" style={{ fontSize: 18 }}>add</span>
          {t('events.create')}
        </Button>
      </div>

      {/* Filters */}
      <div className="flex-gap-sm" style={{ marginBottom: 'var(--space-lg)' }}>
        {['upcoming', 'all', 'my'].map((f) => (
          <Button key={f} variant={filter === f ? 'primary' : 'outline'} size="sm" onClick={() => setFilter(f)}>
            {t(`events.${f}`)}
          </Button>
        ))}
      </div>

      {loading ? <Loader /> : events.length === 0 ? (
        <div className="empty-state">
          <span className="material-symbols-outlined filled">event</span>
          <p>{t('events.noEvents')}</p>
        </div>
      ) : (
        <div className="grid-cols-3">
          {events.map((ev) => (
            <Card key={ev.id} style={{ boxShadow: 'var(--shadow-lg)' }}>
              <h3 className="text-headline-md" style={{ marginBottom: 'var(--space-sm)' }}>{ev.name}</h3>
              <p style={{ color: 'var(--color-gray-500)', marginBottom: 'var(--space-sm)', fontSize: 'var(--fs-sm)' }}>
                <LocaleDate date={ev.startTime} showTime /> — <LocaleDate date={ev.endTime} showTime />
              </p>
              <p className="text-body-md" style={{ marginBottom: 'var(--space-md)' }}>{ev.description}</p>
              <div className="flex-gap-sm">
                <Button variant="primary" size="sm">{t('events.join')}</Button>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={t('events.create')}
        footer={
          <>
            <Button variant="outline" size="sm" onClick={() => setModalOpen(false)}>{t('events.cancel')}</Button>
            <Button variant="primary" size="sm" onClick={handleCreate}>{t('events.save')}</Button>
          </>
        }
      >
        <Input label={t('events.name')} name="name" value={form.name} onChange={handleChange} required />
        <Input label={t('events.description')} name="description" type="textarea" value={form.description} onChange={handleChange} />
        <Input label={t('events.startTime')} name="startTime" type="datetime-local" value={form.startTime} onChange={handleChange} required />
        <Input label={t('events.endTime')} name="endTime" type="datetime-local" value={form.endTime} onChange={handleChange} required />
      </Modal>
    </div>
  );
}
