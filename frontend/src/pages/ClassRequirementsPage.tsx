import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'

import { getSections } from '../services/sectionService'
import { getSubjects } from '../services/subjectService'
import { getTeachers } from '../services/teacherService'

import {
  RequirementApiError,
  createRequirement,
  getRequirements,
  setRequirementStatus,
  updateRequirement,
} from '../services/requirementService'

import type {
  ClassRequirement,
  RequirementInput,
  RequirementPage,
} from '../types/requirement'

import '../styles/classrooms.css'
import '../styles/requirements.css'

type RecordOption = {
  id: number
  label: string
}

type OptionPage = {
  options: RecordOption[]
  totalPages: number
  last: boolean
}

type PickerProps = {
  id: string
  label: string
  value: RecordOption | null
  onChange: (value: RecordOption | null) => void
  loadPage: (
    page: number,
    signal: AbortSignal,
  ) => Promise<OptionPage>
  disabled: boolean
}

async function loadSectionOptions(
  page: number,
  signal: AbortSignal,
): Promise<OptionPage> {
  const result = await getSections(page, true, signal)

  return {
    options: result.content.map((section) => ({
      id: section.id,
      label:
        `${section.name} · ${section.programCode} · ` +
        `${section.academicYearStart}–${section.academicYearStart + 1}`,
    })),
    totalPages: result.totalPages,
    last: result.last,
  }
}

async function loadSubjectOptions(
  page: number,
  signal: AbortSignal,
): Promise<OptionPage> {
  const result = await getSubjects(page, true, signal)

  return {
    options: result.content.map((subject) => ({
      id: subject.id,
      label: `${subject.code} · ${subject.name}`,
    })),
    totalPages: result.totalPages,
    last: result.last,
  }
}

async function loadTeacherOptions(
  page: number,
  signal: AbortSignal,
): Promise<OptionPage> {
  const result = await getTeachers(page, true, signal)

  return {
    options: result.content.map((teacher) => ({
      id: teacher.id,
      label: `${teacher.employeeCode} · ${teacher.fullName}`,
    })),
    totalPages: result.totalPages,
    last: result.last,
  }
}

function RecordPicker({
  id,
  label,
  value,
  onChange,
  loadPage,
  disabled,
}: PickerProps) {
  const navigate = useNavigate()

  const [page, setPage] = useState(0)
  const [refresh, setRefresh] = useState(0)
  const [data, setData] = useState<OptionPage | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      setLoading(true)
      setError('')
      setData(null)

      try {
        const result = await loadPage(page, controller.signal)

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (controller.signal.aborted) return

        if (
          err instanceof Error &&
          'status' in err &&
          err.status === 401
        ) {
          void navigate('/login', { replace: true })
          return
        }

        setError(
          err instanceof Error
            ? err.message
            : 'Unable to load available records.',
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void load()

    return () => controller.abort()
  }, [page, refresh, loadPage, navigate])

  const selectedOffPage =
    value !== null &&
    !data?.options.some((option) => option.id === value.id)

  return (
    <div className="requirement-picker">
      <label htmlFor={id}>{label}</label>

      <select
        id={id}
        value={value?.id ?? ''}
        disabled={disabled || loading || Boolean(error)}
        aria-describedby={`${id}-status`}
        required
        onChange={(event) => {
          if (!event.target.value) {
            onChange(null)
            return
          }

          const selectedId = Number(event.target.value)
          const option = data?.options.find(
            (item) => item.id === selectedId,
          )

          onChange(
            option ??
              (value?.id === selectedId ? value : null),
          )
        }}
      >
        <option value="">Choose {label.toLowerCase()}</option>

        {selectedOffPage && value && (
          <option value={value.id}>{value.label}</option>
        )}

        {data?.options.map((option) => (
          <option key={option.id} value={option.id}>
            {option.label}
          </option>
        ))}
      </select>

      <p id={`${id}-status`} className="requirement-picker-note">
        {loading
          ? 'Loading available records…'
          : error
            ? error
            : data?.options.length === 0
              ? 'No active records on this page.'
              : 'Showing active records.'}
      </p>

      <div className="rooms-actions">
        <button
          className="rooms-button"
          type="button"
          disabled={disabled || loading || page === 0}
          aria-label={`Previous ${label.toLowerCase()} options`}
          onClick={() => setPage((current) => Math.max(0, current - 1))}
        >
          Previous
        </button>

        <span className="requirement-picker-note">
          Page {page + 1}
          {data ? ` of ${Math.max(1, data.totalPages)}` : ''}
        </span>

        <button
          className="rooms-button"
          type="button"
          disabled={disabled || loading || !data || data.last}
          aria-label={`Next ${label.toLowerCase()} options`}
          onClick={() => setPage((current) => current + 1)}
        >
          Next
        </button>

        <button
          className="rooms-button"
          type="button"
          disabled={disabled || loading}
          aria-label={`Reload ${label.toLowerCase()} options`}
          onClick={() => setRefresh((current) => current + 1)}
        >
          Reload
        </button>
      </div>
    </div>
  )
}

