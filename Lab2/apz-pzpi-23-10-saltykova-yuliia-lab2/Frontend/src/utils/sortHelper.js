/**
 * Locale-aware text sorting using Intl.Collator
 */

const LOCALE_MAP = {
  uk: 'uk-UA',
  en: 'en-US',
};

export function localeSort(array, key, locale = 'uk', direction = 'asc') {
  const collator = new Intl.Collator(LOCALE_MAP[locale] || locale, {
    sensitivity: 'base',
    numeric: true,
  });

  return [...array].sort((a, b) => {
    const valA = key ? (a[key] ?? '') : (a ?? '');
    const valB = key ? (b[key] ?? '') : (b ?? '');
    const result = collator.compare(String(valA), String(valB));
    return direction === 'asc' ? result : -result;
  });
}

export function localeSortMulti(array, keys, locale = 'uk') {
  const collator = new Intl.Collator(LOCALE_MAP[locale] || locale, {
    sensitivity: 'base',
    numeric: true,
  });

  return [...array].sort((a, b) => {
    for (const { key, direction = 'asc' } of keys) {
      const valA = a[key] ?? '';
      const valB = b[key] ?? '';
      const result = collator.compare(String(valA), String(valB));
      if (result !== 0) return direction === 'asc' ? result : -result;
    }
    return 0;
  });
}
