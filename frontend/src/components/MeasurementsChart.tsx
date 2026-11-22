import { useMemo } from 'react'
import {
  Chart as ChartJS,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  TimeScale,
  Tooltip,
  type ChartOptions,
  type PointStyle,
} from 'chart.js'
import 'chartjs-adapter-date-fns'
import { Line } from 'react-chartjs-2'
import type { Measurement, Series } from '../api.ts'
import { tickFormat, timeFormat } from '../format.ts'

ChartJS.register(LineElement, PointElement, LinearScale, TimeScale, Tooltip, Legend)

const POINT_STYLES: PointStyle[] = ['circle', 'rect', 'triangle', 'rectRot', 'star', 'crossRot']
const LINE_DASHES = [[], [8, 4], [2, 3], [12, 4, 2, 4]]

const options: ChartOptions<'line'> = {
  responsive: true,
  maintainAspectRatio: false,
  animation: false,
  scales: {
    x: {
      type: 'time',
      ticks: { callback: (value) => tickFormat.format(Number(value)), maxTicksLimit: 8 },
    },
    y: { title: { display: true, text: 'Value' } },
  },
  plugins: {
    legend: { labels: { usePointStyle: true }, onClick: () => {} },
    tooltip: {
      callbacks: {
        title: (items) => timeFormat.format(items[0].parsed.x ?? 0),
      },
    },
  },
}

type Props = {
  series: Series[]
  visible: Series[]
  measurements: Measurement[]
}

export default function MeasurementsChart({ series, visible, measurements }: Props) {
  const data = useMemo(() => {
    const dense = measurements.length / visible.length > 300
    return {
      datasets: visible.map((s) => {
        const index = series.indexOf(s)
        return {
          label: s.unit ? `${s.name} (${s.unit})` : s.name,
          borderColor: s.color,
          backgroundColor: s.color,
          pointStyle: POINT_STYLES[index % POINT_STYLES.length],
          borderDash: LINE_DASHES[index % LINE_DASHES.length],
          pointRadius: dense ? 0.5 : 3,
          pointHoverRadius: 9,
          data: measurements
            .filter((m) => m.seriesId === s.id)
            .map((m) => ({ x: new Date(m.timestamp).getTime(), y: m.value }))
            .toReversed(),
        }
      }),
    }
  }, [series, visible, measurements])

  return (
    <div className="chart">
      <Line
        data={data}
        options={options}
        role="img"
        aria-label={`Line chart of ${visible.map((s) => s.name).join(', ')}. The same values are in the table below.`}
      />
    </div>
  )
}
