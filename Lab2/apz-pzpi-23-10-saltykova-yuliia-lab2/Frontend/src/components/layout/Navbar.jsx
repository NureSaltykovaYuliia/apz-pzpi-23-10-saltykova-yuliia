import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../contexts/AuthContext';
import LanguageSwitcher from './LanguageSwitcher';
import ThemeToggle from '../ui/ThemeToggle';
import Button from '../ui/Button';

export default function Navbar() {
  const { t } = useTranslation();
  const { isAuthenticated, isAdmin, user, logout, unreadCount } = useAuth();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const isActive = (path) => location.pathname === path || location.pathname.startsWith(path + '/');

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        {/* Brand */}
        <Link to="/" className="navbar-brand">MYDOGSPACE</Link>

        {/* Mobile toggle */}
        <button
          className="navbar-mobile-toggle"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          <span className="material-symbols-outlined">
            {mobileOpen ? 'close' : 'menu'}
          </span>
        </button>

        {/* Navigation Links */}
        <div className={`navbar-links ${mobileOpen ? 'open' : ''}`}>
          {isAuthenticated ? (
            <>
              <Link
                to="/dashboard"
                className={`navbar-link ${isActive('/dashboard') ? 'active' : ''}`}
                onClick={() => setMobileOpen(false)}
              >
                {t('nav.dashboard')}
              </Link>
              <Link
                to="/dogs"
                className={`navbar-link ${isActive('/dogs') ? 'active' : ''}`}
                onClick={() => setMobileOpen(false)}
              >
                {t('nav.myDogs')}
              </Link>
              <Link
                to="/events"
                className={`navbar-link ${isActive('/events') ? 'active' : ''}`}
                onClick={() => setMobileOpen(false)}
              >
                {t('nav.events')}
              </Link>
              <Link
                to="/partners"
                className={`navbar-link ${isActive('/partners') ? 'active' : ''}`}
                onClick={() => setMobileOpen(false)}
              >
                {t('nav.partners')}
              </Link>
              <Link
                to="/friends"
                className={`navbar-link ${isActive('/friends') ? 'active' : ''}`}
                onClick={() => setMobileOpen(false)}
              >
                {t('nav.friends')}
              </Link>
              <Link
                to="/messages"
                className={`navbar-link ${isActive('/messages') ? 'active' : ''}`}
                onClick={() => setMobileOpen(false)}
                style={{ position: 'relative' }}
              >
                <span className="material-symbols-outlined" style={{ fontSize: 18, marginRight: 4, verticalAlign: 'middle' }}>chat</span>
                {t('nav.messages')}
                {unreadCount > 0 && (
                  <span style={{
                    position: 'absolute',
                    top: -4,
                    right: -4,
                    backgroundColor: 'var(--danger-color)',
                    color: 'white',
                    borderRadius: '50%',
                    width: 16,
                    height: 16,
                    fontSize: 10,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 'bold',
                    border: '2px solid white'
                  }}>
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </Link>
              {isAdmin && (
                <Link
                  to="/admin"
                  className={`navbar-link ${isActive('/admin') ? 'active' : ''}`}
                  onClick={() => setMobileOpen(false)}
                >
                  {t('nav.admin')}
                </Link>
              )}
            </>
          ) : (
            <>
              <a href="#how-it-works" className="navbar-link" onClick={() => setMobileOpen(false)}>
                {t('nav.services')}
              </a>
              <a href="#tracking" className="navbar-link" onClick={() => setMobileOpen(false)}>
                {t('nav.tracking')}
              </a>
              <a href="#testimonials" className="navbar-link" onClick={() => setMobileOpen(false)}>
                {t('nav.testimonials')}
              </a>
            </>
          )}
        </div>

        {/* Actions */}
        <div className="navbar-actions">
          <ThemeToggle />
          <LanguageSwitcher />
          {isAuthenticated ? (
            <div className="flex-gap-sm items-center">
              <Link to="/profile">
                <Button variant="outline" size="sm">
                  <span className="material-symbols-outlined" style={{ fontSize: 18 }}>person</span>
                  {user?.username || t('nav.profile')}
                </Button>
              </Link>
              <Button variant="danger" size="sm" onClick={logout}>
                {t('nav.logout')}
              </Button>
            </div>
          ) : (
            <div className="flex-gap-sm">
              <Link to="/login">
                <Button variant="danger" size="sm">{t('nav.login')}</Button>
              </Link>
              <Link to="/register">
                <Button variant="success" size="sm">{t('nav.join')}</Button>
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
