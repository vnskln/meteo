export type Series = {
  id: number
  name: string
  minValue: number
  maxValue: number
  color: string
  icon?: string | null
  unit?: string | null
}

export type Measurement = {
  id: number
  seriesId: number
  sensorId?: number
  value: number
  timestamp: string
}

export type TimeRange = {
  from: string
  to?: string
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path, { headers: { Accept: 'application/json' } })
  if (!response.ok) {
    throw new Error(`${response.status} ${response.statusText}`)
  }
  return response.json()
}

export function getSeries(): Promise<Series[]> {
  return getJson('/api/series')
}

export function getMeasurements(range: TimeRange): Promise<Measurement[]> {
  const params = new URLSearchParams({ from: range.from, sort: '-timestamp', limit: '10000' })
  if (range.to) {
    params.set('to', range.to)
  }
  return getJson(`/api/measurements?${params}`)
}
