import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

export default function RegisterPage() {
  const { t } = useTranslation();
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', bio: '', adminCode: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await register(form);
      setSuccess(t('auth.registerSuccess'));
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setError(err.response?.data?.message || t('auth.error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page" style={{ minHeight: 'calc(100vh - 80px)' }}>
      <div className="card" style={{ maxWidth: 600, width: '100%', padding: 'var(--space-md)', boxShadow: 'var(--shadow-lg)' }}>
        <h1 className="text-headline-md" style={{ marginBottom: 'var(--space-md)', textAlign: 'center' }}>
          {t('auth.registerTitle')}
        </h1>

        {error && <div className="alert alert-error" style={{ marginBottom: 'var(--space-md)' }}>{error}</div>}
        {success && <div className="alert alert-success" style={{ marginBottom: 'var(--space-md)' }}>{success}</div>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-xs)' }}>
          <div className="form-grid" style={{ gap: 'var(--space-sm)' }}>
            <Input label={t('auth.username')} name="username" value={form.username} onChange={handleChange} required />
            <Input label={t('auth.email')} name="email" type="email" value={form.email} onChange={handleChange} required />
          </div>
          <div className="form-grid" style={{ gap: 'var(--space-sm)' }}>
            <Input label={t('auth.password')} name="password" type="password" value={form.password} onChange={handleChange} required />
            <Input label={t('auth.adminCode')} name="adminCode" value={form.adminCode} onChange={handleChange} />
          </div>
          <Input label={t('auth.bio')} name="bio" type="textarea" value={form.bio} onChange={handleChange} rows={1} style={{ minHeight: '60px' }} />

          <Button variant="success" size="md" type="submit" disabled={loading} style={{ width: '100%', marginTop: 'var(--space-xs)' }}>
            {loading ? t('common.loading') : t('auth.registerButton')}
          </Button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 'var(--space-xs)', fontWeight: 500, fontSize: 'var(--fs-sm)' }}>
          {t('auth.hasAccount')}{' '}
          <Link to="/login" style={{ fontWeight: 700, textDecoration: 'underline' }}>
            {t('auth.loginLink')}
          </Link>
        </p>
      </div>
    </div>
  );
}
