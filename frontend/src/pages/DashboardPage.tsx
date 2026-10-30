import { useEffect, useState } from 'react'
import { Link, useNavigate, useOutletContext } from 'react-router'

import { signOut } from '../services/authService'
import type { CurrentUser } from '../services/authService'
import { getClassrooms } from '../services/classroomService'
import { getTeachers } from '../services/teacherService'
import StudentSectionCard from '../components/dashboard/StudentSectionCard'
import TeacherProfileCard from '../components/dashboard/TeacherProfileCard'
import '../styles/dashboard.css'

const roleLabels: Record<CurrentUser['role'], string> = {
  ADMIN: 'Administrator',
  COORDINATOR: 'Coordinator',
  TEACHER: 'Teacher',
  STUDENT: 'Student',
}

const introductions: Record<CurrentUser['role'], string> = {
  ADMIN: 'Manage campus resources, institution accounts and planning.',
  COORDINATOR: 'Keep campus resources organised and ready for allocation.',
  TEACHER: 'Your teaching workspace and account information.',
  STUDENT: 'Your campus workspace and account information.',
}

type Counts = {
  rooms: number | null
  activeRooms: number | null
  teachers: number | null
  activeTeachers: number | null
}

const emptyCounts: Counts = {
  rooms: null,
  activeRooms: null,
  teachers: null,
  activeTeachers: null,
}

