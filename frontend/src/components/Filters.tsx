import { useState, type FormEvent } from 'react'
import type { Series, TimeRange } from '../api.ts'
import { lastHours } from '../format.ts'

const PRESETS = [
  { label: 'Last 24 hours', hours: 24 },
  { label: 'Last 7 days', hours: 7 * 24 },
  { label: 'Last 30 days', hours: 30 * 24 },
]

function toInputValue(time: string) {
  const date = new Date(time)
  const offset = date.getTimezoneOffset() * 60 * 1000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

type Props = {
  series: Series[]
  hiddenIds: number[]
  range: TimeRange
  onApply: (range: TimeRange) => void
  onToggle: (id: number) => void
}

export default function Filters({ series, hiddenIds, range, onApply, onToggle }: Props) {
  const [from, setFrom] = useState(() => toInputValue(range.from))
  const [to, setTo] = useState(() => (range.to ? toInputValue(range.to) : ''))
  const [error, setError] = useState('')

  function applyPreset(hours: number) {
    const preset = lastHours(hours)
    setFrom(toInputValue(preset.from))
    setTo('')
    setError('')
    onApply(preset)
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!from) {
      setError('Enter the start of the time range.')
      return
    }
    if (to && from >= to) {
      setError('The start must be earlier than the end.')
      return
    }
    setError('')
    onApply({
      from: new Date(from).toISOString(),
      to: to ? new Date(to).toISOString() : undefined,
    })
  }

  return (
    <form className="filters" onSubmit={handleSubmit}>
      <fieldset>
        <legend>Time range</legend>
        {PRESETS.map((p) => (
          <button key={p.hours} type="button" onClick={() => applyPreset(p.hours)}>
            {p.label}
          </button>
        ))}
        <label>
          From <input type="datetime-local" value={from} onChange={(e) => setFrom(e.target.value)} />
        </label>
        <label>
          To <input type="datetime-local" value={to} onChange={(e) => setTo(e.target.value)} />
        </label>
        <button type="submit">Apply</button>
        {error && <p role="alert">{error}</p>}
      </fieldset>

      <fieldset>
        <legend>Series</legend>
        {series.map((s) => (
          <label key={s.id}>
            <input
              type="checkbox"
              checked={!hiddenIds.includes(s.id)}
              onChange={() => onToggle(s.id)}
            />{' '}
            {s.name}
          </label>
        ))}
      </fieldset>
    </form>
  )
}
