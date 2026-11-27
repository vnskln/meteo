import { useMemo, useState } from 'react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { getMeasurements, getSeries, type TimeRange } from '../api.ts'
import Filters from '../components/Filters.tsx'
import MeasurementsChart from '../components/MeasurementsChart.tsx'
import MeasurementsTable from '../components/MeasurementsTable.tsx'
import { lastHours, timeFormat } from '../format.ts'
import { toRows } from '../rows.ts'

export default function ReaderPage() {
  const [range, setRange] = useState<TimeRange>(() => lastHours(24))
  const [hiddenIds, setHiddenIds] = useState<number[]>([])
  const [selectedTime, setSelectedTime] = useState<number | null>(null)

  const series = useQuery({ queryKey: ['series'], queryFn: getSeries })
  const measurements = useQuery({
    queryKey: ['measurements', range],
    queryFn: () => getMeasurements(range),
    placeholderData: keepPreviousData,
  })

  const allSeries = useMemo(() => series.data ?? [], [series.data])
  const visible = useMemo(
    () => allSeries.filter((s) => !hiddenIds.includes(s.id)),
    [allSeries, hiddenIds],
  )
  const shown = useMemo(
    () => (measurements.data ?? []).filter((m) => !hiddenIds.includes(m.seriesId)),
    [measurements.data, hiddenIds],
  )
  const rows = useMemo(() => toRows(shown), [shown])
  const selected = rows.find((row) => row.time === selectedTime) ?? null

  function toggleRow(time: number) {
    setSelectedTime(time === selectedTime ? null : time)
  }

  function toggleSeries(id: number) {
    setHiddenIds(hiddenIds.includes(id) ? hiddenIds.filter((h) => h !== id) : [...hiddenIds, id])
  }

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
        {allSeries.map((s) => (
          <li key={s.id}>
            {s.name}: {s.minValue} to {s.maxValue} {s.unit}
          </li>
        ))}
      </ul>

      <Filters
        series={allSeries}
        hiddenIds={hiddenIds}
        range={range}
        onApply={setRange}
        onToggle={toggleSeries}
      />

      <h2>
        {timeFormat.format(new Date(range.from))} –{' '}
        {range.to ? timeFormat.format(new Date(range.to)) : 'now'}
      </h2>
      <MeasurementsChart
        series={allSeries}
        visible={visible}
        measurements={shown}
        selected={selected}
      />

      <MeasurementsTable
        visible={visible}
        rows={rows}
        selectedTime={selectedTime}
        onSelect={toggleRow}
      />
    </>
  )
}