export default function ClassRequirementsPage() {
  const navigate = useNavigate()
  const mutationLock = useRef(false)
  const headingRef = useRef<HTMLHeadingElement>(null)

  const [data, setData] = useState<RequirementPage | null>(null)
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState('all')
  const [refresh, setRefresh] = useState(0)

  const [editing, setEditing] =
    useState<ClassRequirement | null>(null)

  const [section, setSection] = useState<RecordOption | null>(null)
  const [subject, setSubject] = useState<RecordOption | null>(null)
  const [teacher, setTeacher] = useState<RecordOption | null>(null)

  const [sessions, setSessions] = useState('3')
  const [periods, setPeriods] = useState('1')

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
        const result = await getRequirements(
          page,
          enabled,
          controller.signal,
        )

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (controller.signal.aborted) return

        if (err instanceof RequirementApiError && err.status === 401) {
          void navigate('/login', { replace: true })
          return
        }

        setLoadError(
          err instanceof Error
            ? err.message
            : 'Unable to load requirements.',
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

  function resetEditor() {
    setEditing(null)
    setSection(null)
    setSubject(null)
    setTeacher(null)
    setSessions('3')
    setPeriods('1')
    setError('')
  }

  function handleError(err: unknown) {
    if (err instanceof RequirementApiError && err.status === 401) {
      void navigate('/login', { replace: true })
      return
    }

    setError(
      err instanceof TypeError
        ? 'Could not confirm the change because the connection failed. Reload the directory before retrying.'
        : err instanceof Error
          ? err.message
          : 'Unable to save the requirement.',
    )
  }

  function startEditing(requirement: ClassRequirement) {
    setEditing(requirement)

    setSection({
      id: requirement.section.id,
      label:
        `${requirement.section.name} · ` +
        `${requirement.section.programCode} · ` +
        `${requirement.section.academicYearStart}–` +
        `${requirement.section.academicYearStart + 1}` +
        (requirement.section.enabled ? '' : ' · Inactive'),
    })

    setSubject({
      id: requirement.subject.id,
      label:
        `${requirement.subject.code} · ${requirement.subject.name}` +
        (requirement.subject.enabled ? '' : ' · Inactive'),
    })

    setTeacher({
      id: requirement.teacher.id,
      label:
        `${requirement.teacher.employeeCode} · ` +
        requirement.teacher.fullName +
        (requirement.teacher.enabled ? '' : ' · Inactive'),
    })

    setSessions(String(requirement.sessionsPerWeek))
    setPeriods(String(requirement.periodsPerSession))
    setError('')
    setSuccess('')
    headingRef.current?.focus()
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (mutationLock.current) return

    setError('')
    setSuccess('')

    if (!section || !subject || !teacher) {
      setError('Choose a section, subject and teacher.')
      return
    }

    const form = event.currentTarget
    const values = new FormData(form)

    const input: RequirementInput = {
      sectionId: section.id,
      subjectId: subject.id,
      teacherId: teacher.id,
      sessionsPerWeek: Number(sessions),
      periodsPerSession: Number(periods),
      requiresProjector: values.get('requiresProjector') === 'on',
      requiresAccessibleRoom:
        values.get('requiresAccessibleRoom') === 'on',
    }

    mutationLock.current = true
    setBusy(true)

    try {
      const saved = editing
        ? await updateRequirement(editing.id, input, editing.version)
        : await createRequirement(input)

      setSuccess(
        `${saved.subject.name} requirement ${
          editing ? 'updated' : 'added'
        } successfully.`,
      )

      form.reset()
      resetEditor()
      setPage(0)
      setRefresh((value) => value + 1)
    } catch (err) {
      handleError(err)
    } finally {
      mutationLock.current = false
      setBusy(false)
    }
  }

  async function toggleStatus(requirement: ClassRequirement) {
    if (mutationLock.current) return

    mutationLock.current = true
    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const saved = await setRequirementStatus(
        requirement.id,
        !requirement.enabled,
        requirement.version,
      )

      if (editing?.id === requirement.id) {
        resetEditor()
      }

      setSuccess(
        `${saved.subject.name} requirement ${
          saved.enabled ? 'activated' : 'deactivated'
        }.`,
      )

      setRefresh((value) => value + 1)
    } catch (err) {
      handleError(err)
    } finally {
      mutationLock.current = false
      setBusy(false)
    }
  }

  const weeklyPeriods = Number(sessions) * Number(periods)

  return (
    <main className="classrooms-page">
      <header className="rooms-header">
        <Link to="/dashboard">← Dashboard</Link>
        <p className="rooms-eyebrow">ALLOCATION PLANNING</p>
        <h1>Class requirements</h1>
        <p>Connect each section and subject to its teacher and weekly needs.</p>
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
          {editing ? 'Edit class requirement' : 'Add a class requirement'}
        </h2>

        <form
          key={editing ? `${editing.id}-${editing.version}` : 'new'}
          onSubmit={handleSubmit}
        >
          <fieldset className="rooms-fieldset" disabled={busy}>
            <legend className="rooms-sr-only">
              Class requirement details
            </legend>

            <div className="rooms-form-grid">
              <RecordPicker
                id="requirement-section"
                label="Section"
                value={section}
                onChange={setSection}
                loadPage={loadSectionOptions}
                disabled={busy}
              />

              <RecordPicker
                id="requirement-subject"
                label="Subject"
                value={subject}
                onChange={setSubject}
                loadPage={loadSubjectOptions}
                disabled={busy}
              />

              <RecordPicker
                id="requirement-teacher"
                label="Teacher"
                value={teacher}
                onChange={setTeacher}
                loadPage={loadTeacherOptions}
                disabled={busy}
              />

              <label>
                Sessions per week
                <input
                  name="sessionsPerWeek"
                  type="number"
                  value={sessions}
                  onChange={(event) => setSessions(event.target.value)}
                  min={1}
                  max={20}
                  step={1}
                  required
                />
              </label>

              <label>
                Consecutive periods per session
                <input
                  name="periodsPerSession"
                  type="number"
                  value={periods}
                  onChange={(event) => setPeriods(event.target.value)}
                  min={1}
                  max={4}
                  step={1}
                  required
                />
              </label>
            </div>

            <div className="rooms-checkboxes">
              <label>
                <input
                  name="requiresProjector"
                  type="checkbox"
                  defaultChecked={editing?.requiresProjector ?? false}
                />
                Projector required
              </label>

              <label>
                <input
                  name="requiresAccessibleRoom"
                  type="checkbox"
                  defaultChecked={
                    editing?.requiresAccessibleRoom ?? false
                  }
                />
                Accessible room required
              </label>
            </div>

            <p className="requirement-workload" role="status">
              Weekly demand:{' '}
              <strong>
                {Number.isFinite(weeklyPeriods) ? weeklyPeriods : 0}
              </strong>{' '}
              teaching periods.
            </p>

            <p>
              Room capacity comes from the section’s student count.
              Room type comes from the selected subject.
            </p>

            <div className="rooms-actions">
              <button
                className="rooms-button yellow"
                type="submit"
                disabled={!section || !subject || !teacher}
              >
                {busy
                  ? 'Saving…'
                  : editing
                    ? 'Save changes'
                    : 'Add requirement'}
              </button>

              {editing && (
                <button
                  className="rooms-button"
                  type="button"
                  onClick={resetEditor}
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
          <h2>Requirement directory</h2>

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
                <option value="all">All requirements</option>
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

        {loading && <p role="status">Loading requirements…</p>}

        {!loading && data && (
          <>
            <p role="status">
              {data.totalElements} requirement
              {data.totalElements === 1 ? '' : 's'} found
            </p>

            {data.content.length === 0 ? (
              <p>
                No requirements on this page. Add one, change the
                filter, or return to the previous page.
              </p>
            ) : (
              <div className="rooms-table-scroll">
                <table className="rooms-table">
                  <caption className="rooms-sr-only">
                    Class requirements and management actions
                  </caption>

                  <thead>
                    <tr>
                      <th scope="col">Section</th>
                      <th scope="col">Subject</th>
                      <th scope="col">Teacher</th>
                      <th scope="col">Weekly demand</th>
                      <th scope="col">Facilities</th>
                      <th scope="col">Status</th>
                      <th scope="col">Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {data.content.map((requirement) => (
                      <tr key={requirement.id}>
                        <td>
                          <strong>{requirement.section.name}</strong>
                          <div>
                            {requirement.section.programCode} · Semester{' '}
                            {requirement.section.semester}
                          </div>
                          <div>
                            {requirement.section.academicYearStart}–
                            {requirement.section.academicYearStart + 1}
                          </div>
                          <div>
                            {requirement.section.studentCount} students
                          </div>
                        </td>

                        <td>
                          {requirement.subject.name}
                          <div>{requirement.subject.code}</div>
                        </td>

                        <td>{requirement.teacher.fullName}</td>

                        <td>
                          {requirement.sessionsPerWeek} sessions ×{' '}
                          {requirement.periodsPerSession} periods
                          <div>
                            <strong>
                              {requirement.weeklyPeriods} periods/week
                            </strong>
                          </div>
                        </td>

                        <td>
                          {[
                            requirement.requiresProjector
                              ? 'Projector'
                              : '',
                            requirement.requiresAccessibleRoom
                              ? 'Accessible room'
                              : '',
                          ]
                            .filter(Boolean)
                            .join(', ') || 'No additional requirements'}
                        </td>

                        <td>
                          <span
                            className={`rooms-status ${
                              requirement.enabled ? 'is-active' : ''
                            }`}
                          >
                            {requirement.enabled ? 'Active' : 'Inactive'}
                          </span>

                          {(!requirement.section.enabled ||
                            !requirement.subject.enabled ||
                            !requirement.teacher.enabled) && (
                            <div>A linked record is inactive.</div>
                          )}
                        </td>

                        <td>
                          <div className="rooms-actions">
                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`Edit ${requirement.subject.name} for ${requirement.section.name}`}
                              onClick={() => startEditing(requirement)}
                            >
                              Edit
                            </button>

                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              onClick={() =>
                                void toggleStatus(requirement)
                              }
                            >
                              {requirement.enabled
                                ? 'Deactivate'
                                : 'Activate'}
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
              aria-label="Requirement directory pages"
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