import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { usersApi } from '../../api/usersApi';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import Card from '../../components/ui/Card';
import Loader from '../../components/ui/Loader';

export default function FriendsPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('my'); // 'my' or 'search'
  const [query, setQuery] = useState('');
  const [users, setUsers] = useState([]);
  const [friends, setFriends] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const loadFriends = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await usersApi.getFriends();
      setFriends(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Error loading friends");
    }
    setLoading(false);
  };

  const handleSearch = async () => {
    setLoading(true);
    setError(null);
    try {
      const params = { query };
      const res = await usersApi.searchUsers(params);
      setUsers(res.data);
    } catch (err) {
      setError(err.response?.data?.message || "Error searching users");
    }
    setLoading(false);
  };

  useEffect(() => {
    if (activeTab === 'my') {
      loadFriends();
    } else {
      handleSearch();
    }
  }, [activeTab]);

  const handleFriendAction = async (userId, isFriend) => {
    try {
      if (isFriend) {
        await usersApi.removeFriend(userId);
      } else {
        await usersApi.addFriend(userId);
      }
      // Refresh both lists to be safe
      if (activeTab === 'my') loadFriends();
      else handleSearch();
    } catch (err) {
      alert(err.response?.data?.message || "Action failed");
    }
  };

  const UserCard = ({ u }) => (
    <Card key={u.id} style={{ boxShadow: 'var(--shadow-lg)', padding: 0, overflow: 'hidden' }}>
      <div style={{ width: '100%', height: 160, background: 'var(--color-gray-100)', display: 'flex', alignItems: 'center', justifyContent: 'center', borderBottom: '2px solid var(--color-black)' }}>
        {u.photoUrl ? (
          <img src={u.photoUrl} alt={u.username} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        ) : (
          <span className="material-symbols-outlined" style={{ fontSize: 64, color: 'var(--color-gray-400)' }}>person</span>
        )}
      </div>
      <div style={{ padding: 'var(--space-md)' }}>
        <h3 className="text-headline-sm">{u.username}</h3>
        <p className="text-body-sm" style={{ margin: 'var(--space-sm) 0', minHeight: 40, color: 'var(--color-gray-600)' }}>
          {u.bio || t('search.noBio')}
        </p>
        <div className="flex-between items-center">
          <span className="badge badge-outline" style={{ fontSize: 11, padding: '4px 10px', background: 'var(--color-white)', fontWeight: 'bold' }}>
            {u.role === 1 || u.role === 'Admin' ? t('search.roleAdmin') : t('search.roleUser')}
          </span>
          <div className="flex-gap-sm">
            <Button 
              variant={u.isFriend || activeTab === 'my' ? "danger" : "success"} 
              size="sm"
              onClick={() => handleFriendAction(u.id, u.isFriend || activeTab === 'my')}
              title={u.isFriend || activeTab === 'my' ? t('search.removeFriend') : t('search.addFriend')}
            >
              <span className="material-symbols-outlined" style={{ fontSize: 16 }}>
                {u.isFriend || activeTab === 'my' ? 'person_remove' : 'person_add'}
              </span>
            </Button>
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => navigate(`/messages/${u.id}`)}
              title={t('search.message')}
            >
              <span className="material-symbols-outlined" style={{ fontSize: 16 }}>mail</span>
            </Button>
          </div>
        </div>
      </div>
    </Card>
  );

  return (
    <div className="page-container animate-fade-in">
      <h1 className="section-title" style={{ marginBottom: 'var(--space-lg)' }}>{t('friends.title')}</h1>

      <div className="flex-gap-md" style={{ marginBottom: 'var(--space-xl)' }}>
        <Button 
          variant={activeTab === 'my' ? 'dark' : 'outline'} 
          onClick={() => setActiveTab('my')}
          style={{ flex: 1 }}
        >
          {t('friends.myFriends')}
        </Button>
        <Button 
          variant={activeTab === 'search' ? 'dark' : 'outline'} 
          onClick={() => setActiveTab('search')}
          style={{ flex: 1 }}
        >
          {t('friends.findFriends')}
        </Button>
      </div>

      {activeTab === 'search' && (
        <div className="card brutal-border" style={{ padding: 'var(--space-lg)', marginBottom: 'var(--space-xl)', background: 'var(--color-yellow-400)' }}>
          <div className="flex-gap-md items-end">
            <div style={{ flex: 1 }}>
              <Input 
                label={t('search.placeholder')} 
                value={query} 
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
            <Button variant="dark" onClick={() => handleSearch()}>{t('search.button')}</Button>
          </div>
        </div>
      )}
      
      {error && (
        <div className="card brutal-border" style={{ padding: 'var(--space-md)', marginBottom: 'var(--space-md)', background: 'var(--color-red-100)', color: 'var(--color-red-600)', fontWeight: 'bold' }}>
          {error}
        </div>
      )}

      {loading ? <Loader /> : (
        <div className="grid-cols-3">
          {(activeTab === 'my' ? friends : users).map(u => (
            <UserCard key={u.id} u={u} />
          ))}
          {(activeTab === 'my' ? friends : users).length === 0 && (
            <div className="empty-state" style={{ gridColumn: '1 / -1' }}>
              <p>{activeTab === 'my' ? t('messages.noChats') : t('search.noResults')}</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
