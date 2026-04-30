import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';

export default function LoginPage() {
  const { t } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const user = await login(form);
      navigate(user.role === 'Admin' ? '/admin' : '/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || t('auth.error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page" style={{ minHeight: 'calc(100vh - 80px)' }}>
      <div className="card" style={{ maxWidth: 400, width: '100%', padding: 'var(--space-md)', boxShadow: 'var(--shadow-lg)' }}>
        <h1 className="text-headline-md" style={{ marginBottom: 'var(--space-md)', textAlign: 'center' }}>
          {t('auth.loginTitle')}
        </h1>

        {error && <div className="alert alert-error" style={{ marginBottom: 'var(--space-md)' }}>{error}</div>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-xs)' }}>
          <Input
            label={t('auth.email')}
            name="email"
            type="email"
            value={form.email}
            onChange={handleChange}
            required
            placeholder="email@example.com"
          />
          <Input
            label={t('auth.password')}
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            required
            placeholder="••••••••"
          />
          <Button variant="danger" size="md" type="submit" disabled={loading} style={{ width: '100%', marginTop: 'var(--space-xs)' }}>
            {loading ? t('common.loading') : t('auth.loginButton')}
          </Button>
        </form>

        <p style={{ textAlign: 'center', marginTop: 'var(--space-xs)', fontWeight: 500, fontSize: 'var(--fs-sm)' }}>
          {t('auth.noAccount')}{' '}
          <Link to="/register" style={{ fontWeight: 700, textDecoration: 'underline' }}>
            {t('auth.registerLink')}
          </Link>
        </p>
      </div>
    </div>
  );
}
