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

export function getMeasurements(from: string): Promise<Measurement[]> {
  const params = new URLSearchParams({ from, sort: '-timestamp' })
  return getJson(`/api/measurements?${params}`)
}
