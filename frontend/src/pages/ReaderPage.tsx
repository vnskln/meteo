import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getMeasurements, getSeries } from '../api.ts'
import MeasurementsChart from '../components/MeasurementsChart.tsx'
import MeasurementsTable from '../components/MeasurementsTable.tsx'
import { lastHours } from '../format.ts'
import { toRows } from '../rows.ts'

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

      <MeasurementsTable visible={series.data} rows={toRows(measurements.data)} />
    </>
  )
}
