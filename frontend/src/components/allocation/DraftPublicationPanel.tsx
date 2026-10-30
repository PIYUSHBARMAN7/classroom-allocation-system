import { useEffect, useRef, useState } from 'react'
import { getDraftValidation } from '../../services/allocationDraftReviewService'
import type { DraftValidation } from '../../services/allocationDraftReviewService'
import {
  getActivePublication,
  PublicationError,
  publishTimetable,
} from '../../services/timetablePublicationService'
import type { TimetablePublication } from '../../services/timetablePublicationService'

type Props = {
  draftId: number
  version: number
}

export default function DraftPublicationPanel({
  draftId,
  version,
}: Props) {
  const [active, setActive] = useState<TimetablePublication | null>(null)
  const [validation, setValidation] = useState<DraftValidation | null>(null)

  const [loading, setLoading] = useState(true)
  const [publishing, setPublishing] = useState(false)
  const [accepted, setAccepted] = useState(false)
  const [refresh, setRefresh] = useState(0)

  const [error, setError] = useState('')
  const [publicationErrors, setPublicationErrors] = useState<string[]>([])
  const [success, setSuccess] = useState('')

  const publishController = useRef<AbortController | null>(null)

  useEffect(() => {
    const controller = new AbortController()

    async function loadChecks() {
      setLoading(true)
      setValidation(null)
      setAccepted(false)
      setError('')
      setPublicationErrors([])
      setSuccess('')

      try {
        const [currentPublication, checks] = await Promise.all([
          getActivePublication(controller.signal),
          getDraftValidation(draftId, controller.signal),
        ])

        if (controller.signal.aborted) return

        if (checks.draftId !== draftId || checks.version !== version) {
          throw new Error(
            'The draft changed. Refresh the saved draft before publishing.',
          )
        }

        setActive(currentPublication)
        setValidation(checks)
      } catch (err) {
        if (!controller.signal.aborted) {
          setError(
            err instanceof Error
              ? err.message
              : 'Unable to load publication checks.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void loadChecks()

    return () => controller.abort()
  }, [draftId, version, refresh])

  useEffect(() => {
    return () => publishController.current?.abort()
  }, [])

  const alreadyActive =
    active?.sourceDraftId === draftId &&
    active.sourceDraftVersion === version

  async function handlePublish() {
    if (
      publishController.current !== null ||
      loading ||
      !validation?.valid ||
      !accepted ||
      alreadyActive
    ) {
      return
    }

    const controller = new AbortController()
    publishController.current = controller

    setPublishing(true)
    setError('')
    setPublicationErrors([])
    setSuccess('')

    try {
      const result = await publishTimetable(
        draftId,
        version,
        active?.id ?? null,
        controller.signal,
      )

      if (!controller.signal.aborted) {
        setActive(result)
        setAccepted(false)
        setSuccess(
          `Published “${result.name}” with ${result.sessionCount} sessions.`,
        )
      }
    } catch (err) {
      if (!controller.signal.aborted) {
        setAccepted(false)
        setValidation(null)

        if (err instanceof PublicationError) {
          setError(err.message)
          setPublicationErrors(err.errors)
        } else {
          setError(
            'Unable to confirm publication. Refresh the checks to see the current active timetable.',
          )
        }
      }
    } finally {
      if (publishController.current === controller) {
        publishController.current = null
      }

      if (!controller.signal.aborted) {
        setPublishing(false)
      }
    }
  }

  return (
    <section
      className="draft-validation"
      aria-label="Timetable publication"
      aria-busy={loading || publishing}
    >
      <h3>Publish campus timetable</h3>

      <p>
        Publishing makes this draft the active campus timetable.
        Include every class intended for the active schedule.
      </p>

      <button
        className="allocation-button"
        type="button"
        disabled={loading || publishing}
        onClick={() => setRefresh((value) => value + 1)}
      >
        {loading ? 'Checking…' : 'Refresh publication checks'}
      </button>

      {loading && (
        <p role="status">Loading the active timetable and draft checks…</p>
      )}

      {!loading && validation && (
        <>
          {active ? (
            <p>
              <strong>Current active timetable:</strong>{' '}
              {active.name} · Publication #{active.id} ·{' '}
              {active.sessionCount} sessions
            </p>
          ) : (
            <p>No campus timetable is currently published.</p>
          )}

          {alreadyActive ? (
            <p className="allocation-success" role="status">
              This draft is already the active timetable.
            </p>
          ) : !validation.valid ? (
            <div className="allocation-error">
              <strong>Fix these issues before publishing:</strong>

              <ul>
                {validation.errors.map((message, index) => (
                  <li key={`${index}-${message}`}>
                    {message}
                  </li>
                ))}
              </ul>
            </div>
          ) : (
            <>
              <label className="publication-confirmation">
                <input
                  type="checkbox"
                  checked={accepted}
                  disabled={publishing}
                  onChange={(event) =>
                    setAccepted(event.target.checked)
                  }
                />

                <span>
                  {active
                    ? `I have reviewed this complete campus schedule and want it to replace “${active.name}”. The previous timetable will remain in history.`
                    : 'I have reviewed this complete campus schedule and want to publish it.'}
                </span>
              </label>

              <button
                className="allocation-button allocation-primary"
                type="button"
                disabled={!accepted || publishing}
                onClick={() => void handlePublish()}
              >
                {publishing
                  ? 'Publishing…'
                  : active
                    ? 'Replace active timetable'
                    : 'Publish timetable'}
              </button>
            </>
          )}
        </>
      )}

      {error && (
        <div className="allocation-error" role="alert">
          <p>{error}</p>

          {publicationErrors.length > 0 && (
            <ul>
              {publicationErrors.map((message, index) => (
                <li key={`${index}-${message}`}>
                  {message}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {success && (
        <p className="allocation-success" role="status">
          {success}
        </p>
      )}
    </section>
  )
}