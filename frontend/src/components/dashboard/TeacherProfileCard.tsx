import { useEffect, useState } from 'react'
import { getMyTeacherProfile } from '../../services/teacherWorkspaceService'
import type { MyTeacherProfileResponse } from '../../services/teacherWorkspaceService'

export default function TeacherProfileCard() {
  const [data, setData] = useState<MyTeacherProfileResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [refresh, setRefresh] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function loadProfile() {
      setLoading(true)
      setError('')

      try {
        const result = await getMyTeacherProfile(controller.signal)

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(
            err instanceof Error
              ? err.message
              : 'Unable to load your teaching profile.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void loadProfile()

    return () => controller.abort()
  }, [refresh])

  const teacher = data?.teacher

  return (
    <section
      className="dashboard-card dashboard-account"
      aria-labelledby="teacher-profile-heading"
      aria-busy={loading}
    >
      <span className="dashboard-label">YOUR TEACHING DETAILS</span>
      <h2 id="teacher-profile-heading">My teaching profile</h2>

      {loading ? (
        <p role="status">Loading your teaching profile…</p>
      ) : error ? (
        <p className="dashboard-error" role="alert">
          {error}
        </p>
      ) : teacher ? (
        <>
          {!teacher.enabled && (
            <p className="dashboard-error" role="status">
              Your teacher record is currently disabled.
              Contact your institution administrator.
            </p>
          )}

          <dl className="dashboard-details">
            <div>
              <dt>Name</dt>
              <dd>{teacher.fullName}</dd>
            </div>

            <div>
              <dt>Employee code</dt>
              <dd>{teacher.employeeCode}</dd>
            </div>

            <div>
              <dt>Department</dt>
              <dd>{teacher.department}</dd>
            </div>

            <div>
              <dt>Daily teaching limit</dt>
              <dd>{teacher.maxPeriodsPerDay} periods</dd>
            </div>

            <div>
              <dt>Weekly teaching limit</dt>
              <dd>{teacher.maxPeriodsPerWeek} periods</dd>
            </div>
          </dl>
        </>
      ) : (
        <p>
          Your account is not linked to a teacher record yet.
          Contact your institution administrator.
        </p>
      )}

      <button
        className="neo-button yellow"
        type="button"
        disabled={loading}
        onClick={() => setRefresh((value) => value + 1)}
      >
        {loading ? 'Loading…' : 'Refresh teaching profile'}
      </button>
    </section>
  )
}