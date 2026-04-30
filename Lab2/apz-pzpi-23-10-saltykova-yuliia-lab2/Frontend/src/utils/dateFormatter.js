/**
 * Locale-aware date/time formatting using Intl.DateTimeFormat
 */

const LOCALE_MAP = {
  uk: 'uk-UA',
  en: 'en-US',
};

export function formatDate(date, locale = 'uk') {
  if (!date) return '—';
  const d = new Date(date);
  if (isNaN(d.getTime())) return '—';
  return new Intl.DateTimeFormat(LOCALE_MAP[locale] || locale, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(d);
}

export function formatDateTime(date, locale = 'uk') {
  if (!date) return '—';
  const d = new Date(date);
  if (isNaN(d.getTime())) return '—';
  return new Intl.DateTimeFormat(LOCALE_MAP[locale] || locale, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(d);
}

export function formatRelativeTime(date, locale = 'uk') {
  if (!date) return '—';
  const d = new Date(date);
  const now = new Date();
  const diffMs = now - d;
  const diffMin = Math.floor(diffMs / 60000);
  const diffHr = Math.floor(diffMs / 3600000);
  const diffDay = Math.floor(diffMs / 86400000);

  const rtf = new Intl.RelativeTimeFormat(LOCALE_MAP[locale] || locale, { numeric: 'auto' });

  if (diffMin < 1) return locale === 'uk' ? 'Щойно' : 'Just now';
  if (diffMin < 60) return rtf.format(-diffMin, 'minute');
  if (diffHr < 24) return rtf.format(-diffHr, 'hour');
  if (diffDay < 30) return rtf.format(-diffDay, 'day');
  return formatDate(date, locale);
}
