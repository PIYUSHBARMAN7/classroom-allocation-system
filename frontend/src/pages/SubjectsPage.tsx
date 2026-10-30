import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'

import {
  SubjectApiError,
  createSubject,
  getSubjects,
  setSubjectStatus,
  updateSubject,
} from '../services/subjectService'

import { roomTypeLabels } from '../types/classroom'
import type { RoomType } from '../types/classroom'
import type {
  Subject,
  SubjectInput,
  SubjectPage,
} from '../types/subject'

import '../styles/classrooms.css'

export default function SubjectsPage() {
  const navigate = useNavigate()
  const mutationLock = useRef(false)
  const headingRef = useRef<HTMLHeadingElement>(null)

  const [data, setData] = useState<SubjectPage | null>(null)
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState('all')
  const [refresh, setRefresh] = useState(0)

  const [editing, setEditing] = useState<Subject | null>(null)
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  const [loadError, setLoadError] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      setLoading(true)
      setLoadError('')
      setData(null)

      const enabled =
        filter === 'all' ? undefined : filter === 'active'

      try {
        const result = await getSubjects(
          page,
          enabled,
          controller.signal,
        )

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (controller.signal.aborted) return

        if (err instanceof SubjectApiError && err.status === 401) {
          void navigate('/login', { replace: true })
          return
        }

        setLoadError(
          err instanceof Error
            ? err.message
            : 'Unable to load subjects.',
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void load()

    return () => controller.abort()
  }, [page, filter, refresh, navigate])

  function handleError(err: unknown) {
    if (err instanceof SubjectApiError && err.status === 401) {
      void navigate('/login', { replace: true })
      return
    }

    setError(
      err instanceof TypeError
        ? 'Could not confirm the change because the connection failed. Reload the directory before retrying.'
        : err instanceof Error
          ? err.message
          : 'Unable to save the subject.',
    )
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (mutationLock.current) return

    const form = event.currentTarget
    const values = new FormData(form)

    const input: SubjectInput = {
      code: String(values.get('code') ?? '').trim().toUpperCase(),
      name: String(values.get('name') ?? '').trim(),
      department: String(values.get('department') ?? '').trim(),
      requiredRoomType: String(
        values.get('requiredRoomType'),
      ) as RoomType,
    }

    setError('')
    setSuccess('')

    if (!input.name || !input.department) {
      setError('Subject name and department cannot be blank.')
      return
    }

    mutationLock.current = true
    setBusy(true)

    try {
      const saved = editing
        ? await updateSubject(editing.id, input, editing.version)
        : await createSubject(input)

      setSuccess(
        `${saved.code} ${editing ? 'updated' : 'added'} successfully.`,
      )

      form.reset()
      setEditing(null)
      setPage(0)
      setRefresh((value) => value + 1)
    } catch (err) {
      handleError(err)
    } finally {
      mutationLock.current = false
      setBusy(false)
    }
  }

  function startEditing(subject: Subject) {
    setEditing(subject)
    setError('')
    setSuccess('')
    headingRef.current?.focus()
  }

  async function toggleStatus(subject: Subject) {
    if (mutationLock.current) return

    mutationLock.current = true
    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const saved = await setSubjectStatus(
        subject.id,
        !subject.enabled,
        subject.version,
      )

      if (editing?.id === subject.id) {
        setEditing(null)
      }

      setSuccess(
        `${saved.code} ${saved.enabled ? 'activated' : 'deactivated'}.`,
      )

      setRefresh((value) => value + 1)
    } catch (err) {
      handleError(err)
    } finally {
      mutationLock.current = false
      setBusy(false)
    }
  }

  return (
    <main className="classrooms-page">
      <header className="rooms-header">
        <Link to="/dashboard">← Dashboard</Link>
        <p className="rooms-eyebrow">ACADEMIC MANAGEMENT</p>
        <h1>Subjects</h1>
        <p>Manage your subject catalogue and room requirements.</p>
      </header>

      {error && (
        <p className="rooms-message rooms-error" role="alert">
          {error}
        </p>
      )}

      {success && (
        <p className="rooms-message rooms-success" role="status">
          {success}
        </p>
      )}

      <section className="rooms-panel">
        <h2 ref={headingRef} tabIndex={-1}>
          {editing ? `Edit ${editing.code}` : 'Add a subject'}
        </h2>

        <form
          key={editing ? `${editing.id}-${editing.version}` : 'new'}
          onSubmit={handleSubmit}
        >
          <fieldset className="rooms-fieldset" disabled={busy}>
            <legend className="rooms-sr-only">Subject details</legend>

            <div className="rooms-form-grid">
              <label>
                Subject code
                <input
                  name="code"
                  defaultValue={editing?.code ?? ''}
                  placeholder="CS-JAVA"
                  maxLength={30}
                  pattern="[A-Za-z0-9][A-Za-z0-9_\-]{0,29}"
                  title="Use letters, numbers, hyphens or underscores."
                  required
                />
              </label>

              <label>
                Subject name
                <input
                  name="name"
                  defaultValue={editing?.name ?? ''}
                  placeholder="Java Programming"
                  maxLength={100}
                  required
                />
              </label>

              <label>
                Department
                <input
                  name="department"
                  defaultValue={editing?.department ?? ''}
                  placeholder="Computer Science"
                  maxLength={100}
                  required
                />
              </label>

              <label>
                Required room type
                <select
                  name="requiredRoomType"
                  defaultValue={
                    editing?.requiredRoomType ?? 'LECTURE_HALL'
                  }
                  required
                >
                  {Object.entries(roomTypeLabels).map(([value, label]) => (
                    <option key={value} value={value}>
                      {label}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <p>
              Add theory and practical subjects separately when they
              require different room types.
            </p>

            <div className="rooms-actions">
              <button
                className="rooms-button yellow"
                type="submit"
              >
                {busy
                  ? 'Saving…'
                  : editing
                    ? 'Save changes'
                    : 'Add subject'}
              </button>

              {editing && (
                <button
                  className="rooms-button"
                  type="button"
                  onClick={() => {
                    setEditing(null)
                    setError('')
                  }}
                >
                  Cancel editing
                </button>
              )}
            </div>
          </fieldset>
        </form>
      </section>

      <section className="rooms-panel" aria-busy={loading || busy}>
        <div className="rooms-toolbar">
          <h2>Subject directory</h2>

          <div className="rooms-actions">
            <label className="rooms-filter">
              Status
              <select
                value={filter}
                disabled={busy}
                onChange={(event) => {
                  setFilter(event.target.value)
                  setPage(0)
                }}
              >
                <option value="all">All subjects</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
              </select>
            </label>

            <button
              className="rooms-button"
              type="button"
              disabled={busy || loading}
              onClick={() => setRefresh((value) => value + 1)}
            >
              Reload
            </button>
          </div>
        </div>

        {loadError && (
          <p className="rooms-message rooms-error" role="alert">
            {loadError}
          </p>
        )}

        {loading && <p role="status">Loading subjects…</p>}

        {!loading && data && (
          <>
            <p role="status">
              {data.totalElements} subject
              {data.totalElements === 1 ? '' : 's'} found
            </p>

            {data.content.length === 0 ? (
              <p>
                No subjects on this page. Add a subject, change the
                filter, or go to the previous page.
              </p>
            ) : (
              <div className="rooms-table-scroll">
                <table className="rooms-table">
                  <caption className="rooms-sr-only">
                    Subject details and management actions
                  </caption>

                  <thead>
                    <tr>
                      <th scope="col">Code</th>
                      <th scope="col">Subject</th>
                      <th scope="col">Department</th>
                      <th scope="col">Required room</th>
                      <th scope="col">Status</th>
                      <th scope="col">Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {data.content.map((subject) => (
                      <tr key={subject.id}>
                        <th scope="row">{subject.code}</th>
                        <td>{subject.name}</td>
                        <td>{subject.department}</td>
                        <td>
                          {roomTypeLabels[subject.requiredRoomType]}
                        </td>
                        <td>
                          <span
                            className={`rooms-status ${
                              subject.enabled ? 'is-active' : ''
                            }`}
                          >
                            {subject.enabled ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <div className="rooms-actions">
                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`Edit ${subject.code}`}
                              onClick={() => startEditing(subject)}
                            >
                              Edit
                            </button>

                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`${
                                subject.enabled ? 'Deactivate' : 'Activate'
                              } ${subject.code}`}
                              onClick={() => void toggleStatus(subject)}
                            >
                              {subject.enabled ? 'Deactivate' : 'Activate'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <nav
              className="rooms-pagination"
              aria-label="Subject directory pages"
            >
              <button
                className="rooms-button"
                type="button"
                disabled={busy || page === 0}
                onClick={() =>
                  setPage((value) => Math.max(0, value - 1))
                }
              >
                Previous
              </button>

              <span>
                Page {page + 1} of {Math.max(1, data.totalPages)}
              </span>

              <button
                className="rooms-button"
                type="button"
                disabled={busy || data.last}
                onClick={() => setPage((value) => value + 1)}
              >
                Next
              </button>
            </nav>
          </>
        )}
      </section>
    </main>
  )
}