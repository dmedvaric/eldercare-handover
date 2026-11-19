// Small date/time helpers. The backend sends ISO strings without a time zone (local time).

export function fmtTime(iso) {
  if (!iso) return ''
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export function fmtDate(iso) {
  if (!iso) return ''
  // A plain date such as "2026-11-03" would be parsed as UTC; pin it to local midnight instead.
  const date = iso.length === 10 ? new Date(`${iso}T00:00:00`) : new Date(iso)
  return date.toLocaleDateString([], { weekday: 'short', day: 'numeric', month: 'short' })
}

export function fmtDateTime(iso) {
  if (!iso) return ''
  return `${fmtDate(iso)}, ${fmtTime(iso)}`
}

/** "08:00:00" -> "08:00" */
export const hhmm = (time) => (time || '').slice(0, 5)

export const SHIFT_LABEL = { EARLY: 'Early shift', LATE: 'Late shift', NIGHT: 'Night shift' }
