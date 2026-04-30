import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { dogsApi } from '../../api/dogsApi';
import { devicesApi } from '../../api/devicesApi';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Loader from '../../components/ui/Loader';
import LocaleDate from '../../components/common/LocaleDate';

export default function DogDetailPage() {
  const { id } = useParams();
  const { t } = useTranslation();
  const [dog, setDog] = useState(null);
  const [device, setDevice] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const dogRes = await dogsApi.getDogById(id);
        setDog(dogRes.data);
        try {
          const devRes = await devicesApi.getByDogId(id);
          setDevice(devRes.data);
        } catch { /* no device */ }
      } catch { /* ignore */ }
      setLoading(false);
    }
    load();
  }, [id]);

  if (loading) return <Loader />;
  if (!dog) return <div className="page-container"><p>{t('common.error')}</p></div>;

  return (
    <div className="page-container animate-fade-in" style={{ maxWidth: 800 }}>
      <Link to="/dogs" className="flex-gap-sm items-center" style={{ marginBottom: 'var(--space-lg)', fontWeight: 700 }}>
        <span className="material-symbols-outlined">arrow_back</span>
        {t('common.back')}
      </Link>

      <div className="grid-cols-2">
        <Card style={{ boxShadow: 'var(--shadow-xl)' }}>
          {dog.photoUrl && (
            <img src={dog.photoUrl} alt={dog.name}
              style={{ width: '100%', height: 300, objectFit: 'cover', border: '2px solid var(--color-black)', marginBottom: 'var(--space-md)' }}
            />
          )}
          <h1 className="text-headline-lg" style={{ marginBottom: 'var(--space-md)' }}>{dog.name}</h1>
          <div className="flex-col" style={{ gap: 'var(--space-sm)' }}>
            <p><strong>{t('dogs.breed')}:</strong> {dog.breed || '—'}</p>
            <p><strong>{t('dogs.dateOfBirth')}:</strong> <LocaleDate date={dog.dateOfBirth} /></p>
            <p><strong>{t('dogs.description')}:</strong> {dog.description || '—'}</p>
          </div>
        </Card>

        <Card color={device ? 'green' : undefined} style={{ boxShadow: 'var(--shadow-xl)' }}>
          <h2 className="text-headline-md" style={{ marginBottom: 'var(--space-lg)' }}>
            <span className="material-symbols-outlined filled" style={{ fontSize: 28, marginRight: 8 }}>devices</span>
            {t('dogs.device')}
          </h2>
          {device ? (
            <div className="flex-col" style={{ gap: 'var(--space-sm)' }}>
              <p><strong>GUID:</strong> {device.deviceGuid}</p>
              <p>
                <strong>{t('dogs.battery')}:</strong>{' '}
                <Badge variant={device.batteryLevel > 20 ? 'success' : 'danger'}>
                  {Math.round(device.batteryLevel)}%
                </Badge>
              </p>
              <p><strong>{t('dogs.distance')}:</strong> {(device.totalDistance / 1000).toFixed(1)} km</p>
              <p><strong>{t('dogs.location')}:</strong> {device.lastLatitude?.toFixed(4)}, {device.lastLongitude?.toFixed(4)}</p>
            </div>
          ) : (
            <p style={{ color: 'var(--color-gray-500)' }}>{t('dogs.noDevice')}</p>
          )}
        </Card>
      </div>
    </div>
  );
}
