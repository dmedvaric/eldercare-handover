import { fmtTime } from '../format'

const WINDOWS = [4, 8, 12, 24]

export default function ActivityFeed({ activity, hours, onHoursChange, onRefresh }) {
  // group by resident so the incoming nurse reads one person at a time
  const groups = new Map()
  for (const event of activity) {
    if (!groups.has(event.residentId)) groups.set(event.residentId, { name: event.residentName, events: [] })
    groups.get(event.residentId).events.push(event)
  }

  return (
    <section>
      <div className="toolbar">
        <div className="segmented" role="group" aria-label="Time window">
          {WINDOWS.map((h) => (
            <button key={h} className={hours === h ? 'active' : ''} onClick={() => onHoursChange(h)}>
              Last {h} h
            </button>
          ))}
        </div>
        <button className="btn ghost" onClick={onRefresh}>
          Refresh
        </button>
      </div>

      {activity.length === 0 && <p className="muted center">Nothing has changed in this period.</p>}

      <div className="cards">
        {[...groups.values()].map((group) => (
          <article className="card" key={group.name}>
            <div className="card-head static">
              <h2>{group.name}</h2>
              <span className="muted small">{group.events.length} change(s)</span>
            </div>
            <ul className="list feed">
              {group.events.map((e) => (
                <li key={e.id}>
                  <span className="time">{fmtTime(e.occurredAt)}</span>
                  <span>
                    {e.summary} <span className="muted small">· {e.nurse}</span>
                  </span>
                </li>
              ))}
            </ul>
          </article>
        ))}
      </div>
    </section>
  )
}
