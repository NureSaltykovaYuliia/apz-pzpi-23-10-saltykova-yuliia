import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';
import { usersApi } from '../api/usersApi';

const AuthContext = createContext(null);

function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('mydogspace_token'));
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('mydogspace_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(false);

  const isAuthenticated = !!token;
  const role = user?.role || (token ? parseJwt(token)?.role : null);
  const isAdmin = role === 'Admin';

  useEffect(() => {
    if (token && !user) {
      loadProfile();
    }
  }, [token]);

  async function loadProfile() {
    try {
      const res = await usersApi.getProfile();
      const profile = res.data;
      const decoded = parseJwt(token);
      const userData = { ...profile, role: decoded?.role || 'DogOwner' };
      setUser(userData);
      localStorage.setItem('mydogspace_user', JSON.stringify(userData));
    } catch {
      logout();
    }
  }

  async function login(credentials) {
    setLoading(true);
    try {
      const res = await authApi.login(credentials);
      const newToken = res.data.token;
      localStorage.setItem('mydogspace_token', newToken);
      setToken(newToken);

      const decoded = parseJwt(newToken);
      const profileRes = await usersApi.getProfile();
      const userData = { ...profileRes.data, role: decoded?.role || 'DogOwner' };
      setUser(userData);
      localStorage.setItem('mydogspace_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  }

  async function register(data) {
    setLoading(true);
    try {
      await authApi.register(data);
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    localStorage.removeItem('mydogspace_token');
    localStorage.removeItem('mydogspace_user');
    setToken(null);
    setUser(null);
  }

  function updateUser(updatedData) {
    const newUser = { ...user, ...updatedData };
    setUser(newUser);
    localStorage.setItem('mydogspace_user', JSON.stringify(newUser));
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        role,
        isAuthenticated,
        isAdmin,
        loading,
        login,
        register,
        logout,
        updateUser,
        loadProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
}
