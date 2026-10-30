import { useEffect, useState } from 'react'
import { getMySection } from '../../services/studentWorkspaceService'
import type { MySectionResponse } from '../../services/studentWorkspaceService'

export default function StudentSectionCard() {
  const [data, setData] = useState<MySectionResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [refresh, setRefresh] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function loadSection() {
      setLoading(true)
      setError('')

      try {
        const result = await getMySection(controller.signal)

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(
            err instanceof Error
              ? err.message
              : 'Unable to load your section.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void loadSection()

    return () => controller.abort()
  }, [refresh])

  const section = data?.section

  return (
    <section
      className="dashboard-card dashboard-account"
      aria-labelledby="my-section-heading"
      aria-busy={loading}
    >
      <span className="dashboard-label">YOUR ACADEMIC DETAILS</span>
      <h2 id="my-section-heading">My section</h2>

      {loading ? (
        <p role="status">Loading your section…</p>
      ) : error ? (
        <p className="dashboard-error" role="alert">
          {error}
        </p>
      ) : section ? (
        <>
          {!section.enabled && (
            <p className="dashboard-error" role="status">
              Your assigned section is currently disabled.
              Contact your institution administrator.
            </p>
          )}

          <dl className="dashboard-details">
            <div>
              <dt>Section</dt>
              <dd>
                {section.code} · {section.name}
              </dd>
            </div>

            <div>
              <dt>Programme</dt>
              <dd>{section.programCode}</dd>
            </div>

            <div>
              <dt>Semester</dt>
              <dd>{section.semester}</dd>
            </div>

            <div>
              <dt>Academic year</dt>
              <dd>
                {section.academicYearStart}–
                {section.academicYearStart + 1}
              </dd>
            </div>
          </dl>
        </>
      ) : (
        <p>
          Your account has no section assigned yet.
          Contact your institution administrator.
        </p>
      )}

      <button
        className="neo-button yellow"
        type="button"
        disabled={loading}
        onClick={() => setRefresh((value) => value + 1)}
      >
        {loading ? 'Loading…' : 'Refresh section'}
      </button>
    </section>
  )
}