import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

const adminLinks = [
  { path: '/admin', icon: 'dashboard', labelKey: 'admin.dashboard', exact: true },
  { path: '/admin/users', icon: 'group', labelKey: 'admin.users' },
  { path: '/admin/dogs', icon: 'pets', labelKey: 'admin.dogs' },
  { path: '/admin/events', icon: 'event', labelKey: 'admin.events' },
  { path: '/admin/partners', icon: 'handshake', labelKey: 'admin.partners' },
  { path: '/admin/devices', icon: 'devices', labelKey: 'admin.devices' },
  { path: '/admin/data', icon: 'database', labelKey: 'admin.data' },
];

export default function Sidebar({ isOpen, onClose }) {
  const { t } = useTranslation();

  return (
    <>
      <div className={`sidebar-overlay ${isOpen ? 'open' : ''}`} onClick={onClose} />
      <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <h3>{t('nav.admin')}</h3>
        </div>
        <nav className="sidebar-nav">
          {adminLinks.map((link) => (
            <NavLink
              key={link.path}
              to={link.path}
              end={link.exact}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
              onClick={onClose}
            >
              <span className="material-symbols-outlined filled">{link.icon}</span>
              {t(link.labelKey)}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
