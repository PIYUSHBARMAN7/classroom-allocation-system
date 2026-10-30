import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router'

import {
  TeacherApiError,
  getAvailableTeacherAccounts,
  linkTeacherAccount,
} from '../../services/teacherService'

import type {
  Teacher,
  TeacherAccountPage,
} from '../../types/teacher'

type Props = {
  teacher: Teacher
  onLinked: (teacher: Teacher) => void
  onClose: () => void
}

export default function TeacherAccountLinker({
  teacher,
  onLinked,
  onClose,
}: Props) {
  const navigate = useNavigate()
  const lock = useRef(false)
  const headingRef = useRef<HTMLHeadingElement>(null)

  const [data, setData] = useState<TeacherAccountPage | null>(null)
  const [page, setPage] = useState(0)
  const [selectedId, setSelectedId] = useState('')
  const [refresh, setRefresh] = useState(0)

  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [loadError, setLoadError] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    headingRef.current?.focus()
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      setLoading(true)
      setLoadError('')
      setData(null)

      try {
        const result = await getAvailableTeacherAccounts(
          page,
          controller.signal,
        )

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (controller.signal.aborted) return

        if (err instanceof TeacherApiError && err.status === 401) {
          void navigate('/login', { replace: true })
          return
        }

        setLoadError(
          err instanceof Error
            ? err.message
            : 'Unable to load available accounts.',
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void load()

    return () => controller.abort()
  }, [page, refresh, navigate])

  const selectedAccount = data?.content.find(
    (account) => String(account.id) === selectedId,
  )

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (lock.current || loading || !selectedAccount) return

    lock.current = true
    setBusy(true)
    setError('')

    try {
      const updated = await linkTeacherAccount(
        teacher.id,
        selectedAccount.id,
        teacher.version,
      )

      onLinked(updated)
    } catch (err) {
      if (err instanceof TeacherApiError && err.status === 401) {
        void navigate('/login', { replace: true })
        return
      }

      setError(
        err instanceof TypeError
          ? 'Could not confirm the change. Close this panel and reload the teacher directory before retrying.'
          : err instanceof Error
            ? err.message
            : 'Unable to link the account.',
      )
    } finally {
      lock.current = false
      setBusy(false)
    }
  }

  function changePage(nextPage: number) {
    setSelectedId('')
    setError('')
    setPage(nextPage)
  }

  return (
    <section
      className="rooms-panel"
      aria-labelledby="teacher-account-title"
      aria-busy={loading || busy}
    >
      <h2
        id="teacher-account-title"
        ref={headingRef}
        tabIndex={-1}
      >
        Link account: {teacher.fullName}
      </h2>

      <p>
        Choose the institution account belonging to this teacher.
        Only enabled, unlinked Teacher accounts appear.
      </p>

      {teacher.userId !== null && (
        <p className="rooms-message">
          This profile already has a linked account. Saving a different
          account will replace that association.
        </p>
      )}

      {loadError && (
        <p className="rooms-message rooms-error" role="alert">
          {loadError}
        </p>
      )}

      {error && (
        <p className="rooms-message rooms-error" role="alert">
          {error}
        </p>
      )}

      {loading && <p role="status">Loading available accounts…</p>}

      {!loading && data && (
        <>
          {data.content.length > 0 ? (
            <form onSubmit={handleSubmit}>
              <fieldset className="rooms-fieldset" disabled={busy}>
                <legend className="rooms-sr-only">
                  Teacher login account
                </legend>

                <div className="rooms-form-grid">
                  <label>
                    Available Teacher account
                    <select
                      value={selectedId}
                      onChange={(event) =>
                        setSelectedId(event.target.value)
                      }
                      required
                    >
                      <option value="">Choose an account</option>

                      {data.content.map((account) => (
                        <option key={account.id} value={account.id}>
                          {account.displayName} — {account.email}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                {selectedAccount && (
                  <p>
                    Link <strong>{selectedAccount.email}</strong> to{' '}
                    <strong>{teacher.fullName}</strong>.
                  </p>
                )}

                <div className="rooms-actions">
                  <button
                    className="rooms-button yellow"
                    type="submit"
                    disabled={!selectedAccount}
                  >
                    {busy ? 'Linking…' : 'Save account link'}
                  </button>
                </div>
              </fieldset>
            </form>
          ) : (
            <p>
              No available accounts on this page. Create an account
              with the Teacher role if this teacher does not have one.
            </p>
          )}

          <nav
            className="rooms-pagination"
            aria-label="Available account pages"
          >
            <button
              className="rooms-button"
              type="button"
              disabled={busy || page === 0}
              onClick={() => changePage(page - 1)}
            >
              Previous accounts
            </button>

            <span>
              Page {page + 1} of {Math.max(1, data.totalPages)}
            </span>

            <button
              className="rooms-button"
              type="button"
              disabled={busy || data.last}
              onClick={() => changePage(page + 1)}
            >
              Next accounts
            </button>
          </nav>
        </>
      )}

      <div className="rooms-actions" style={{ marginTop: '1rem' }}>
        <button
          className="rooms-button"
          type="button"
          disabled={busy}
          onClick={onClose}
        >
          Close account panel
        </button>

        <button
          className="rooms-button"
          type="button"
          disabled={busy || loading}
          onClick={() => {
            setSelectedId('')
            setRefresh((value) => value + 1)
          }}
        >
          Reload accounts
        </button>
      </div>
    </section>
  )
}