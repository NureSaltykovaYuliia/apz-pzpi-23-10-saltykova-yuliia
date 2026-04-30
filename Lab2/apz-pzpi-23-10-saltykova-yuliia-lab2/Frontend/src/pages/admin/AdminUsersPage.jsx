import { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { usersApi } from '../../api/usersApi';
import Table from '../../components/ui/Table';
import Button from '../../components/ui/Button';
import Badge from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import Input from '../../components/ui/Input';
import Loader from '../../components/ui/Loader';

export default function AdminUsersPage() {
  const { t } = useTranslation();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [blockModal, setBlockModal] = useState({ open: false, user: null });
  const [blockReason, setBlockReason] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => { loadUsers(); }, []);

  async function loadUsers() {
    setLoading(true);
    try {
      const res = await usersApi.getAllActivity();
      setUsers(res.data);
    } catch { /* ignore */ }
    setLoading(false);
  }

  const handleBlock = async () => {
    try {
      await usersApi.blockUser(blockModal.user.id, blockReason);
      setBlockModal({ open: false, user: null });
      setBlockReason('');
      loadUsers();
    } catch { /* ignore */ }
  };

  const handleUnblock = async (userId) => {
    try {
      await usersApi.unblockUser(userId);
      loadUsers();
    } catch { /* ignore */ }
  };

  const filtered = users.filter((u) =>
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase())
  );

  const columns = [
    { key: 'username', label: t('admin.username') },
    { key: 'email', label: t('admin.email') },
    { key: 'role', label: t('admin.role') },
    {
      key: 'isBlocked',
      label: t('admin.status'),
      render: (row) => (
        <Badge variant={row.isBlocked ? 'danger' : 'success'}>
          {row.isBlocked ? t('admin.blocked') : t('admin.active')}
        </Badge>
      ),
    },
    {
      key: 'actions',
      label: t('admin.actions'),
      sortable: false,
      render: (row) => (
        <div className="flex-gap-sm">
          {row.isBlocked ? (
            <Button variant="success" size="sm" onClick={() => handleUnblock(row.id)}>
              {t('admin.unblock')}
            </Button>
          ) : (
            <Button variant="danger" size="sm" onClick={() => setBlockModal({ open: true, user: row })}>
              {t('admin.block')}
            </Button>
          )}
        </div>
      ),
    },
  ];

  if (loading) return <Loader />;

  return (
    <div className="animate-fade-in">
      <h1 className="admin-page-title">{t('admin.usersTitle')}</h1>

      <div className="toolbar">
        <input
          type="text"
          className="search-input"
          placeholder={t('admin.searchUsers')}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="data-section">
        <Table columns={columns} data={filtered} />
      </div>

      <Modal
        isOpen={blockModal.open}
        onClose={() => setBlockModal({ open: false, user: null })}
        title={t('admin.blockUser')}
        footer={
          <>
            <Button variant="outline" size="sm" onClick={() => setBlockModal({ open: false, user: null })}>
              {t('admin.cancel')}
            </Button>
            <Button variant="danger" size="sm" onClick={handleBlock}>
              {t('admin.block')}
            </Button>
          </>
        }
      >
        <p>{t('admin.blockConfirm', { name: blockModal.user?.username })}</p>
        <Input
          label={t('admin.blockReason')}
          value={blockReason}
          onChange={(e) => setBlockReason(e.target.value)}
          type="textarea"
        />
      </Modal>
    </div>
  );
}
