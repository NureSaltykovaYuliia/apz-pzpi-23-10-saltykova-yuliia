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
  const [form, setForm] = useState({ username: '', email: '', password: '', adminCode: '' });
  const [errors, setErrors] = useState({});
  const [generalError, setGeneralError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const validateField = (name, value) => {
    let error = '';
    if (name === 'email') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!value) error = t('auth.emailRequired') || 'Email is required';
      else if (!emailRegex.test(value)) error = t('auth.emailInvalid') || 'Invalid email format';
    }
    if (name === 'password') {
      if (!value) error = t('auth.passwordRequired') || 'Password is required';
      else if (value.length < 6) error = t('auth.passwordMinLength') || 'Password must be at least 6 characters';
    }
    if (name === 'username' && !value) {
      error = t('auth.usernameRequired') || 'Username is required';
    }
    return error;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
    
    // Clear field error when user types
    if (errors[name]) {
      setErrors({ ...errors, [name]: '' });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setGeneralError('');
    setSuccess('');
    
    // Final validation
    const newErrors = {};
    Object.keys(form).forEach(key => {
      const fieldError = validateField(key, form[key]);
      if (fieldError) newErrors[key] = fieldError;
    });

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      await register(form);
      setSuccess(t('auth.registerSuccess'));
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setGeneralError(err.response?.data?.message || t('auth.error'));
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

        {generalError && <div className="alert alert-error" style={{ marginBottom: 'var(--space-md)' }}>{generalError}</div>}
        {success && <div className="alert alert-success" style={{ marginBottom: 'var(--space-md)' }}>{success}</div>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-xs)' }}>
          <div className="form-grid" style={{ gap: 'var(--space-sm)' }}>
            <Input 
              label={t('auth.username')} 
              name="username" 
              value={form.username} 
              onChange={handleChange} 
              error={errors.username}
              required 
            />
            <Input 
              label={t('auth.email')} 
              name="email" 
              type="email" 
              value={form.email} 
              onChange={handleChange} 
              error={errors.email}
              required 
            />
          </div>
          <div className="form-grid" style={{ gap: 'var(--space-sm)' }}>
            <Input 
              label={t('auth.password')} 
              name="password" 
              type="password" 
              value={form.password} 
              onChange={handleChange} 
              error={errors.password}
              required 
            />
            <Input 
              label={t('auth.adminCode')} 
              name="adminCode" 
              value={form.adminCode} 
              onChange={handleChange} 
            />
          </div>

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
