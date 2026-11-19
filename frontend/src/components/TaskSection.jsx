import { useState } from 'react'
import * as api from '../api'
import { fmtTime } from '../format'

export default function TaskSection({ resident: r, nurse, act }) {
  const [text, setText] = useState('')
  const [onlyToday, setOnlyToday] = useState(true)

  const submit = async (e) => {
    e.preventDefault()
    if (!text.trim()) return
    const ok = await act(() => api.addTask(r.id, { description: text, onlyToday, nurse }))
    if (ok) setText('')
  }

  return (
    <>
      {r.tasks.length === 0 && <p className="muted">No open tasks.</p>}
      <ul className="list">
        {r.tasks.map((t) => (
          <li key={t.id} className={`task-row ${t.done ? 'done' : ''}`}>
            <label>
              <input
                type="checkbox"
                checked={t.done}
                disabled={t.done}
                onChange={() => act(() => api.completeTask(t.id, nurse))}
              />
              <span>{t.description}</span>
            </label>
            <div className="task-meta">
              {t.onlyToday && <span className="tag">today only</span>}
              {t.done && (
                <span className="muted small">
                  {t.doneBy} · {fmtTime(t.doneAt)}
                </span>
              )}
            </div>
          </li>
        ))}
      </ul>

      <form className="form-row" onSubmit={submit}>
        <input value={text} onChange={(e) => setText(e.target.value)} placeholder="New task for the next shift" />
        <label className="check">
          <input type="checkbox" checked={onlyToday} onChange={(e) => setOnlyToday(e.target.checked)} />
          today only
        </label>
        <button className="btn primary" type="submit" disabled={!text.trim()}>
          Add
        </button>
      </form>
    </>
  )
}
