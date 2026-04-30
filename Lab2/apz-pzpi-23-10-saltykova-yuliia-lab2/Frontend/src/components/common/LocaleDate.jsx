import { useLocale } from '../../contexts/LocaleContext';
import { formatDate, formatDateTime, formatRelativeTime } from '../../utils/dateFormatter';

export default function LocaleDate({ date, showTime = false, relative = false }) {
  const { locale } = useLocale();

  if (!date) return <span>—</span>;

  let formatted;
  if (relative) {
    formatted = formatRelativeTime(date, locale);
  } else if (showTime) {
    formatted = formatDateTime(date, locale);
  } else {
    formatted = formatDate(date, locale);
  }

  return <span>{formatted}</span>;
}
