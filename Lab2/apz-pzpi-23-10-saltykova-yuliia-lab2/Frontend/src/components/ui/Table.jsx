import { useTranslation } from 'react-i18next';

export default function Table({ columns, data, onSort, sortKey, sortDir }) {
  const { t } = useTranslation();

  if (!data || data.length === 0) {
    return (
      <div className="empty-state">
        <span className="material-symbols-outlined filled">inbox</span>
        <p>{t('common.noData')}</p>
      </div>
    );
  }

  return (
    <div className="data-table-wrapper">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                onClick={() => onSort && col.sortable !== false && onSort(col.key)}
                style={{ cursor: onSort && col.sortable !== false ? 'pointer' : 'default' }}
              >
                {col.label}
                {sortKey === col.key && (
                  <span style={{ marginLeft: 4 }}>{sortDir === 'asc' ? '▲' : '▼'}</span>
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row, idx) => (
            <tr key={row.id || idx}>
              {columns.map((col) => (
                <td key={col.key}>
                  {col.render ? col.render(row) : row[col.key] ?? '—'}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
