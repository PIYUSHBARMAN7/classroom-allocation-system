import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'

import {
  SectionApiError,
  createSection,
  getSections,
  setSectionStatus,
  updateSection,
} from '../services/sectionService'

import type {
  Section,
  SectionInput,
  SectionPage,
} from '../types/section'

import '../styles/classrooms.css'

type FormField = {
  name: keyof SectionInput
  label: string
  type?: 'text' | 'number'
  defaultValue: string | number
  placeholder?: string
  min?: number
  max?: number
  maxLength?: number
  pattern?: string
}

export default function SectionsPage() {
  const navigate = useNavigate()
  const mutationLock = useRef(false)
  const headingRef = useRef<HTMLHeadingElement>(null)

  const [data, setData] = useState<SectionPage | null>(null)
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState('all')
  const [refresh, setRefresh] = useState(0)

  const [editing, setEditing] = useState<Section | null>(null)
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
        const result = await getSections(
          page,
          enabled,
          controller.signal,
        )

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (controller.signal.aborted) return

        if (err instanceof SectionApiError && err.status === 401) {
          void navigate('/login', { replace: true })
          return
        }

        setLoadError(
          err instanceof Error
            ? err.message
            : 'Unable to load sections.',
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
    if (err instanceof SectionApiError && err.status === 401) {
      void navigate('/login', { replace: true })
      return
    }

    setError(
      err instanceof TypeError
        ? 'Could not confirm the change because the connection failed. Reload the directory before retrying.'
        : err instanceof Error
          ? err.message
          : 'Unable to save the section.',
    )
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (mutationLock.current) return

    const form = event.currentTarget
    const values = new FormData(form)

    const input: SectionInput = {
      code: String(values.get('code') ?? '').trim().toUpperCase(),
      name: String(values.get('name') ?? '').trim(),
      programCode: String(values.get('programCode') ?? '')
        .trim()
        .toUpperCase(),
      department: String(values.get('department') ?? '').trim(),
      semester: Number(values.get('semester')),
      academicYearStart: Number(values.get('academicYearStart')),
      studentCount: Number(values.get('studentCount')),
    }

    setError('')
    setSuccess('')

    if (!input.name || !input.department) {
      setError('Section name and department cannot be blank.')
      return
    }

    mutationLock.current = true
    setBusy(true)

    try {
      const saved = editing
        ? await updateSection(editing.id, input, editing.version)
        : await createSection(input)

      setSuccess(
        `${saved.name} ${editing ? 'updated' : 'added'} successfully.`,
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

  function startEditing(section: Section) {
    setEditing(section)
    setError('')
    setSuccess('')
    headingRef.current?.focus()
  }

  async function toggleStatus(section: Section) {
    if (mutationLock.current) return

    mutationLock.current = true
    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const saved = await setSectionStatus(
        section.id,
        !section.enabled,
        section.version,
      )

      if (editing?.id === section.id) {
        setEditing(null)
      }

      setSuccess(
        `${saved.name} ${saved.enabled ? 'activated' : 'deactivated'}.`,
      )

      setRefresh((value) => value + 1)
    } catch (err) {
      handleError(err)
    } finally {
      mutationLock.current = false
      setBusy(false)
    }
  }

  const codePattern = '[A-Za-z0-9][A-Za-z0-9_\\-]{0,29}'

  const fields: FormField[] = [
    {
      name: 'code',
      label: 'Section code',
      defaultValue: editing?.code ?? '',
      placeholder: 'A',
      maxLength: 30,
      pattern: codePattern,
    },
    {
      name: 'name',
      label: 'Section name',
      defaultValue: editing?.name ?? '',
      placeholder: 'BCA Semester III – Section A',
      maxLength: 100,
    },
    {
      name: 'programCode',
      label: 'Program code',
      defaultValue: editing?.programCode ?? '',
      placeholder: 'BCA',
      maxLength: 30,
      pattern: codePattern,
    },
    {
      name: 'department',
      label: 'Department',
      defaultValue: editing?.department ?? '',
      placeholder: 'Computer Science',
      maxLength: 100,
    },
    {
      name: 'semester',
      label: 'Semester',
      type: 'number',
      defaultValue: editing?.semester ?? 1,
      min: 1,
      max: 12,
    },
    {
      name: 'academicYearStart',
      label: 'Academic year start',
      type: 'number',
      defaultValue:
        editing?.academicYearStart ?? new Date().getFullYear(),
      min: 2000,
      max: 2200,
    },
    {
      name: 'studentCount',
      label: 'Number of students',
      type: 'number',
      defaultValue: editing?.studentCount ?? 60,
      min: 1,
      max: 2000,
    },
  ]

  return (
    <main className="classrooms-page">
      <header className="rooms-header">
        <Link to="/dashboard">← Dashboard</Link>
        <p className="rooms-eyebrow">ACADEMIC MANAGEMENT</p>
        <h1>Sections</h1>
        <p>Organise student groups by program, semester and year.</p>
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
          {editing ? `Edit ${editing.name}` : 'Add a section'}
        </h2>

        <form
          key={editing ? `${editing.id}-${editing.version}` : 'new'}
          onSubmit={handleSubmit}
        >
          <fieldset className="rooms-fieldset" disabled={busy}>
            <legend className="rooms-sr-only">Section details</legend>

            <div className="rooms-form-grid">
              {fields.map((field) => (
                <label key={field.name}>
                  {field.label}
                  <input
                    name={field.name}
                    type={field.type ?? 'text'}
                    defaultValue={field.defaultValue}
                    placeholder={field.placeholder}
                    min={field.min}
                    max={field.max}
                    maxLength={field.maxLength}
                    pattern={field.pattern}
                    step={field.type === 'number' ? 1 : undefined}
                    title={
                      field.pattern
                        ? 'Use letters, numbers, hyphens or underscores.'
                        : undefined
                    }
                    required
                  />
                </label>
              ))}
            </div>

            <p>
              Enter 2026 for academic year 2026–2027. Student count
              will guide room-capacity checks.
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
                    : 'Add section'}
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
          <h2>Section directory</h2>

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
                <option value="all">All sections</option>
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

        {loading && <p role="status">Loading sections…</p>}

        {!loading && data && (
          <>
            <p role="status">
              {data.totalElements} section
              {data.totalElements === 1 ? '' : 's'} found
            </p>

            {data.content.length === 0 ? (
              <p>
                No sections on this page. Add one, change the filter,
                or return to the previous page.
              </p>
            ) : (
              <div className="rooms-table-scroll">
                <table className="rooms-table">
                  <caption className="rooms-sr-only">
                    Sections and management actions
                  </caption>

                  <thead>
                    <tr>
                      <th scope="col">Section</th>
                      <th scope="col">Program</th>
                      <th scope="col">Department</th>
                      <th scope="col">Semester</th>
                      <th scope="col">Academic year</th>
                      <th scope="col">Students</th>
                      <th scope="col">Status</th>
                      <th scope="col">Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {data.content.map((section) => (
                      <tr key={section.id}>
                        <td>
                          <strong>{section.code}</strong>
                          <div>{section.name}</div>
                        </td>
                        <td>{section.programCode}</td>
                        <td>{section.department}</td>
                        <td>{section.semester}</td>
                        <td>
                          {section.academicYearStart}–
                          {section.academicYearStart + 1}
                        </td>
                        <td>{section.studentCount}</td>
                        <td>
                          <span
                            className={`rooms-status ${
                              section.enabled ? 'is-active' : ''
                            }`}
                          >
                            {section.enabled ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <div className="rooms-actions">
                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`Edit ${section.name}`}
                              onClick={() => startEditing(section)}
                            >
                              Edit
                            </button>

                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`${
                                section.enabled ? 'Deactivate' : 'Activate'
                              } ${section.name}`}
                              onClick={() => void toggleStatus(section)}
                            >
                              {section.enabled ? 'Deactivate' : 'Activate'}
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
              aria-label="Section directory pages"
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