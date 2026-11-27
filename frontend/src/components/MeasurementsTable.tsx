import type { Series } from '../api.ts'
import { timeFormat } from '../format.ts'
import type { Row } from '../rows.ts'

type Props = {
  visible: Series[]
  rows: Row[]
  selectedTime: number | null
  onSelect: (time: number) => void
}

export default function MeasurementsTable({ visible, rows, selectedTime, onSelect }: Props) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th scope="col">Time</th>
            {visible.map((s) => (
              <th key={s.id} scope="col">
                {s.name}
                {s.unit ? ` (${s.unit})` : ''}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.time} className={row.time === selectedTime ? 'selected' : undefined}>
              <td>
                <button
                  type="button"
                  className="row-button"
                  aria-pressed={row.time === selectedTime}
                  onClick={() => onSelect(row.time)}
                >
                  {timeFormat.format(row.time)}
                </button>
              </td>
              {visible.map((s) => (
                <td key={s.id}>{row.values[s.id]?.value ?? '–'}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
