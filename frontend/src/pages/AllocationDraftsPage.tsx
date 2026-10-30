import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import {
  getAllocationDraft,
  getAllocationDrafts,
} from '../services/allocationDraftReviewService'
import type {
  DraftDetail,
  DraftSummary,
} from '../services/allocationDraftReviewService'
import '../styles/allocation.css'
import DraftValidationPanel from '../components/allocation/DraftValidationPanel'
import DraftPublicationPanel from '../components/allocation/DraftPublicationPanel'

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

function formatDate(value: string) {
  return new Date(value).toLocaleString()
}

export default function AllocationDraftsPage() {
  const [drafts, setDrafts] = useState<DraftSummary[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [refresh, setRefresh] = useState(0)

  const [loading, setLoading] = useState(true)
  const [listError, setListError] = useState('')

  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [detail, setDetail] = useState<DraftDetail | null>(null)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [detailError, setDetailError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function loadDrafts() {
      setLoading(true)
      setListError('')

      try {
        const result = await getAllocationDrafts(
          page,
          controller.signal,
        )

        if (controller.signal.aborted) return

        setDrafts(result.content)
        setTotalPages(result.totalPages)
        setTotalElements(result.totalElements)
      } catch (error) {
        if (controller.signal.aborted) return

        setDrafts([])
        setListError(
          error instanceof Error
            ? error.message
            : 'Unable to load saved drafts.',
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void loadDrafts()

    return () => controller.abort()
  }, [page, refresh])

  useEffect(() => {
    if (selectedId === null) {
      setDetail(null)
      setDetailError('')
      setLoadingDetail(false)
      return
    }

    const controller = new AbortController()
    const draftId = selectedId

    async function loadDetail() {
      setLoadingDetail(true)
      setDetailError('')
      setDetail(null)

      try {
        const result = await getAllocationDraft(
          draftId,
          controller.signal,
        )

        if (!controller.signal.aborted) {
          setDetail(result)
        }
      } catch (error) {
        if (!controller.signal.aborted) {
          setDetailError(
            error instanceof Error
              ? error.message
              : 'Unable to load the selected draft.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoadingDetail(false)
        }
      }
    }

    void loadDetail()

    return () => controller.abort()
  }, [selectedId, refresh])

  return (
    <main className="allocation-page">
      <Link className="allocation-back" to="/dashboard">
        ← Back to dashboard
      </Link>

      <header className="allocation-heading">
        <p className="allocation-eyebrow">REVIEW YOUR PLANNING</p>
        <h1>Saved allocation drafts</h1>
        <p>
          Open a saved draft to review its assigned and missing sessions.
        </p>
      </header>

      <div className="allocation-actions">
        <Link
          className="allocation-button allocation-primary"
          to="/allocations"
        >
          Create a new draft →
        </Link>

        <button
          className="allocation-button"
          type="button"
          disabled={loading || loadingDetail}
          onClick={() => setRefresh((value) => value + 1)}
        >
          Refresh
        </button>
      </div>

      <section
        className="allocation-panel"
        aria-labelledby="saved-drafts-heading"
      >
        <h2 id="saved-drafts-heading">Your saved drafts</h2>

        {listError && (
          <p className="allocation-error" role="alert">
            {listError}
          </p>
        )}

        {loading ? (
          <p role="status">Loading saved drafts…</p>
        ) : !listError && drafts.length === 0 ? (
          <p>No drafts are available on this page.</p>
        ) : !listError ? (
          <>
            <p>{totalElements} saved drafts</p>

            <div className="allocation-table-scroll">
              <table className="allocation-table">
                <caption>Saved draft list</caption>

                <thead>
                  <tr>
                    <th scope="col">ID</th>
                    <th scope="col">Draft name</th>
                    <th scope="col">Created</th>
                    <th scope="col">Action</th>
                  </tr>
                </thead>

                <tbody>
                  {drafts.map((draft) => (
                    <tr key={draft.id}>
                      <td>#{draft.id}</td>
                      <td>{draft.name}</td>
                      <td>{formatDate(draft.createdAt)}</td>
                      <td>
                        <button
                          className="allocation-button"
                          type="button"
                          aria-label={`Open draft ${draft.name}`}
                          onClick={() => {
                            setDetail(null)
                            setDetailError('')
                            setSelectedId(draft.id)
                          }}
                          disabled={selectedId === draft.id}
                        >
                          {selectedId === draft.id
                            ? 'Selected'
                            : 'Open draft'}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        ) : null}

        <div className="allocation-pagination">
          <button
            className="allocation-button"
            type="button"
            disabled={loading || page === 0}
            onClick={() => setPage((value) => value - 1)}
          >
            Previous
          </button>

          <span>
            Page {page + 1} of {Math.max(1, totalPages)}
          </span>

          <button
            className="allocation-button"
            type="button"
            disabled={loading || page + 1 >= totalPages}
            onClick={() => setPage((value) => value + 1)}
          >
            Next
          </button>
        </div>
      </section>

      {selectedId !== null && (
        <section
          className="allocation-panel"
          aria-labelledby="draft-review-heading"
          aria-busy={loadingDetail}
        >
          <div className="allocation-section-heading">
            <h2 id="draft-review-heading">
              Draft #{selectedId}
            </h2>

            <button
              className="allocation-button"
              type="button"
              onClick={() => setSelectedId(null)}
            >
              Close review
            </button>
          </div>

          {loadingDetail && (
            <p role="status">Loading saved sessions…</p>
          )}

          {detailError && (
            <p className="allocation-error" role="alert">
              {detailError}
            </p>
          )}

          {detail && (
            <>
              <h3>{detail.name}</h3>

<DraftValidationPanel
  key={`${detail.id}-${detail.version}`}
  draftId={detail.id}
  version={detail.version}
/>
<DraftPublicationPanel
  key={`publication-${detail.id}-${detail.version}`}
  draftId={detail.id}
  version={detail.version}
/>

<p>Created: {formatDate(detail.createdAt)}</p>
              

              <p className="allocation-result-summary">
                {detail.requestedSessions} requested ·{' '}
                {detail.allocatedSessions} allocated ·{' '}
                {detail.unallocatedSessions} unallocated
              </p>

              <p className="allocation-notice">
                This is a saved draft. Teachers and Students cannot
                view it as a published timetable.
              </p>

              <div className="allocation-table-scroll">
                <table className="allocation-table">
                  <caption>Requirement completion</caption>

                  <thead>
                    <tr>
                      <th scope="col">Requirement ID</th>
                      <th scope="col">Requested sessions</th>
                      <th scope="col">Periods/session</th>
                      <th scope="col">Allocated</th>
                      <th scope="col">Missing</th>
                    </tr>
                  </thead>

                  <tbody>
                    {detail.requirements.map((requirement) => (
                      <tr key={requirement.requirementId}>
                        <td>#{requirement.requirementId}</td>
                        <td>{requirement.sessionsRequested}</td>
                        <td>{requirement.periodsPerSession}</td>
                        <td>{requirement.allocatedSessions}</td>
                        <td>{requirement.unallocatedSessions}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {detail.sessions.length === 0 ? (
                <p>No classroom sessions were allocated in this draft.</p>
              ) : (
                <div className="allocation-table-scroll">
                  <table className="allocation-table">
                    <caption>Saved classroom assignments</caption>

                    <thead>
                      <tr>
                        <th scope="col">Requirement ID</th>
                        <th scope="col">Session</th>
                        <th scope="col">Day</th>
                        <th scope="col">Time</th>
                        <th scope="col">Room ID</th>
                        <th scope="col">Teacher ID</th>
                        <th scope="col">Section ID</th>
                      </tr>
                    </thead>

                    <tbody>
                      {detail.sessions.map((session) => (
                        <tr key={session.id}>
                          <td>#{session.requirementId}</td>
                          <td>{session.sessionNumber}</td>
                          <td>{weekdays[session.dayOfWeek]}</td>
                          <td>
                            {session.startTime.slice(0, 5)}–
                            {session.endTime.slice(0, 5)}
                          </td>
                          <td>#{session.roomId}</td>
                          <td>#{session.teacherId}</td>
                          <td>#{session.sectionId}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {!detail.allSessionsAllocated && (
                <p className="allocation-error">
                  This draft has incomplete allocations. Review room
                  suitability, teaching periods and workload limits
                  before creating a replacement draft.
                </p>
              )}
            </>
          )}
        </section>
      )}
    </main>
  )
}