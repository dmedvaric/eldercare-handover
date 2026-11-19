import { useState } from 'react'
import * as api from '../api'
import { fmtTime, hhmm } from '../format'

export default function MedicationSection({ resident: r, nurse, act }) {
  const [reasonFor, setReasonFor] = useState(null) // medication id waiting for a "not given" reason
  const [reason, setReason] = useState('')
  const [editing, setEditing] = useState(null) // medication id whose decision is being changed
  const [adding, setAdding] = useState(false)
  const [form, setForm] = useState({ name: '', dose: '', time: '' })

  const give = async (m) => {
    if (await act(() => api.administer(m.id, { status: 'GIVEN', nurse }))) setEditing(null)
  }

  const submitNotGiven = async (m) => {
    if (!reason.trim()) return
    const ok = await act(() => api.administer(m.id, { status: 'NOT_GIVEN', reason, nurse }))
    if (ok) {
      setReasonFor(null)
      setReason('')
      setEditing(null)
    }
  }

  const submitNew = async (e) => {
    e.preventDefault()
    if (!form.name.trim()) return
    const ok = await act(() =>
      api.addMedication(r.id, {
        name: form.name,
        dose: form.dose,
        scheduledTime: form.time || null,
        onlyToday: true,
        nurse,
      }),
    )
    if (ok) {
      setForm({ name: '', dose: '', time: '' })
      setAdding(false)
    }
  }

  return (
    <>
      {r.medications.length === 0 && <p className="muted">No medication scheduled today.</p>}
      <ul className="list">
        {r.medications.map((m) => {
          const handled = m.status !== null
          const showButtons = !handled || editing === m.id
          return (
            <li key={m.id} className={`med-row ${handled ? m.status.toLowerCase() : ''}`}>
              <div className="med-main">
                <span className="time">{hhmm(m.scheduledTime)}</span>
                <span className="med-name">
                  {m.name} {m.dose}
                </span>
                {m.onlyToday && <span className="tag">today only</span>}
              </div>

              {handled && (
                <p className="small status-line">
                  {m.status === 'GIVEN' ? '✓ Given' : '✗ Not given'} · {fmtTime(m.recordedAt)} · {m.nurse}
                  {m.reason ? ` · ${m.reason}` : ''}{' '}
                  {editing !== m.id && (
                    <button className="link" onClick={() => setEditing(m.id)}>
                      change
                    </button>
                  )}
                </p>
              )}

              {showButtons && reasonFor !== m.id && (
                <div className="btn-row">
                  <button className="btn primary" onClick={() => give(m)}>
                    Given
                  </button>
                  <button className="btn" onClick={() => setReasonFor(m.id)}>
                    Not given
                  </button>
                </div>
              )}

              {reasonFor === m.id && (
                <div className="form-row">
                  <input
                    autoFocus
                    value={reason}
                    onChange={(e) => setReason(e.target.value)}
                    placeholder="Reason (required), e.g. refused, asleep"
                    onKeyDown={(e) => e.key === 'Enter' && submitNotGiven(m)}
                  />
                  <button className="btn primary" onClick={() => submitNotGiven(m)} disabled={!reason.trim()}>
                    Save
                  </button>
                  <button
                    className="btn ghost"
                    onClick={() => {
                      setReasonFor(null)
                      setReason('')
                    }}
                  >
                    Cancel
                  </button>
                </div>
              )}
            </li>
          )
        })}
      </ul>

      {adding ? (
        <form className="form-grid" onSubmit={submitNew}>
          <input
            autoFocus
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            placeholder="Medication"
          />
          <input
            value={form.dose}
            onChange={(e) => setForm({ ...form, dose: e.target.value })}
            placeholder="Dose"
          />
          <input
            type="time"
            value={form.time}
            onChange={(e) => setForm({ ...form, time: e.target.value })}
            aria-label="Time"
          />
          <button className="btn primary" type="submit" disabled={!form.name.trim()}>
            Add for today
          </button>
          <button className="btn ghost" type="button" onClick={() => setAdding(false)}>
            Cancel
          </button>
        </form>
      ) : (
        <button className="btn ghost" onClick={() => setAdding(true)}>
          + Medication for today only
        </button>
      )}
    </>
  )
}
