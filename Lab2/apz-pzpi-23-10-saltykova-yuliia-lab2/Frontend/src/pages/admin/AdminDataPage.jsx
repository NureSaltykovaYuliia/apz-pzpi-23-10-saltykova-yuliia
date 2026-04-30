import { useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { saveAs } from 'file-saver';
import axiosClient from '../../api/axiosClient';
import Button from '../../components/ui/Button';

export default function AdminDataPage() {
  const { t } = useTranslation();
  const fileRef = useRef();

  const handleExport = async () => {
    try {
      const res = await axiosClient.get('/data/export', { responseType: 'blob' });
      saveAs(res.data, `mydogspace-backup-${Date.now()}.json`);
    } catch { /* ignore */ }
  };

  const handleImport = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);
    try {
      await axiosClient.post('/data/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      alert(t('admin.importSuccess'));
    } catch {
      alert(t('admin.importError'));
    }
    if (fileRef.current) fileRef.current.value = '';
  };

  return (
    <div className="animate-fade-in">
      <h1 className="admin-page-title">{t('admin.dataTitle')}</h1>

      <div className="grid-cols-2">
        {/* Export */}
        <div className="data-section">
          <h3>
            <span className="material-symbols-outlined" style={{ fontSize: 24, marginRight: 8 }}>cloud_download</span>
            {t('admin.exportData')}
          </h3>
          <p>{t('admin.exportDesc')}</p>
          <Button variant="primary" size="md" onClick={handleExport}>
            <span className="material-symbols-outlined" style={{ fontSize: 18 }}>download</span>
            {t('admin.exportButton')}
          </Button>
        </div>

        {/* Import */}
        <div className="data-section">
          <h3>
            <span className="material-symbols-outlined" style={{ fontSize: 24, marginRight: 8 }}>cloud_upload</span>
            {t('admin.importData')}
          </h3>
          <p>{t('admin.importDesc')}</p>
          <div className="file-upload-zone" onClick={() => fileRef.current?.click()}>
            <span className="material-symbols-outlined">upload_file</span>
            <p>{t('admin.dropFile')}</p>
          </div>
          <input ref={fileRef} type="file" accept=".json" style={{ display: 'none' }} onChange={handleImport} />
        </div>
      </div>
    </div>
  );
}
