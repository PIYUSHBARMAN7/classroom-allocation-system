import { useEffect, useState } from 'react'
import { getDraftValidation } from '../../services/allocationDraftReviewService'
import type { DraftValidation } from '../../services/allocationDraftReviewService'

type Props = {
  draftId: number
  version: number
}

export default function DraftValidationPanel({
  draftId,
  version,
}: Props) {
  const [validation, setValidation] =
    useState<DraftValidation | null>(null)

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [refresh, setRefresh] = useState(0)

  useEffect(() => {
    const controller = new AbortController()

    async function loadValidation() {
      setLoading(true)
      setValidation(null)
      setError('')

      try {
        const result = await getDraftValidation(
          draftId,
          controller.signal,
        )

        if (controller.signal.aborted) return

        if (result.draftId !== draftId || result.version !== version) {
          throw new Error(
            'The draft has changed. Refresh the saved draft before checking it again.',
          )
        }

        setValidation(result)
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(
            err instanceof Error
              ? err.message
              : 'Unable to check this draft.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void loadValidation()

    return () => controller.abort()
  }, [draftId, version, refresh])

  return (
    <section
      className="draft-validation"
      aria-label="Saved schedule validation"
      aria-busy={loading}
    >
      <div className="allocation-section-heading">
        <h3>Saved schedule checks</h3>

        <button
          className="allocation-button"
          type="button"
          disabled={loading}
          onClick={() => setRefresh((value) => value + 1)}
        >
          {loading ? 'Checking…' : 'Check again'}
        </button>
      </div>

      {loading && (
        <p role="status">
          Checking saved sessions against current campus information…
        </p>
      )}

      {error && (
        <p className="allocation-error" role="alert">
          {error}
        </p>
      )}

      {!loading && validation && (
        validation.valid ? (
          <div className="allocation-success" role="status">
            <strong>Schedule checks passed.</strong>
            <p>
              The draft passed the current completeness, room,
              timetable conflict and workload checks.
              It remains unpublished.
            </p>
          </div>
        ) : (
          <div className="allocation-error">
            <p role="status">
              <strong>This draft needs attention.</strong>
            </p>

            <ul>
              {validation.errors.map((message, index) => (
                <li key={`${index}-${message}`}>
                  {message}
                </li>
              ))}
            </ul>
          </div>
        )
      )}

      <p className="draft-validation-note">
        These checks do not yet include availability blocks or
        conflicts with other published timetables.
      </p>
    </section>
  )
}