import { useState } from 'react'
import * as api from '../api'
import { SHIFT_LABEL, fmtDateTime } from '../format'

export default function NotesSection({ resident: r, nurse, shift, act }) {
  const [text, setText] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    if (!text.trim()) return
    const ok = await act(() => api.addNote(r.id, { shift, content: text, nurse }))
    if (ok) setText('')
  }

  return (
    <>
      {r.notes.length === 0 && <p className="muted">No notes.</p>}
      <ul className="list">
        {r.notes.map((n) => (
          <li key={n.id} className={`note ${n.acknowledgedAt ? '' : 'unread'}`}>
            <p className="small muted">
              {SHIFT_LABEL[n.shift]} · {n.author} · {fmtDateTime(n.createdAt)}
            </p>
            <p>{n.content}</p>
            {n.acknowledgedAt ? (
              <p className="small muted">
                ✓ Read by {n.acknowledgedBy} · {fmtDateTime(n.acknowledgedAt)}
              </p>
            ) : (
              <button className="btn primary" onClick={() => act(() => api.acknowledgeNote(n.id, nurse))}>
                I have read this
              </button>
            )}
          </li>
        ))}
      </ul>

      <form className="form-stack" onSubmit={submit}>
        <textarea
          rows={2}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Write a note for the next shift…"
        />
        <button className="btn" type="submit" disabled={!text.trim()}>
          Add note
        </button>
      </form>
    </>
  )
}
