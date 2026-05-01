import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { usersApi } from '../../api/usersApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Loader from '../../components/ui/Loader';

export default function ProfilePage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user, updateUser, logout } = useAuth();
  const [form, setForm] = useState({ username: '', bio: '', photoUrl: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [friends, setFriends] = useState([]);

  useEffect(() => {
    async function load() {
      try {
        const [profileRes, friendsRes] = await Promise.all([
          usersApi.getProfile(),
          usersApi.getFriends()
        ]);
        setForm({ 
          username: profileRes.data.username || '', 
          bio: profileRes.data.bio || '',
          photoUrl: profileRes.data.photoUrl || ''
        });
        setFriends(friendsRes.data);
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

  const handlePhotoChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setForm({ ...form, photoUrl: reader.result });
      };
      reader.readAsDataURL(file);
    }
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
          <div className="flex-center" style={{ marginBottom: 'var(--space-md)' }}>
             <div className="photo-upload-container brutal-border" style={{ 
               width: 150, 
               height: 150, 
               background: 'var(--color-gray-100)', 
               overflow: 'hidden',
               position: 'relative',
               cursor: 'pointer'
             }}>
               {form.photoUrl ? (
                 <img src={form.photoUrl} alt="Profile" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
               ) : (
                 <div className="flex-col flex-center h-full" style={{ color: 'var(--color-gray-400)' }}>
                   <span className="material-symbols-outlined" style={{ fontSize: 48 }}>person</span>
                   <span style={{ fontSize: 10 }}>{t('profile.addPhoto')}</span>
                 </div>
               )}
               <input 
                 type="file" 
                 accept="image/*" 
                 onChange={handlePhotoChange}
                 style={{ position: 'absolute', inset: 0, opacity: 0, cursor: 'pointer' }}
               />
             </div>
          </div>

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

      <div style={{ marginTop: 'var(--space-xl)' }}>
        <h2 className="text-headline-sm" style={{ marginBottom: 'var(--space-md)' }}>
          {t('nav.findFriends')} ({friends.length})
        </h2>
        <div className="flex-col" style={{ gap: 'var(--space-sm)' }}>
          {friends.map(f => (
            <div key={f.id} className="card brutal-border" style={{ padding: 'var(--space-sm)', background: 'var(--color-white)' }}>
              <div className="flex-between items-center">
                <div className="flex-gap-sm items-center">
                  <div className="brutal-border" style={{ width: 40, height: 40, background: 'var(--color-gray-100)', overflow: 'hidden' }}>
                    {f.photoUrl ? <img src={f.photoUrl} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} /> : <span className="material-symbols-outlined" style={{ fontSize: 24, padding: 8 }}>person</span>}
                  </div>
                  <div>
                    <div style={{ fontWeight: 'bold' }}>{f.username}</div>
                    <div style={{ fontSize: 11, color: 'var(--color-gray-500)' }}>{f.bio?.substring(0, 30)}...</div>
                  </div>
                </div>
                <Button variant="outline" size="sm" onClick={() => navigate(`/messages/${f.id}`)}>
                   <span className="material-symbols-outlined" style={{ fontSize: 16 }}>mail</span>
                </Button>
              </div>
            </div>
          ))}
          {friends.length === 0 && (
            <div style={{ padding: 'var(--space-md)', textAlign: 'center', color: 'var(--color-gray-400)', background: 'var(--color-gray-50)', border: '2px dashed var(--color-gray-300)' }}>
              {t('messages.noChats')}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
