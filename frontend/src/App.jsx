import { useCallback, useEffect, useMemo, useState } from 'react'
import * as api from './api'
import ResidentCard from './components/ResidentCard.jsx'
import ActivityFeed from './components/ActivityFeed.jsx'

const SHIFTS = [
  ['EARLY', 'Early'],
  ['LATE', 'Late'],
  ['NIGHT', 'Night'],
]

function usePersistentState(key, initial) {
  const [value, setValue] = useState(() => {
    try {
      return localStorage.getItem(key) ?? initial
    } catch {
      return initial
    }
  })
  useEffect(() => {
    try {
      localStorage.setItem(key, value)
    } catch {
      // storage unavailable (private mode), the value just won't survive a reload
    }
  }, [key, value])
  return [value, setValue]
}

function needsAttention(r) {
  return (
    r.pendingMedications > 0 ||
    r.openTasks > 0 ||
    r.unacknowledgedNotes > 0 ||
    r.bedStatus !== 'OK' ||
    r.moodDeclining ||
    !r.todayMood
  )
}

export default function App() {
  const [nurse, setNurse] = usePersistentState('handover.nurse', '')
  const [shift, setShift] = usePersistentState('handover.shift', 'EARLY')
  const [tab, setTab] = useState('board')
  const [filter, setFilter] = useState('all')
  const [expandAll, setExpandAll] = useState(false)
  const [residents, setResidents] = useState([])
  const [activity, setActivity] = useState([])
  const [hours, setHours] = useState(8)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    try {
      setResidents(await api.getResidents())
      setError('')
    } catch (e) {
      setError(`Cannot reach the server: ${e.message}`)
    } finally {
      setLoading(false)
    }
  }, [])

  // initial load + refresh every 30 s so changes from colleagues show up
  useEffect(() => {
    load()
    const timer = setInterval(load, 30000)
    return () => clearInterval(timer)
  }, [load])

  const loadActivity = useCallback(async () => {
    try {
      setActivity(await api.getActivity(hours))
    } catch (e) {
      setError(`Cannot load changes: ${e.message}`)
    }
  }, [hours])

  useEffect(() => {
    if (tab === 'changes') loadActivity()
  }, [tab, loadActivity])

  // Runs a write call, swaps the returned resident into the list, reports success.
  const act = useCallback(async (call) => {
    try {
      const updated = await call()
      setResidents((list) => list.map((r) => (r.id === updated.id ? updated : r)))
      setError('')
      return true
    } catch (e) {
      setError(e.message)
      return false
    }
  }, [])

  const totals = useMemo(
    () => ({
      meds: residents.reduce((sum, r) => sum + r.pendingMedications, 0),
      tasks: residents.reduce((sum, r) => sum + r.openTasks, 0),
      notes: residents.reduce((sum, r) => sum + r.unacknowledgedNotes, 0),
      beds: residents.filter((r) => r.bedStatus !== 'OK').length,
    }),
    [residents],
  )

  const visible = filter === 'attention' ? residents.filter(needsAttention) : residents

  return (
    <div className="app">
      <header className="topbar">
        <div className="topbar-inner">
          <h1>Care Handover</h1>
          <div className="who">
            <input
              className={nurse.trim() ? '' : 'needs-input'}
              value={nurse}
              onChange={(e) => setNurse(e.target.value)}
              placeholder="Your name"
              aria-label="Your name"
            />
            <div className="segmented" role="group" aria-label="Your shift">
              {SHIFTS.map(([value, label]) => (
                <button
                  key={value}
                  className={shift === value ? 'active' : ''}
                  onClick={() => setShift(value)}
                >
                  {label}
                </button>
              ))}
            </div>
          </div>
          <nav className="tabs">
            <button className={tab === 'board' ? 'active' : ''} onClick={() => setTab('board')}>
              Handover board
            </button>
            <button className={tab === 'changes' ? 'active' : ''} onClick={() => setTab('changes')}>
              What changed
            </button>
          </nav>
        </div>
      </header>

      <main className="content">
        {error && (
          <div className="banner error" role="alert">
            {error}
            <button className="link" onClick={() => setError('')}>
              dismiss
            </button>
          </div>
        )}

        {tab === 'board' && (
          <>
            <div className="summary">
              <div className={`stat ${totals.meds ? 'warn' : ''}`}>
                <strong>{totals.meds}</strong>
                <span>medications open</span>
              </div>
              <div className={`stat ${totals.tasks ? 'warn' : ''}`}>
                <strong>{totals.tasks}</strong>
                <span>tasks open</span>
              </div>
              <div className={`stat ${totals.notes ? 'warn' : ''}`}>
                <strong>{totals.notes}</strong>
                <span>notes to read</span>
              </div>
              <div className={`stat ${totals.beds ? 'danger' : ''}`}>
                <strong>{totals.beds}</strong>
                <span>beds to change</span>
              </div>
            </div>

            <div className="toolbar">
              <div className="segmented" role="group" aria-label="Filter residents">
                <button className={filter === 'all' ? 'active' : ''} onClick={() => setFilter('all')}>
                  All residents
                </button>
                <button
                  className={filter === 'attention' ? 'active' : ''}
                  onClick={() => setFilter('attention')}
                >
                  Needs attention
                </button>
              </div>
              <button className="btn ghost" onClick={() => setExpandAll((v) => !v)}>
                {expandAll ? 'Collapse all' : 'Expand all'}
              </button>
            </div>

            {loading && <p className="muted center">Loading…</p>}
            {!loading && visible.length === 0 && !error && (
              <p className="muted center">Nothing needs attention right now. 🎉</p>
            )}

            <div className="cards">
              {visible.map((resident) => (
                <ResidentCard
                  key={resident.id}
                  resident={resident}
                  nurse={nurse}
                  shift={shift}
                  act={act}
                  expandAll={expandAll}
                />
              ))}
            </div>
          </>
        )}

        {tab === 'changes' && (
          <ActivityFeed activity={activity} hours={hours} onHoursChange={setHours} onRefresh={loadActivity} />
        )}
      </main>
    </div>
  )
}
