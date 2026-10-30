import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { getPersonalTimetable } from '../services/personalTimetableService'
import type { PersonalTimetable } from '../services/personalTimetableService'
import '../styles/allocation.css'

const weekdays = [
  '',
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'Sunday',
]

export default function MyTimetablePage() {
  const [data, setData] = useState<PersonalTimetable | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [refresh, setRefresh] = useState(0)
  const [selectedDay, setSelectedDay] = useState(0)
  const [lastChecked, setLastChecked] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function loadTimetable() {
      setLoading(true)
      setError('')

      try {
        const result = await getPersonalTimetable(controller.signal)

        if (!controller.signal.aborted) {
          setData(result)
          setLastChecked(new Date().toLocaleTimeString())
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          setData(null)
          setError(
            err instanceof Error
              ? err.message
              : 'Unable to load your timetable.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void loadTimetable()

    return () => controller.abort()
  }, [refresh])

  useEffect(() => {
    function refreshIfVisible() {
      if (document.visibilityState === 'visible') {
        setRefresh((value) => value + 1)
      }
    }

    const intervalId = window.setInterval(refreshIfVisible, 60_000)

    window.addEventListener('focus', refreshIfVisible)

    return () => {
      window.clearInterval(intervalId)
      window.removeEventListener('focus', refreshIfVisible)
    }
  }, [])

  const sessions = (data?.sessions ?? []).filter(
    (session) =>
      selectedDay === 0 || session.dayOfWeek === selectedDay,
  )

  return (
    <main className="allocation-page">
      <Link className="allocation-back" to="/dashboard">
        ← Back to dashboard
      </Link>

      <header className="allocation-heading">
        <p className="allocation-eyebrow">YOUR PUBLISHED SCHEDULE</p>
        <h1>My timetable</h1>
        <p>
          View the published classes assigned to your teaching profile
          or section.
        </p>
      </header>

      <section
        className="allocation-panel"
        aria-labelledby="my-timetable-heading"
        aria-busy={loading}
      >
        <div className="allocation-section-heading">
          <h2 id="my-timetable-heading">Weekly timetable</h2>

          <button
            className="allocation-button"
            type="button"
            disabled={loading}
            onClick={() => setRefresh((value) => value + 1)}
          >
            {loading ? 'Refreshing…' : 'Refresh timetable'}
          </button>
        </div>

        <p className="timetable-update-note">
          Checks for updates every minute while this tab is visible.
          {lastChecked && ` Last checked: ${lastChecked}.`}
        </p>

        {loading && <p role="status">Checking your timetable…</p>}

        {error && (
          <p className="allocation-error" role="alert">
            {error}
          </p>
        )}

        {!loading && data && (
          <>
            {data.publication && (
              <div className="personal-timetable-publication">
                <strong>{data.publication.name}</strong>

                <p>
                  Published:{' '}
                  {new Date(
                    data.publication.publishedAt,
                  ).toLocaleString()}
                </p>
              </div>
            )}

            <p
              className={
                data.status === 'PROFILE_DISABLED'
                  ? 'allocation-error'
                  : 'allocation-notice'
              }
              role="status"
            >
              {data.message}
            </p>

            {data.status === 'AVAILABLE' && (
              <>
                <div className="allocation-draft-form timetable-filter">
                  <label htmlFor="timetable-day">
                    Filter by day
                  </label>

                  <select
                    id="timetable-day"
                    value={selectedDay}
                    onChange={(event) =>
                      setSelectedDay(Number(event.target.value))
                    }
                  >
                    <option value={0}>All days</option>

                    {weekdays.slice(1).map((day, index) => (
                      <option key={day} value={index + 1}>
                        {day}
                      </option>
                    ))}
                  </select>
                </div>

                <p>
                  {sessions.length}{' '}
                  {sessions.length === 1 ? 'session' : 'sessions'}
                  {selectedDay !== 0
                    ? ` on ${weekdays[selectedDay]}`
                    : ' this week'}
                </p>

                {sessions.length === 0 ? (
                  <p>No classes are scheduled for the selected day.</p>
                ) : (
                  <div className="allocation-table-scroll">
                    <table className="allocation-table">
                      <caption>
                        {selectedDay === 0
                          ? 'Your published weekly classes'
                          : `Your published classes for ${weekdays[selectedDay]}`}
                      </caption>

                      <thead>
                        <tr>
                          <th scope="col">Day</th>
                          <th scope="col">Time</th>
                          <th scope="col">Subject</th>
                          <th scope="col">Room</th>
                          <th scope="col">Teacher</th>
                          <th scope="col">Section</th>
                        </tr>
                      </thead>

                      <tbody>
                        {sessions.map((session) => (
                          <tr key={session.id}>
                            <td>{weekdays[session.dayOfWeek]}</td>

                            <td>
                              {session.startTime.slice(0, 5)}–
                              {session.endTime.slice(0, 5)}
                            </td>

                            <td>{session.subjectName}</td>
                            <td>{session.roomCode}</td>
                            <td>{session.teacherName}</td>
                            <td>{session.sectionCode}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </>
            )}
          </>
        )}
      </section>
    </main>
  )
}