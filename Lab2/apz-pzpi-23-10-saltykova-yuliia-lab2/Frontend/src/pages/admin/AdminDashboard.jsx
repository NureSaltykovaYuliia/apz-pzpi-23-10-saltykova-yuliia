import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { usersApi } from '../../api/usersApi';
import Loader from '../../components/ui/Loader';

export default function AdminDashboard() {
  const { t } = useTranslation();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res = await usersApi.getStatistics();
        setStats(res.data);
      } catch { /* ignore */ }
      setLoading(false);
    }
    load();
  }, []);

  if (loading) return <Loader />;

  const cards = [
    { key: 'totalUsers', icon: 'group', color: 'stat-card-red', value: stats?.totalUsers || 0 },
    { key: 'totalDogs', icon: 'pets', color: 'stat-card-yellow', value: stats?.totalDogs || 0 },
    { key: 'totalEvents', icon: 'event', color: 'stat-card-green', value: stats?.totalEvents || 0 },
    { key: 'totalPartners', icon: 'handshake', color: 'stat-card-white', value: stats?.totalPartners || 0 },
  ];

  return (
    <div className="animate-fade-in">
      <h1 className="admin-page-title">{t('admin.dashboardTitle')}</h1>

      <div className="stats-grid">
        {cards.map((c) => (
          <div key={c.key} className={`stat-card ${c.color}`}>
            <span className="material-symbols-outlined filled">{c.icon}</span>
            <div className="stat-value">{c.value}</div>
            <div className="stat-label">{t(`admin.${c.key}`)}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
