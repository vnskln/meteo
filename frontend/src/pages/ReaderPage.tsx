import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getMeasurements, getSeries } from '../api.ts'
import MeasurementsChart from '../components/MeasurementsChart.tsx'
import { lastHours, timeFormat } from '../format.ts'

export default function ReaderPage() {
  const [range] = useState(() => lastHours(24))
  const series = useQuery({ queryKey: ['series'], queryFn: getSeries })
  const measurements = useQuery({
    queryKey: ['measurements', range],
    queryFn: () => getMeasurements(range),
  })

  if (series.isPending || measurements.isPending) {
    return <p role="status">Loading…</p>
  }
  if (series.isError) {
    return <p role="alert">Could not load data: {series.error.message}</p>
  }
  if (measurements.isError) {
    return <p role="alert">Could not load data: {measurements.error.message}</p>
  }

  const names = new Map(series.data.map((s) => [s.id, s.name]))

  return (
    <>
      <h1>Measurements</h1>

      <h2>Series</h2>
      <ul>
        {series.data.map((s) => (
          <li key={s.id}>
            {s.name}: {s.minValue} to {s.maxValue} {s.unit}
          </li>
        ))}
      </ul>

      <h2>Last 24 hours</h2>
      <MeasurementsChart
        series={series.data}
        visible={series.data}
        measurements={measurements.data}
      />

      <table>
        <thead>
          <tr>
            <th scope="col">Time</th>
            <th scope="col">Series</th>
            <th scope="col">Value</th>
          </tr>
        </thead>
        <tbody>
          {measurements.data.map((m) => (
            <tr key={m.id}>
              <td>{timeFormat.format(new Date(m.timestamp))}</td>
              <td>{names.get(m.seriesId)}</td>
              <td>{m.value}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  )
}
