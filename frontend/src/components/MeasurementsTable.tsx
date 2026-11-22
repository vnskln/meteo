import type { Series } from '../api.ts'
import { timeFormat } from '../format.ts'
import type { Row } from '../rows.ts'

type Props = {
  visible: Series[]
  rows: Row[]
}

export default function MeasurementsTable({ visible, rows }: Props) {
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
            <tr key={row.time}>
              <td>{timeFormat.format(row.time)}</td>
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
