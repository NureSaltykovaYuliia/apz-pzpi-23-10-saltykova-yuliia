import { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate } from 'react-router-dom';
import * as signalR from '@microsoft/signalr';
import axios from 'axios';
import axiosClient from '../../api/axiosClient';
import { useAuth } from '../../contexts/AuthContext';
import { conversationsApi } from '../../api/conversationsApi';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Loader from '../../components/ui/Loader';

export default function MessagesPage() {
  const { t } = useTranslation();
  const { 
    user, 
    token, 
    connection: hubConnection, 
    setUnreadCount, 
    setActiveConversationId,
    onMessageReceivedRef 
  } = useAuth();
  const { userId: targetUserId } = useParams();
  const navigate = useNavigate();
  
  const [conversations, setConversations] = useState([]);
  const [activeConversation, setActiveConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(true);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  
  const messagesEndRef = useRef(null);
  const startingConv = useRef(false);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadConversations = async () => {
    try {
      const res = await conversationsApi.getConversations();
      setConversations(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const loadMessages = async (id) => {
    try {
      const res = await conversationsApi.getMessages(id);
      setMessages(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadConversations();
  }, []);

  useEffect(() => {
    if (activeConversation) {
      loadMessages(activeConversation.id);
      if (hubConnection && hubConnection.state === 'Connected') {
        hubConnection.invoke('JoinConversation', activeConversation.id)
          .catch(err => console.error('JoinConversation error:', err));
      }
    }
  }, [activeConversation, hubConnection]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // SignalR setup via AuthContext
  useEffect(() => {
    setUnreadCount(0);
    
    onMessageReceivedRef.current = (message) => {
      setMessages(prev => {
        if (prev.find(m => m.id === message.id)) return prev;
        return [...prev, message];
      });
      setConversations(prev => prev.map(c => 
        c.id === message.conversationId ? { ...c, lastMessage: message.content } : c
      ));
      scrollToBottom();
    };

    return () => {
      onMessageReceivedRef.current = null;
      setActiveConversationId(null);
    };
  }, []);

  useEffect(() => {
    if (activeConversation) {
      setActiveConversationId(activeConversation.id);
    }
  }, [activeConversation]);

  // Handle targetUserId from URL
  useEffect(() => {
    if (targetUserId && !startingConv.current) {
      startingConv.current = true;
      startNewConversation(targetUserId);
    }
  }, [targetUserId]);

  const startNewConversation = async (tId) => {
    try {
      const res = await conversationsApi.getOrCreatePrivate(tId);
      const conv = res.data;
      setConversations(prev => {
        if (prev.find(c => c.id === conv.id)) return prev;
        return [conv, ...prev];
      });
      setActiveConversation(conv);
      navigate('/messages', { replace: true });
    } catch (err) {
      console.error(err);
      alert(t('common.error') || "Failed to start conversation");
    } finally {
      startingConv.current = false;
    }
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!newMessage.trim() || !activeConversation || !hubConnection) return;

    try {
      await hubConnection.invoke('SendMessage', activeConversation.id, newMessage);
      setNewMessage('');
    } catch (err) {
      console.error('SignalR SendMessage error:', err);
      alert(t('common.error') || "Failed to send message");
    }
  };

  const handleDeleteConversation = async (e, id) => {
    e.stopPropagation();
    if (!window.confirm(t('common.confirmDelete') || "Видалити цей чат?")) return;
    try {
      await conversationsApi.deleteConversation(id);
      setConversations(prev => prev.filter(c => c.id !== id));
      if (activeConversation?.id === id) {
        setActiveConversation(null);
      }
    } catch (err) {
      console.error(err);
      alert(t('common.error') || "Failed to delete chat");
    }
  };

  const getPartnerName = (conv) => {
    if (conv.name) return conv.name;
    const myName = user?.username?.toLowerCase();
    return conv.participantNames.find(n => n.toLowerCase() !== myName) || 'Chat';
  };

  if (loading) return <Loader />;

  return (
    <div className="page-container animate-fade-in" style={{ height: 'calc(100vh - 120px)', display: 'flex', gap: 'var(--space-md)' }}>
      {/* Left Sidebar - Conversation List */}
      <div className="card brutal-border" style={{ width: 350, display: 'flex', flexDirection: 'column', padding: 0, overflow: 'hidden' }}>
        <div style={{ padding: 'var(--space-md)', borderBottom: '2px solid var(--color-black)', background: 'var(--color-yellow-400)' }}>
          <h2 className="text-headline-sm">{t('messages.title')}</h2>
        </div>
        <div style={{ flex: 1, overflowY: 'auto' }}>
          {conversations.length === 0 ? (
            <div style={{ padding: 'var(--space-md)', textAlign: 'center', color: 'var(--color-gray-500)' }}>
              <p style={{ marginBottom: 'var(--space-md)' }}>{t('messages.noChats')}</p>
              <Button variant="outline" size="sm" onClick={() => navigate('/friends')}>
                {t('messages.findFriends')}
              </Button>
            </div>
          ) : (
            conversations.map(conv => (
              <div 
                key={conv.id}
                onClick={() => setActiveConversation(conv)}
                style={{ 
                  padding: 'var(--space-md)', 
                  cursor: 'pointer',
                  borderBottom: '1px solid var(--color-gray-200)',
                  background: activeConversation?.id === conv.id ? 'var(--color-gray-100)' : 'transparent',
                  transition: 'var(--transition-fast)',
                  position: 'relative'
                }}
                className="hover-lift"
              >
                <div className="flex-between">
                  <span style={{ fontWeight: 'bold' }}>{getPartnerName(conv)}</span>
                  <button 
                    onClick={(e) => handleDeleteConversation(e, conv.id)}
                    style={{ 
                      background: 'none', 
                      border: 'none', 
                      color: 'var(--color-gray-400)',
                      cursor: 'pointer',
                      padding: '4px'
                    }}
                    className="hover-text-danger"
                    title={t('common.delete')}
                  >
                    <span className="material-symbols-outlined" style={{ fontSize: 18 }}>delete</span>
                  </button>
                </div>
                <div style={{ fontSize: 13, color: 'var(--color-gray-500)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', paddingRight: 24 }}>
                  {conv.lastMessage || '...'}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Right Content - Chat Window */}
      <div className="card brutal-border" style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: 0, overflow: 'hidden' }}>
        {activeConversation ? (
          <>
            {/* Header */}
            <div style={{ padding: 'var(--space-md)', borderBottom: '2px solid var(--color-black)', background: 'var(--color-black)', color: 'var(--color-white)' }}>
              <h3 className="text-headline-sm">{getPartnerName(activeConversation)}</h3>
            </div>

            {/* Messages */}
            <div style={{ flex: 1, overflowY: 'auto', padding: 'var(--space-md)', display: 'flex', flexDirection: 'column', gap: 'var(--space-sm)', background: 'var(--color-gray-50)' }}>
              {messages.map(m => {
                const isMine = m.senderId === user.id;
                return (
                  <div 
                    key={m.id} 
                    style={{ 
                      alignSelf: isMine ? 'flex-end' : 'flex-start',
                      maxWidth: '70%',
                      padding: '10px 14px',
                      borderRadius: 'var(--radius-md)',
                      background: isMine ? 'var(--color-yellow-400)' : 'var(--color-white)',
                      border: '2px solid var(--color-black)',
                      boxShadow: 'var(--shadow-sm)',
                      position: 'relative'
                    }}
                  >
                    <div style={{ fontSize: 14, color: isMine ? '#1b1b1b' : 'inherit' }}>{m.content}</div>
                    <div style={{ fontSize: 10, marginTop: 4, textAlign: 'right', opacity: 0.6 }}>
                      {new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </div>
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>

            {/* Emoji Picker Overlay */}
            {showEmojiPicker && (
              <div 
                style={{ 
                  position: 'absolute', 
                  bottom: 80, 
                  left: 'var(--space-md)', 
                  background: 'var(--color-white)', 
                  border: '2px solid var(--color-black)', 
                  padding: 'var(--space-sm)',
                  display: 'grid',
                  gridTemplateColumns: 'repeat(6, 1fr)',
                  gap: '4px',
                  zIndex: 10,
                  boxShadow: 'var(--shadow-md)'
                }}
              >
                {['😊', '😂', '😍', '👍', '🙏', '🔥', '🐶', '🐾', '🦴', '❤️', '😎', '😢', '😮', '👏', '🎉', '✨', '🎂', '🍕'].map(emoji => (
                  <button 
                    key={emoji}
                    type="button"
                    onClick={() => {
                      setNewMessage(prev => prev + emoji);
                      setShowEmojiPicker(false);
                    }}
                    style={{ fontSize: 24, background: 'none', border: 'none', cursor: 'pointer' }}
                    className="hover-scale"
                  >
                    {emoji}
                  </button>
                ))}
              </div>
            )}

            {/* Input */}
            <form onSubmit={handleSendMessage} style={{ padding: 'var(--space-md)', borderTop: '2px solid var(--color-black)', background: 'var(--color-white)', display: 'flex', gap: 'var(--space-sm)', alignItems: 'center', position: 'relative' }}>
              <button 
                type="button"
                onClick={() => setShowEmojiPicker(!showEmojiPicker)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                className="hover-scale"
              >
                <span className="material-symbols-outlined" style={{ color: 'var(--color-gray-600)' }}>mood</span>
              </button>

              <div style={{ flex: 1 }}>
                <Input 
                  placeholder={t('messages.typePlaceholder')}
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  style={{ marginBottom: 0 }}
                />
              </div>
              <Button variant="dark" type="submit" disabled={!newMessage.trim()}>
                <span className="material-symbols-outlined">send</span>
              </Button>
            </form>
          </>
        ) : (
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-gray-400)', flexDirection: 'column' }}>
            <span className="material-symbols-outlined" style={{ fontSize: 64, marginBottom: 'var(--space-md)' }}>chat</span>
            <p>{t('messages.selectChat')}</p>
          </div>
        )}
      </div>
    </div>
  );
}
