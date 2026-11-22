import type { TimeRange } from './api.ts'

export const timeFormat = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'Europe/Warsaw',
  dateStyle: 'short',
  timeStyle: 'short',
})

export const tickFormat = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'Europe/Warsaw',
  day: '2-digit',
  month: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
})

export function lastHours(hours: number): TimeRange {
  return { from: new Date(Date.now() - hours * 60 * 60 * 1000).toISOString() }
}
