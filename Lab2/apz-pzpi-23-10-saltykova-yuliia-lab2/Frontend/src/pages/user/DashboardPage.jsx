import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { dogsApi } from '../../api/dogsApi';
import { eventsApi } from '../../api/eventsApi';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Loader from '../../components/ui/Loader';
import LocaleDate from '../../components/common/LocaleDate';

export default function DashboardPage() {
  const { t } = useTranslation();
  const { user } = useAuth();
  const [dogs, setDogs] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [dogsRes, eventsRes] = await Promise.all([
          dogsApi.getMyDogs(),
          eventsApi.getUpcoming(),
        ]);
        setDogs(dogsRes.data);
        setEvents(eventsRes.data?.slice(0, 3) || []);
      } catch { /* ignore */ }
      setLoading(false);
    }
    load();
  }, []);

  if (loading) return <Loader />;

  return (
    <div className="page-container animate-fade-in">
      <h1 className="text-headline-xl" style={{ marginBottom: 'var(--space-xl)' }}>
        {t('dashboard.welcome', { name: user?.username || '' })}
      </h1>

      <div className="grid-cols-2" style={{ marginBottom: 'var(--space-2xl)' }}>
        {/* Dogs section */}
        <Card color="yellow" style={{ boxShadow: 'var(--shadow-lg)' }}>
          <div className="flex-between" style={{ marginBottom: 'var(--space-md)' }}>
            <h2 className="text-headline-md">
              <span className="material-symbols-outlined filled" style={{ fontSize: 28, marginRight: 8 }}>pets</span>
              {t('dashboard.myDogs')}
            </h2>
            <Link to="/dogs"><Button variant="outline" size="sm">{t('dashboard.viewAll')}</Button></Link>
          </div>
          {dogs.length === 0 ? (
            <div>
              <p style={{ marginBottom: 'var(--space-md)' }}>{t('dashboard.noDogs')}</p>
              <Link to="/dogs"><Button variant="dark" size="sm">{t('dashboard.addDog')}</Button></Link>
            </div>
          ) : (
            <div className="flex-col" style={{ gap: 'var(--space-sm)' }}>
              {dogs.slice(0, 3).map((dog) => (
                <Link key={dog.id} to={`/dogs/${dog.id}`} className="card card-flat brutal-border-thin" style={{ padding: 'var(--space-sm) var(--space-md)' }}>
                  <div className="flex-between">
                    <strong>{dog.name}</strong>
                    <span style={{ color: 'var(--color-gray-500)', fontSize: 'var(--fs-sm)' }}>{dog.breed || '—'}</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </Card>

        {/* Events section */}
        <Card color="green" style={{ boxShadow: 'var(--shadow-lg)' }}>
          <div className="flex-between" style={{ marginBottom: 'var(--space-md)' }}>
            <h2 className="text-headline-md">
              <span className="material-symbols-outlined filled" style={{ fontSize: 28, marginRight: 8 }}>event</span>
              {t('dashboard.upcomingEvents')}
            </h2>
            <Link to="/events"><Button variant="outline" size="sm">{t('dashboard.viewAll')}</Button></Link>
          </div>
          {events.length === 0 ? (
            <p>{t('dashboard.noEvents')}</p>
          ) : (
            <div className="flex-col" style={{ gap: 'var(--space-sm)' }}>
              {events.map((ev) => (
                <div key={ev.id} className="card card-flat brutal-border-thin" style={{ padding: 'var(--space-sm) var(--space-md)' }}>
                  <div className="flex-between">
                    <strong>{ev.name}</strong>
                    <LocaleDate date={ev.startTime} showTime />
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
