import { createContext, useContext, useState, useEffect, useRef } from 'react';
import * as signalR from '@microsoft/signalr';
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
  const [unreadCount, setUnreadCount] = useState(0);
  const [connection, setConnection] = useState(null);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const onMessageReceivedRef = useRef(null);

  const isAuthenticated = !!token;
  const role = user?.role || (token ? parseJwt(token)?.role : null);
  const isAdmin = role === 'Admin';

  useEffect(() => {
    if (isAuthenticated && !connection) {
      const newConnection = new signalR.HubConnectionBuilder()
        .withUrl('/chathub', {
          accessTokenFactory: () => token
        })
        .withAutomaticReconnect()
        .build();

      newConnection.start()
        .then(() => {
          console.log('SignalR Connected');
          setConnection(newConnection);
          
          newConnection.on('ReceiveMessage', (message) => {
            // Call page-specific handler if set
            if (onMessageReceivedRef.current) {
              onMessageReceivedRef.current(message);
            }

            // Global notification logic
            if (activeConversationId !== message.conversationId) {
              setUnreadCount(prev => prev + 1);
            }
          });
        })
        .catch(err => console.error('SignalR Connection Error: ', err));
    }

    return () => {
      if (connection) {
        connection.stop();
        setConnection(null);
      }
    };
  }, [isAuthenticated, token, activeConversationId]);

  useEffect(() => {
    if (token) {
      loadProfile();
    }
  }, []); // Run once on mount

  function saveUserToStorage(userData) {
    try {
      localStorage.setItem('mydogspace_user', JSON.stringify(userData));
    } catch (e) {
      console.warn('LocalStorage quota exceeded, storing minimal user data');
      const minimalUser = { ...userData };
      delete minimalUser.photoUrl;
      try {
        localStorage.setItem('mydogspace_user', JSON.stringify(minimalUser));
      } catch (e2) {
        console.error('Failed to save user even without photo:', e2);
      }
    }
  }

  async function loadProfile() {
    try {
      const res = await usersApi.getProfile();
      const profile = res.data;
      const decoded = parseJwt(token);
      const userData = { ...profile, role: decoded?.role || 'DogOwner' };
      setUser(userData);
      saveUserToStorage(userData);
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
      saveUserToStorage(userData);
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
    if (!user) return;
    const newUser = { ...user, ...updatedData };
    setUser(newUser);
    saveUserToStorage(newUser);
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
        unreadCount,
        setUnreadCount,
        connection,
        activeConversationId,
        setActiveConversationId,
        onMessageReceivedRef
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
