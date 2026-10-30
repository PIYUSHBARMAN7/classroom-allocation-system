import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import {
    Link,
    useNavigate,
    useOutletContext,
} from 'react-router'
import type { CurrentUser } from '../services/authService'
import TeacherAccountLinker from '../components/teachers/TeacherAccountLinker'
import {
    TeacherApiError,
    createTeacher,
    getTeachers,
    setTeacherStatus,
    updateTeacher,
} from '../services/teacherService'

import type {
    Teacher,
    TeacherInput,
    TeacherPage,
} from '../types/teacher'

import '../styles/classrooms.css'

export default function TeachersPage() {
    const navigate = useNavigate()
    const mutationLock = useRef(false)
    const user = useOutletContext<CurrentUser>()

    const [accountTeacher, setAccountTeacher] =
        useState<Teacher | null>(null)
    const [data, setData] = useState<TeacherPage | null>(null)
    const [page, setPage] = useState(0)
    const [filter, setFilter] = useState('all')
    const [refresh, setRefresh] = useState(0)

    const [editing, setEditing] = useState<Teacher | null>(null)
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
                const result = await getTeachers(
                    page,
                    enabled,
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
                        : 'Unable to load teachers.',
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
        if (err instanceof TeacherApiError && err.status === 401) {
            void navigate('/login', { replace: true })
            return
        }

        setError(
            err instanceof TypeError
                ? 'Could not confirm the change because the connection failed. Reload the directory before retrying.'
                : err instanceof Error
                    ? err.message
                    : 'Unable to save the teacher profile.',
        )
    }

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault()
        if (mutationLock.current) return

        const form = event.currentTarget
        const values = new FormData(form)

        const input: TeacherInput = {
            employeeCode: String(values.get('employeeCode') ?? '')
                .trim()
                .toUpperCase(),
            fullName: String(values.get('fullName') ?? '').trim(),
            department: String(values.get('department') ?? '').trim(),
            maxPeriodsPerDay: Number(values.get('maxPeriodsPerDay')),
            maxPeriodsPerWeek: Number(values.get('maxPeriodsPerWeek')),
        }

        setError('')
        setSuccess('')

        if (!input.fullName || !input.department) {
            setError('Teacher name and department cannot be blank.')
            return
        }

        mutationLock.current = true
        setBusy(true)

        try {
            const saved = editing
                ? await updateTeacher(editing.id, input, editing.version)
                : await createTeacher(input)

            setSuccess(
                `${saved.fullName} ${editing ? 'updated' : 'added'} successfully.`,
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

    function startEditing(teacher: Teacher) {
        setEditing(teacher)
        setError('')
        setSuccess('')

        document.getElementById('teacher-form-title')?.focus()
    }

    function cancelEditing() {
        setEditing(null)
        setError('')
    }

    async function toggleStatus(teacher: Teacher) {
        if (mutationLock.current) return

        mutationLock.current = true
        setBusy(true)
        setError('')
        setSuccess('')

        try {
            const saved = await setTeacherStatus(
                teacher.id,
                !teacher.enabled,
                teacher.version,
            )

            if (editing?.id === teacher.id) {
                setEditing(null)
            }

            setSuccess(
                `${saved.fullName} ${saved.enabled ? 'activated' : 'deactivated'
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

    return (
        <main className="classrooms-page">
            <header className="rooms-header">
                <Link to="/dashboard">← Dashboard</Link>

                <p className="rooms-eyebrow">CAMPUS MANAGEMENT</p>
                <h1>Teachers</h1>
                <p>Manage teacher profiles and teaching workload limits.</p>
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
                <h2 id="teacher-form-title" tabIndex={-1}>
                    {editing
                        ? `Edit ${editing.employeeCode}`
                        : 'Add a teacher'}
                </h2>

                <form
                    key={editing ? `${editing.id}-${editing.version}` : 'new'}
                    onSubmit={handleSubmit}
                >
                    <fieldset className="rooms-fieldset" disabled={busy}>
                        <legend className="rooms-sr-only">
                            Teacher profile details
                        </legend>

                        <div className="rooms-form-grid">
                            <label>
                                Employee code
                                <input
                                    name="employeeCode"
                                    defaultValue={editing?.employeeCode ?? ''}
                                    placeholder="EMP-101"
                                    maxLength={30}
                                    pattern="[A-Za-z0-9][A-Za-z0-9_\-]{0,29}"
                                    title="Use letters, numbers, hyphens or underscores."
                                    required
                                />
                            </label>

                            <label>
                                Full name
                                <input
                                    name="fullName"
                                    defaultValue={editing?.fullName ?? ''}
                                    placeholder="Teacher name"
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
                                Maximum periods per day
                                <input
                                    name="maxPeriodsPerDay"
                                    type="number"
                                    defaultValue={editing?.maxPeriodsPerDay ?? 6}
                                    min={1}
                                    max={12}
                                    step={1}
                                    required
                                />
                            </label>

                            <label>
                                Maximum periods per week
                                <input
                                    name="maxPeriodsPerWeek"
                                    type="number"
                                    defaultValue={editing?.maxPeriodsPerWeek ?? 24}
                                    min={1}
                                    max={60}
                                    step={1}
                                    required
                                />
                            </label>
                        </div>

                        <p>
                            These limits will guide timetable allocation. Creating
                            a profile does not create a login account.
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
                                        : 'Add teacher'}
                            </button>

                            {editing && (
                                <button
                                    className="rooms-button"
                                    type="button"
                                    onClick={cancelEditing}
                                >
                                    Cancel editing
                                </button>
                            )}
                        </div>
                    </fieldset>
                </form>
            </section>

            {user.role === 'ADMIN' && accountTeacher && (
                <TeacherAccountLinker
                    key={`${accountTeacher.id}-${accountTeacher.version}`}
                    teacher={accountTeacher}
                    onClose={() => setAccountTeacher(null)}
                    onLinked={(updated) => {
                        setAccountTeacher(null)

                        if (editing?.id === updated.id) {
                            setEditing(null)
                        }

                        setError('')
                        setSuccess(`Account linked to ${updated.fullName}.`)
                        setRefresh((value) => value + 1)
                    }}
                />
            )}


            <section className="rooms-panel" aria-busy={loading || busy}>
                <div className="rooms-toolbar">
                    <h2>Teacher directory</h2>

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
                                <option value="all">All teachers</option>
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

                {loading && <p role="status">Loading teachers…</p>}

                {!loading && data && (
                    <>
                        <p role="status">
                            {data.totalElements} teacher
                            {data.totalElements === 1 ? '' : 's'} found
                        </p>

                        {data.content.length === 0 ? (
                            <p>
                                No teachers on this page. Add a profile, change the
                                filter, or return to the previous page.
                            </p>
                        ) : (
                            <div className="rooms-table-scroll">
                                <table className="rooms-table">
                                    <caption className="rooms-sr-only">
                                        Teacher profiles and management actions
                                    </caption>

                                    <thead>
                                        <tr>
                                            <th scope="col">Teacher</th>
                                            <th scope="col">Department</th>
                                            <th scope="col">Daily limit</th>
                                            <th scope="col">Weekly limit</th>
                                            <th scope="col">Login account</th>
                                            <th scope="col">Status</th>
                                            <th scope="col">Actions</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {data.content.map((teacher) => (
                                            <tr key={teacher.id}>
                                                <td>
                                                    <strong>{teacher.fullName}</strong>
                                                    <div>{teacher.employeeCode}</div>
                                                </td>

                                                <td>{teacher.department}</td>
                                                <td>{teacher.maxPeriodsPerDay}</td>
                                                <td>{teacher.maxPeriodsPerWeek}</td>

                                                <td>
                                                    {teacher.userId === null
                                                        ? 'Not linked'
                                                        : 'Linked'}
                                                </td>

                                                <td>
                                                    <span
                                                        className={`rooms-status ${teacher.enabled ? 'is-active' : ''
                                                            }`}
                                                    >
                                                        {teacher.enabled ? 'Active' : 'Inactive'}
                                                    </span>
                                                </td>

                                                <td>
                                                    <div className="rooms-actions">
                                                        <button
                                                            className="rooms-button"
                                                            type="button"
                                                            disabled={busy}
                                                            aria-label={`Edit ${teacher.fullName}`}
                                                            onClick={() => startEditing(teacher)}
                                                        >
                                                            Edit
                                                        </button>
                                                        {user.role === 'ADMIN' && (
                                                            <button
                                                                className="rooms-button"
                                                                type="button"
                                                                disabled={busy}
                                                                onClick={() => {
                                                                    setError('')
                                                                    setSuccess('')
                                                                    setAccountTeacher(teacher)
                                                                }}
                                                            >
                                                                {teacher.userId === null
                                                                    ? 'Link account'
                                                                    : 'Change account'}
                                                            </button>
                                                        )}

                                                        <button
                                                            className="rooms-button"
                                                            type="button"
                                                            disabled={busy}
                                                            aria-label={`${teacher.enabled
                                                                ? 'Deactivate'
                                                                : 'Activate'
                                                                } ${teacher.fullName}`}
                                                            onClick={() => void toggleStatus(teacher)}
                                                        >
                                                            {teacher.enabled
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
                            aria-label="Teacher directory pages"
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