export default function DashboardPage() {
  const user = useOutletContext<CurrentUser>()
  const navigate = useNavigate()

  const canManage =
    user.role === 'ADMIN' || user.role === 'COORDINATOR'

  const [menuOpen, setMenuOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const [counts, setCounts] = useState<Counts>(emptyCounts)
  const [loading, setLoading] = useState(canManage)
  const [summaryError, setSummaryError] = useState('')
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null)
  const [refresh, setRefresh] = useState(0)

  const initials =
    user.displayName
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((part) => part.charAt(0))
      .join('')
      .toUpperCase() || 'U'

  useEffect(() => {
    if (!canManage) return

    const controller = new AbortController()

    async function loadSummary() {
      setLoading(true)
      setSummaryError('')

      const results = await Promise.allSettled([
        getClassrooms(0, undefined, controller.signal),
        getClassrooms(0, true, controller.signal),
        getTeachers(0, undefined, controller.signal),
        getTeachers(0, true, controller.signal),
      ])

      if (controller.signal.aborted) return

      const sessionExpired = results.some(
        (result) =>
          result.status === 'rejected' &&
          result.reason?.status === 401,
      )

      if (sessionExpired) {
        void navigate('/login', { replace: true })
        return
      }

      const totals = results.map((result) =>
        result.status === 'fulfilled'
          ? result.value.totalElements
          : null,
      )

      setCounts({
        rooms: totals[0] ?? null,
        activeRooms: totals[1] ?? null,
        teachers: totals[2] ?? null,
        activeTeachers: totals[3] ?? null,
      })

      if (results.some((result) => result.status === 'rejected')) {
        setSummaryError(
          'Some totals could not be loaded. Check your connection and refresh.',
        )
      } else {
        setLastUpdated(new Date())
      }

      setLoading(false)
    }

    void loadSummary()

    return () => controller.abort()
  }, [canManage, refresh, navigate])

  useEffect(() => {
    if (!canManage) return

    function refreshWhenVisible() {
      if (!document.hidden) {
        setRefresh((value) => value + 1)
      }
    }

    const timer = window.setInterval(refreshWhenVisible, 60_000)

    window.addEventListener('focus', refreshWhenVisible)

    return () => {
      window.clearInterval(timer)
      window.removeEventListener('focus', refreshWhenVisible)
    }
  }, [canManage])

  async function handleSignOut() {
    if (busy) return

    setBusy(true)
    setError('')

    try {
      await signOut()
      void navigate('/login', { replace: true })
    } catch {
      setError('Unable to sign out. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  const metrics = [
    {
      title: 'Total classrooms',
      value: counts.rooms,
      detail: 'All registered rooms',
      colour: 'yellow',
    },
    {
      title: 'Active classrooms',
      value: counts.activeRooms,
      detail: 'Marked active for planning',
      colour: 'blue',
    },
    {
      title: 'Total teachers',
      value: counts.teachers,
      detail: 'All teacher profiles',
      colour: 'pink',
    },
    {
      title: 'Active teachers',
      value: counts.activeTeachers,
      detail: 'Marked active for planning',
      colour: 'green',
    },
  ]

  return (
    <div className="dashboard-page db-shell">
      <a className="db-skip-link" href="#dashboard-content">
        Skip to dashboard content
      </a>

      <aside
        id="dashboard-navigation"
        className={`db-sidebar ${menuOpen ? 'is-open' : ''}`}
        aria-label="Workspace navigation"
      >
        <Link className="db-brand" to="/">
          <span className="db-brand-icon" aria-hidden="true">
            CA
          </span>
          <span>
            CLASSROOM
            <br />
            ALLOCATOR
          </span>
        </Link>

        <p className="db-nav-heading">WORKSPACE</p>

        <nav className="db-navigation" aria-label="Main">
          <Link
            className="db-nav-link is-current"
            to="/dashboard"
            aria-current="page"
            onClick={() => setMenuOpen(false)}
          >
            <span aria-hidden="true">01</span>
            Overview
          </Link>

          {canManage && (
            <>
              <Link className="db-nav-link" to="/classrooms">
                <span aria-hidden="true">02</span>
                Classrooms
              </Link>

              <Link className="db-nav-link" to="/teachers">
                <span aria-hidden="true">03</span>
                Teachers
              </Link>
              <Link className="db-nav-link" to="/subjects">
                <span aria-hidden="true">SB</span>
                Subjects
              </Link>
              <Link className="db-nav-link" to="/sections">
                <span aria-hidden="true">SC</span>
                Sections
              </Link>
              <Link className="db-nav-link" to="/time-slots">
                <span aria-hidden="true">TS</span>
                Time slots
              </Link>
              <Link className="db-nav-link" to="/requirements">
                <span aria-hidden="true">CR</span>
                Class requirements
              </Link>
              <Link className="neo-button yellow" to="/allocations">
                Allocation preview →
              </Link>
            </>
          )}



          {(user.role === 'ADMIN' || user.role === 'COORDINATOR') && (
            <Link className="neo-button yellow" to="/allocation-drafts">
              Saved drafts →
            </Link>
          )}

          {user.role === 'ADMIN' && (
            <Link
              className="neo-button yellow"
              to="/student-section-assignments"
            >
              Assign Student sections →
            </Link>
          )}

          {user.role === 'ADMIN' && (
            <Link className="db-nav-link" to="/users">
              <span aria-hidden="true">04</span>
              Create account
            </Link>
          )}

          <Link className="db-nav-link" to="/change-password">
            <span aria-hidden="true">PW</span>
            Change password
          </Link>
          {(user.role === 'TEACHER' || user.role === 'STUDENT') && (
            <Link className="neo-button yellow" to="/my-timetable">
              My timetable →
            </Link>
          )}
        </nav>

        <div className="db-sidebar-note">
          <strong>Everything in its place.</strong>
          <p>Rooms, people and better campus planning.</p>
        </div>

        <div className="db-sidebar-account">
          <span className="db-avatar" aria-hidden="true">
            {initials}
          </span>

          <div>
            <strong>{user.displayName}</strong>
            <span>{roleLabels[user.role]}</span>
          </div>
        </div>
      </aside>

      <div className="db-workspace">
        <header className="db-topbar">
          <div className="db-topbar-title">
            <button
              className="db-button db-menu-button"
              type="button"
              aria-expanded={menuOpen}
              aria-controls="dashboard-navigation"
              onClick={() => setMenuOpen((value) => !value)}
            >
              {menuOpen ? 'Close menu' : 'Menu'}
            </button>

            <span>Workspace / Overview</span>
          </div>

          <div className="db-topbar-actions">
            <span className="db-role-badge">
              {roleLabels[user.role]}
            </span>

            <button
              className="db-button"
              type="button"
              disabled={busy}
              onClick={() => void handleSignOut()}
            >
              {busy ? 'Signing out…' : 'Sign out'}
            </button>
          </div>
        </header>

        <main id="dashboard-content" className="db-main">
          {error && (
            <p className="db-alert" role="alert">
              {error}
            </p>
          )}

          <section className="db-welcome">
            <div>
              <p className="db-eyebrow">
                {roleLabels[user.role].toUpperCase()} WORKSPACE
              </p>

              <h1>Welcome, {user.displayName}.</h1>
              <p>{introductions[user.role]}</p>

              <div className="db-welcome-actions">
                {canManage ? (
                  <Link
                    className="db-button db-button-yellow"
                    to="/classrooms"
                  >
                    Manage classrooms ↗
                  </Link>
                ) : (
                  <Link
                    className="db-button db-button-yellow"
                    to="/change-password"
                  >
                    Account security ↗
                  </Link>
                )}

                {canManage && (
                  <Link className="db-text-link" to="/teachers">
                    Open teacher directory →
                  </Link>
                )}
              </div>
            </div>

            <div className="db-welcome-art" aria-hidden="true">
              <span className="db-art-label">CAMPUS</span>
              <div className="db-art-building">
                <span />
                <span />
                <span />
                <span />
                <span />
                <span />
              </div>
              <strong>MAKE ROOM.</strong>
            </div>
          </section>

          {canManage && (
            <section aria-labelledby="campus-overview-title">
              <div className="db-section-heading">
                <div>
                  <h2 id="campus-overview-title">Campus overview</h2>
                  <p>
                    {loading
                      ? 'Refreshing totals…'
                      : lastUpdated
                        ? `Last refreshed ${lastUpdated.toLocaleTimeString(
                          [],
                          { hour: '2-digit', minute: '2-digit' },
                        )}`
                        : 'Totals have not loaded yet.'}
                  </p>
                </div>

                <button
                  className="db-button"
                  type="button"
                  disabled={loading}
                  onClick={() => setRefresh((value) => value + 1)}
                >
                  {loading ? 'Refreshing…' : 'Refresh'}
                </button>
              </div>

              {summaryError && (
                <p className="db-alert" role="alert">
                  {summaryError}
                </p>
              )}

              <div className="db-metrics" aria-busy={loading}>
                {metrics.map((metric) => (
                  <article
                    key={metric.title}
                    className={`db-metric db-${metric.colour}`}
                  >
                    <h3>{metric.title}</h3>
                    <strong className="db-metric-value">
                      {metric.value === null ? '—' : metric.value}
                    </strong>
                    <p>
                      {metric.value === null && !loading
                        ? 'Currently unavailable'
                        : metric.detail}
                    </p>
                  </article>
                ))}
              </div>

              <p className="db-small-note">
                Totals refresh every minute while this tab is visible.
                Active rooms are not necessarily free at a particular time.
              </p>
            </section>
          )}

          <div className="db-bottom-grid">
            <section className="db-panel">
              <div className="db-panel-heading">
                <h2>Quick actions</h2>
                <span className="db-chip">YOUR WORKSPACE</span>
              </div>

              <div className="db-quick-actions">
                {canManage && (
                  <>
                    <Link className="db-action-card" to="/classrooms">
                      <span className="db-action-icon db-yellow">
                        RM
                      </span>
                      <span>
                        <strong>Classroom directory</strong>
                        <small>Add rooms and manage facilities.</small>
                      </span>
                      <span aria-hidden="true">↗</span>
                    </Link>

                    <Link className="db-action-card" to="/teachers">
                      <span className="db-action-icon db-pink">TR</span>
                      <span>
                        <strong>Teacher directory</strong>
                        <small>Manage profiles and workload limits.</small>
                      </span>
                      <span aria-hidden="true">↗</span>
                    </Link>
                  </>
                )}

                {user.role === 'ADMIN' && (
                  <Link className="db-action-card" to="/users">
                    <span className="db-action-icon db-blue">AC</span>
                    <span>
                      <strong>Create an account</strong>
                      <small>Assign institution account access.</small>
                    </span>
                    <span aria-hidden="true">↗</span>
                  </Link>
                )}

                <Link className="db-action-card" to="/change-password">
                  <span className="db-action-icon db-green">PW</span>
                  <span>
                    <strong>Change password</strong>
                    <small>Update your account password.</small>
                  </span>
                  <span aria-hidden="true">↗</span>
                </Link>
              </div>
            </section>

            <section className="db-panel db-profile-panel">
              <div className="db-panel-heading">
                <h2>Your profile</h2>
                <span className="db-avatar" aria-hidden="true">
                  {initials}
                </span>
              </div>

              <dl className="db-profile-details">
                <div>
                  <dt>Name</dt>
                  <dd>{user.displayName}</dd>
                </div>
                <div>
                  <dt>Institution email</dt>
                  <dd>{user.email}</dd>
                </div>
                <div>
                  <dt>Account role</dt>
                  <dd>{roleLabels[user.role]}</dd>
                </div>
              </dl>
            </section>
          </div>

          <section className="db-panel db-upcoming">
            <div>
              <span className="db-chip">COMING NEXT</span>
              <h2>
                {canManage
                  ? 'From campus data to a clear timetable.'
                  : 'Your published timetable will appear here.'}
              </h2>
              <p>
                {canManage
                  ? 'Subjects, sections, time slots and allocation tools are still being built.'
                  : 'Timetable access is still being built. Your schedule will be connected to your assigned account or section.'}
              </p>
            </div>

            <span className="db-upcoming-tag">Timetable in progress</span>
          </section>

          <footer className="db-footer">
            <span>CLASSROOM ALLOCATOR</span>
            <Link to="/">Visit home page ↗</Link>
          </footer>
          {user.role === 'STUDENT' && <StudentSectionCard />}
          {user.role === 'TEACHER' && <TeacherProfileCard />}
        </main>
      </div>
    </div>
  )
}