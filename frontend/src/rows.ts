import type { Measurement } from './api.ts'

export type Row = {
  time: number
  values: Record<number, Measurement>
}

const SAME_ROW_MS = 60 * 1000

export function toRows(measurements: Measurement[]) {
  const rows: Row[] = []
  for (const m of measurements.toReversed()) {
    const time = new Date(m.timestamp).getTime()
    const last = rows.at(-1)
    if (last && time - last.time < SAME_ROW_MS && !last.values[m.seriesId]) {
      last.values[m.seriesId] = m
    } else {
      rows.push({ time, values: { [m.seriesId]: m } })
    }
  }
  return rows.toReversed()
}
