import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../contexts/AuthContext';
import { usersApi } from '../../api/usersApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Loader from '../../components/ui/Loader';

export default function ProfilePage() {
  const { t } = useTranslation();
  const { user, updateUser, logout } = useAuth();
  const [form, setForm] = useState({ username: '', bio: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const res = await usersApi.getProfile();
        setForm({ username: res.data.username || '', bio: res.data.bio || '' });
      } catch { /* ignore */ }
      setLoading(false);
    }
    load();
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage('');
    try {
      const res = await usersApi.updateProfile(form);
      updateUser(res.data);
      setMessage(t('profile.updated'));
    } catch (err) {
      setMessage(err.response?.data?.message || t('common.error'));
    }
    setSaving(false);
  };

  const handleDelete = async () => {
    if (!window.confirm(t('profile.deleteConfirm'))) return;
    try {
      await usersApi.deleteProfile();
      logout();
    } catch { /* ignore */ }
  };

  if (loading) return <Loader />;

  return (
    <div className="page-container animate-fade-in" style={{ maxWidth: 600 }}>
      <h1 className="section-title" style={{ marginBottom: 'var(--space-xl)' }}>{t('profile.title')}</h1>

      {message && (
        <div className={`alert ${message.includes('!') ? 'alert-success' : 'alert-error'}`} style={{ marginBottom: 'var(--space-md)' }}>
          {message}
        </div>
      )}

      <form onSubmit={handleSave} className="card" style={{ boxShadow: 'var(--shadow-lg)' }}>
        <div className="flex-col" style={{ gap: 'var(--space-md)' }}>
          <div className="input-group">
            <label>{t('profile.email')}</label>
            <div className="input-field" style={{ background: 'var(--color-gray-100)', cursor: 'not-allowed' }}>
              {user?.email || '—'}
            </div>
          </div>
          <Input label={t('profile.username')} name="username" value={form.username} onChange={handleChange} required />
          <Input label={t('profile.bio')} name="bio" type="textarea" value={form.bio} onChange={handleChange} />

          <div className="form-actions">
            <Button variant="primary" size="md" type="submit" disabled={saving}>
              {saving ? t('common.loading') : t('profile.save')}
            </Button>
            <Button variant="danger" size="md" type="button" onClick={handleDelete}>
              {t('profile.delete')}
            </Button>
          </div>
        </div>
      </form>
    </div>
  );
}
