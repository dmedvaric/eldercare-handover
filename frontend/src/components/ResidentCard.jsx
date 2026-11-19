import { useEffect, useState } from 'react'
import * as api from '../api'
import { fmtDate, fmtTime } from '../format'
import MedicationSection from './MedicationSection.jsx'
import TaskSection from './TaskSection.jsx'
import NotesSection from './NotesSection.jsx'

const MOODS = [
  { score: 1, emoji: '😞', label: 'Very low' },
  { score: 2, emoji: '😕', label: 'Low' },
  { score: 3, emoji: '😐', label: 'Okay' },
  { score: 4, emoji: '🙂', label: 'Good' },
  { score: 5, emoji: '😄', label: 'Great' },
]

const BED = {
  OK: { label: 'Bed linen OK', cls: 'ok' },
  DUE_TODAY: { label: 'Bed change due today', cls: 'warn' },
  OVERDUE: { label: 'Bed change overdue', cls: 'danger' },
}

export default function ResidentCard({ resident: r, nurse, shift, act, expandAll }) {
  const [open, setOpen] = useState(false)
  useEffect(() => {
    setOpen(expandAll)
  }, [expandAll])

  const bed = BED[r.bedStatus]
  const mood = r.todayMood ? MOODS.find((m) => m.score === r.todayMood.score) : null

  return (
    <article className="card">
      <button className="card-head" onClick={() => setOpen((o) => !o)} aria-expanded={open}>
        <div>
          <h2>{r.fullName}</h2>
          <p className="muted">
            Room {r.room} · Bed {r.bed}
          </p>
        </div>
        <span className="chevron" aria-hidden="true">
          {open ? '▾' : '▸'}
        </span>
      </button>

      <div className="chips">
        <span className={`chip ${bed.cls}`}>{bed.label}</span>
        {mood ? (
          <span className="chip">
            {mood.emoji} {mood.label}
          </span>
        ) : (
          <span className="chip warn">No mood yet today</span>
        )}
        {r.moodDeclining && <span className="chip danger">Mood declining</span>}
        {r.pendingMedications > 0 && (
          <span className="chip warn">💊 {r.pendingMedications} to give</span>
        )}
        {r.openTasks > 0 && (
          <span className="chip warn">
            ✔ {r.openTasks} {r.openTasks === 1 ? 'task' : 'tasks'}
          </span>
        )}
        {r.unacknowledgedNotes > 0 && (
          <span className="chip warn">📝 {r.unacknowledgedNotes} new note(s)</span>
        )}
      </div>

      {open && (
        <div className="card-body">
          {r.careNotes && <p className="care-notes">{r.careNotes}</p>}

          <section className="section">
            <h3>Notes from the last shift</h3>
            <NotesSection resident={r} nurse={nurse} shift={shift} act={act} />
          </section>

          <section className="section">
            <h3>Medication today</h3>
            <MedicationSection resident={r} nurse={nurse} act={act} />
          </section>

          <section className="section">
            <h3>Tasks</h3>
            <TaskSection resident={r} nurse={nurse} act={act} />
          </section>

          <section className="section">
            <h3>How is {r.fullName.split(' ')[0]} feeling today?</h3>
            <div className="mood-row">
              {MOODS.map((m) => (
                <button
                  key={m.score}
                  className={`mood-btn ${r.todayMood?.score === m.score ? 'active' : ''}`}
                  onClick={() => act(() => api.recordMood(r.id, { score: m.score, nurse }))}
                  title={m.label}
                >
                  <span className="emoji">{m.emoji}</span>
                  <span className="mood-label">{m.label}</span>
                </button>
              ))}
            </div>
            {r.todayMood && (
              <p className="muted small">
                Recorded {fmtTime(r.todayMood.recordedAt)} by {r.todayMood.nurse}
              </p>
            )}
            {r.moodHistory.length > 0 && (
              <div className="trend" aria-label="Mood over the last days">
                {r.moodHistory.map((d) => (
                  <div className="trend-col" key={d.date} title={`${fmtDate(d.date)}: ${d.average} of 5`}>
                    <span className="trend-bar" style={{ height: `${d.average * 7}px` }} />
                    <span className="trend-label">{fmtDate(d.date).slice(0, 3)}</span>
                  </div>
                ))}
              </div>
            )}
          </section>

          <section className="section">
            <h3>Bed linen</h3>
            <p className="muted">
              Last changed {fmtDate(r.lastBedChange)} · next due {fmtDate(r.bedChangeDueDate)}
            </p>
            <button
              className={`btn ${r.bedStatus === 'OK' ? '' : 'primary'}`}
              onClick={() => act(() => api.changeBed(r.id, nurse))}
            >
              Mark bed as changed
            </button>
          </section>
        </div>
      )}
    </article>
  )
}
