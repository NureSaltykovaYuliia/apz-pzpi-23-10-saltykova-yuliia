import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { LocaleProvider } from './contexts/LocaleContext';
import { ThemeProvider } from './contexts/ThemeContext';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import ProtectedRoute from './components/common/ProtectedRoute';
import './index.css';
import './styles/components.css';

// Public
import LandingPage from './pages/public/LandingPage';
import LoginPage from './pages/public/LoginPage';
import RegisterPage from './pages/public/RegisterPage';

// User
import DashboardPage from './pages/user/DashboardPage';
import ProfilePage from './pages/user/ProfilePage';
import DogsPage from './pages/user/DogsPage';
import DogDetailPage from './pages/user/DogDetailPage';
import EventsPage from './pages/user/EventsPage';
import PartnersPage from './pages/user/PartnersPage';

// Admin
import AdminLayout from './pages/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUsersPage from './pages/admin/AdminUsersPage';
import AdminDogsPage from './pages/admin/AdminDogsPage';
import AdminEventsPage from './pages/admin/AdminEventsPage';
import AdminPartnersPage from './pages/admin/AdminPartnersPage';
import AdminDevicesPage from './pages/admin/AdminDevicesPage';
import AdminDataPage from './pages/admin/AdminDataPage';

function AppContent() {
  const location = useLocation();
  const isAuthPage = location.pathname === '/login' || location.pathname === '/register';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Navbar />
      <main style={{ flex: 1 }}>
        <Routes>
          {/* Public */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* User (authenticated) */}
          <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
          <Route path="/dogs" element={<ProtectedRoute><DogsPage /></ProtectedRoute>} />
          <Route path="/dogs/:id" element={<ProtectedRoute><DogDetailPage /></ProtectedRoute>} />
          <Route path="/events" element={<ProtectedRoute><EventsPage /></ProtectedRoute>} />
          <Route path="/partners" element={<ProtectedRoute><PartnersPage /></ProtectedRoute>} />

          {/* Admin */}
          <Route
            path="/admin"
            element={<ProtectedRoute requiredRole="Admin"><AdminLayout /></ProtectedRoute>}
          >
            <Route index element={<AdminDashboard />} />
            <Route path="users" element={<AdminUsersPage />} />
            <Route path="dogs" element={<AdminDogsPage />} />
            <Route path="events" element={<AdminEventsPage />} />
            <Route path="partners" element={<AdminPartnersPage />} />
            <Route path="devices" element={<AdminDevicesPage />} />
            <Route path="data" element={<AdminDataPage />} />
          </Route>
        </Routes>
      </main>
      {!isAuthPage && <Footer />}
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <LocaleProvider>
          <ThemeProvider>
            <AppContent />
          </ThemeProvider>
        </LocaleProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
