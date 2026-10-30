import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import {
  assignStudentSection,
  getAssignmentSections,
  lookupStudentSection,
  StudentSectionError,
} from '../services/studentSectionService'
import type {
  SectionOption,
  StudentSection,
} from '../services/studentSectionService'
import '../styles/allocation.css'

function sectionLabel(section: SectionOption) {
  return `${section.code} · ${section.programCode} · Semester ${
    section.semester
  } · ${section.academicYearStart}–${section.academicYearStart + 1}`
}

export default function StudentSectionAssignmentPage() {
  const [email, setEmail] = useState('')
  const [student, setStudent] = useState<StudentSection | null>(null)
  const [selectedSection, setSelectedSection] =
    useState<SectionOption | null>(null)

  const [sections, setSections] = useState<SectionOption[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [refresh, setRefresh] = useState(0)
  const [loadingSections, setLoadingSections] = useState(true)

  const [operation, setOperation] =
    useState<'lookup' | 'save' | null>(null)

  const [error, setError] = useState('')
  const [sectionError, setSectionError] = useState('')
  const [success, setSuccess] = useState('')

  const operationController = useRef<AbortController | null>(null)
  const busy = operation !== null

  useEffect(() => {
    const controller = new AbortController()

    async function loadSections() {
      setLoadingSections(true)
      setSectionError('')

      try {
        const result = await getAssignmentSections(
          page,
          controller.signal,
        )

        if (controller.signal.aborted) return

        setSections(result.content)
        setTotalPages(result.totalPages)
      } catch (err) {
        if (controller.signal.aborted) return

        setSections([])
        setSectionError(
          err instanceof Error
            ? err.message
            : 'Unable to load sections.',
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoadingSections(false)
        }
      }
    }

    void loadSections()

    return () => controller.abort()
  }, [page, refresh])

  useEffect(() => {
    return () => operationController.current?.abort()
  }, [])

  async function handleLookup(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (operationController.current !== null) return

    const controller = new AbortController()
    operationController.current = controller

    setOperation('lookup')
    setError('')
    setSuccess('')
    setStudent(null)
    setSelectedSection(null)

    try {
      const result = await lookupStudentSection(
        email,
        controller.signal,
      )

      if (!controller.signal.aborted) {
        setStudent(result)
      }
    } catch (err) {
      if (!controller.signal.aborted) {
        setError(
          err instanceof Error
            ? err.message
            : 'Unable to look up the Student.',
        )
      }
    } finally {
      if (operationController.current === controller) {
        operationController.current = null
      }

      if (!controller.signal.aborted) {
        setOperation(null)
      }
    }
  }

  async function handleAssign(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (
      operationController.current !== null ||
      !student ||
      !selectedSection
    ) {
      return
    }

    const controller = new AbortController()
    operationController.current = controller

    setOperation('save')
    setError('')
    setSuccess('')

    try {
      const updated = await assignStudentSection(
        student.userId,
        selectedSection.id,
        student.version,
        controller.signal,
      )

      if (!controller.signal.aborted) {
        setStudent(updated)
        setSelectedSection(null)

        setSuccess(
          `${updated.displayName} is now assigned to ${
            updated.section?.code ?? 'the selected section'
          }.`,
        )
      }
    } catch (err) {
      if (!controller.signal.aborted) {
        if (
          err instanceof StudentSectionError &&
          [400, 404, 409].includes(err.status)
        ) {
          setStudent(null)
          setSelectedSection(null)
        }

        setError(
          err instanceof Error
            ? err.message
            : 'Unable to save the assignment.',
        )
      }
    } finally {
      if (operationController.current === controller) {
        operationController.current = null
      }

      if (!controller.signal.aborted) {
        setOperation(null)
      }
    }
  }

  return (
    <main className="allocation-page">
      <Link className="allocation-back" to="/dashboard">
        ← Back to dashboard
      </Link>

      <header className="allocation-heading">
        <p className="allocation-eyebrow">ADMINISTRATION</p>
        <h1>Student section assignments</h1>
        <p>
          Connect each Student account to its current section.
        </p>
      </header>

      {error && (
        <p className="allocation-error" role="alert">
          {error}
        </p>
      )}

      {success && (
        <p className="allocation-success" role="status">
          {success}
        </p>
      )}

      <section className="allocation-panel">
        <h2>1. Find the Student</h2>

        <form
          className="allocation-draft-form"
          onSubmit={(event) => void handleLookup(event)}
        >
          <label htmlFor="student-account-email">
            Student account email
          </label>

          <input
            id="student-account-email"
            type="email"
            value={email}
            onChange={(event) => {
              setEmail(event.target.value)
              setStudent(null)
              setSelectedSection(null)
              setError('')
              setSuccess('')
            }}
            placeholder="student@college.edu"
            autoCapitalize="none"
            spellCheck={false}
            maxLength={254}
            required
            disabled={busy}
          />

          <button
            className="allocation-button allocation-primary"
            type="submit"
            disabled={busy || !email.trim()}
          >
            {operation === 'lookup'
              ? 'Looking up…'
              : 'Look up Student'}
          </button>
        </form>
      </section>

      {student && (
        <section className="allocation-panel">
          <h2>2. Assign a section</h2>

          <dl className="student-assignment-details">
            <div>
              <dt>Student</dt>
              <dd>{student.displayName}</dd>
            </div>

            <div>
              <dt>Email</dt>
              <dd>{student.email}</dd>
            </div>

            <div>
              <dt>Current section</dt>
              <dd>
                {student.section
                  ? sectionLabel(student.section)
                  : 'No section assigned'}

                {student.section && !student.section.enabled
                  ? ' · Disabled'
                  : ''}
              </dd>
            </div>
          </dl>

          {sectionError && (
            <p className="allocation-error" role="alert">
              {sectionError}
            </p>
          )}

          <form
            className="allocation-draft-form"
            onSubmit={(event) => void handleAssign(event)}
          >
            <label htmlFor="student-section">
              Choose an enabled section
            </label>

            <select
              id="student-section"
              value={selectedSection?.id ?? ''}
              onChange={(event) => {
                const id = Number(event.target.value)

                setSelectedSection(
                  sections.find((section) => section.id === id) ?? null,
                )

                setSuccess('')
              }}
              required
              disabled={busy || loadingSections}
            >
              <option value="">
                {loadingSections
                  ? 'Loading sections…'
                  : 'Select a section'}
              </option>

              {selectedSection &&
                !sections.some(
                  (section) => section.id === selectedSection.id,
                ) && (
                  <option value={selectedSection.id}>
                    {sectionLabel(selectedSection)}
                  </option>
                )}

              {sections.map((section) => (
                <option key={section.id} value={section.id}>
                  {sectionLabel(section)}
                </option>
              ))}
            </select>

            <div className="allocation-pagination">
              <button
                className="allocation-button"
                type="button"
                disabled={busy || loadingSections || page === 0}
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
                disabled={
                  busy ||
                  loadingSections ||
                  page + 1 >= totalPages
                }
                onClick={() => setPage((value) => value + 1)}
              >
                Next
              </button>
            </div>

            <button
              className="allocation-button"
              type="button"
              disabled={busy || loadingSections}
              onClick={() => setRefresh((value) => value + 1)}
            >
              Refresh sections
            </button>

            {!loadingSections &&
              !sectionError &&
              sections.length === 0 && (
                <p>No enabled sections are available on this page.</p>
              )}

            <button
              className="allocation-button allocation-primary"
              type="submit"
              disabled={
                busy ||
                !selectedSection ||
                selectedSection.id === student.section?.id
              }
            >
              {operation === 'save'
                ? 'Saving assignment…'
                : 'Save section assignment'}
            </button>
          </form>
        </section>
      )}
    </main>
  )
}