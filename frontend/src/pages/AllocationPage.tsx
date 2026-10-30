import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import {
    createAllocationDraft,
    generateAllocationPreview,
    getAllocationRequirements,
} from '../services/allocationService'
import type {
    AllocationDraftCreated,
    AllocationPreview,
    AllocationRequirementOption,
} from '../services/allocationService'
import '../styles/allocation.css'

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

export default function AllocationPage() {
    const [requirements, setRequirements] = useState<
        AllocationRequirementOption[]
    >([])
    const [selected, setSelected] = useState<
        Map<number, AllocationRequirementOption>
    >(new Map())

    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)
    const [refresh, setRefresh] = useState(0)
    const [loading, setLoading] = useState(true)
    const [busy, setBusy] = useState(false)
    const [loadError, setLoadError] = useState('')
    const [previewError, setPreviewError] = useState('')
    const [preview, setPreview] = useState<AllocationPreview | null>(null)

    const previewController = useRef<AbortController | null>(null)
    const [draftName, setDraftName] = useState('')
    const [saving, setSaving] = useState(false)
    const [savedDraft, setSavedDraft] =
        useState<AllocationDraftCreated | null>(null)

    const selectedRequirements = [...selected.values()]
    const sessionCount = selectedRequirements.reduce(
        (total, requirement) => total + requirement.sessionsPerWeek,
        0,
    )

    useEffect(() => {
        const controller = new AbortController()


        async function loadRequirements() {
            setLoading(true)
            setLoadError('')

            try {
                const result = await getAllocationRequirements(
                    page,
                    controller.signal,
                )

                if (controller.signal.aborted) return

                setRequirements(result.content)
                setTotalPages(result.totalPages)
            } catch (error) {
                if (controller.signal.aborted) return

                setRequirements([])
                setLoadError(
                    error instanceof Error
                        ? error.message
                        : 'Unable to load class requirements.',
                )
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false)
                }
            }
        }

        void loadRequirements()

        return () => controller.abort()
    }, [page, refresh])

    useEffect(() => {
        return () => previewController.current?.abort()
    }, [])

    function toggleRequirement(
        requirement: AllocationRequirementOption,
    ) {
        setSelected((current) => {
            const updated = new Map(current)

            if (updated.has(requirement.id)) {
                updated.delete(requirement.id)
            } else {
                updated.set(requirement.id, requirement)
            }

            return updated
        })

        setPreview(null)
        setPreviewError('')
    }

    function clearSelection() {
        setSelected(new Map())
        setPreview(null)
        setPreviewError('')
    }

    async function handleGenerate() {
        if (previewController.current !== null) return

        if (selected.size === 0) {
            setPreviewError('Select at least one class requirement.')
            return
        }

        if (selected.size > 25 || sessionCount > 50) {
            setPreviewError(
                'Choose no more than 25 requirements and 50 weekly sessions.',
            )
            return
        }

        const controller = new AbortController()
        previewController.current = controller

        setBusy(true)
        setPreview(null)
        setPreviewError('')

        try {
            const result = await generateAllocationPreview(
                [...selected.keys()],
                controller.signal,
            )

            if (!controller.signal.aborted) {
                setPreview(result)
            }
        } catch (error) {
            if (!controller.signal.aborted) {
                setPreviewError(
                    error instanceof Error
                        ? error.message
                        : 'Unable to generate the preview.',
                )
            }
        } finally {
            if (previewController.current === controller) {
                previewController.current = null
            }

            if (!controller.signal.aborted) {
                setBusy(false)
            }
        }
    }

    function requirementLabel(id: number) {
        const requirement = selected.get(id)

        return requirement
            ? `${requirement.section.code} · ${requirement.subject.name}`
            : `Requirement #${id}`
    }
    async function handleSaveDraft() {
        if (previewController.current !== null || savedDraft !== null) {
            return
        }

        const name = draftName.trim()

        if (!name || name.length > 120) {
            setPreviewError('Enter a draft name between 1 and 120 characters.')
            return
        }

        if (
            !preview ||
            selected.size === 0 ||
            selected.size > 25 ||
            sessionCount > 50
        ) {
            setPreviewError(
                'Generate a preview with at most 25 requirements and 50 weekly sessions.',
            )
            return
        }

        const controller = new AbortController()
        previewController.current = controller

        setBusy(true)
        setSaving(true)
        setPreviewError('')

        try {
            const saved = await createAllocationDraft(
                name,
                [...selected.keys()],
                controller.signal,
            )

            if (!controller.signal.aborted) {
                setSavedDraft(saved)
                setPreview(saved.result)
            }
        } catch (error) {
            if (!controller.signal.aborted) {
                setPreviewError(
                    error instanceof Error
                        ? error.message
                        : 'Unable to save the draft. Please try again.',
                )
            }
        } finally {
            if (previewController.current === controller) {
                previewController.current = null
            }

            if (!controller.signal.aborted) {
                setBusy(false)
                setSaving(false)
            }
        }
    }
    return (
        <main className="allocation-page">
            <Link className="allocation-back" to="/dashboard">
                ← Back to dashboard
            </Link>

            <header className="allocation-heading">
                <p className="allocation-eyebrow">PLAN YOUR CLASSROOMS</p>
                <h1>Allocation preview</h1>
                <p>
                    Select requirements for one weekly timetable, then generate
                    a proposed allocation.
                </p>
            </header>

            <p className="allocation-notice">
                Preview only. Nothing is saved or published. Existing timetables
                and availability blocks are not checked yet.
            </p>

            <section
                className="allocation-panel"
                aria-labelledby="requirements-heading"
            >
                <div className="allocation-section-heading">
                    <h2 id="requirements-heading">Choose class requirements</h2>

                    <button
                        className="allocation-button"
                        type="button"
                        disabled={loading || busy}
                        onClick={() => setRefresh((value) => value + 1)}
                    >
                        Refresh list
                    </button>
                </div>

                {loadError && (
                    <p className="allocation-error" role="alert">
                        {loadError}
                    </p>
                )}

                {loading ? (
                    <p role="status">Loading requirements…</p>
                ) : requirements.length === 0 ? (
                    <p>No requirements are available on this page.</p>
                ) : (
                    <ul className="allocation-requirement-list">
                        {requirements.map((requirement) => {
                            const checked = selected.has(requirement.id)

                            const limitReached =
                                !checked &&
                                (selected.size >= 25 ||
                                    sessionCount + requirement.sessionsPerWeek > 50)

                            return (
                                <li key={requirement.id}>
                                    <label
                                        className={`allocation-option ${checked ? 'is-selected' : ''
                                            }`}
                                    >
                                        <input
                                            type="checkbox"
                                            checked={checked}
                                            disabled={busy || limitReached}
                                            onChange={() => toggleRequirement(requirement)}
                                        />

                                        <span>
                                            <strong>
                                                {requirement.section.code} ·{' '}
                                                {requirement.subject.name}
                                            </strong>

                                            <span className="allocation-option-detail">
                                                {requirement.teacher.fullName}
                                            </span>

                                            <span className="allocation-option-detail">
                                                {requirement.sessionsPerWeek} sessions/week ·{' '}
                                                {requirement.periodsPerSession} periods/session
                                            </span>
                                        </span>
                                    </label>
                                </li>
                            )
                        })}
                    </ul>
                )}

                <div className="allocation-pagination">
                    <button
                        className="allocation-button"
                        type="button"
                        disabled={loading || busy || page === 0}
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
                            loading ||
                            busy ||
                            totalPages === 0 ||
                            page + 1 >= totalPages
                        }
                        onClick={() => setPage((value) => value + 1)}
                    >
                        Next
                    </button>
                </div>
            </section>

            <section
                className="allocation-panel allocation-selection"
                aria-labelledby="selection-heading"
            >
                <h2 id="selection-heading">Your selection</h2>

                <p role="status">
                    {selected.size}/25 requirements · {sessionCount}/50 weekly sessions
                </p>

                {selectedRequirements.length > 0 && (
                    <ul className="allocation-selected-list">
                        {selectedRequirements.map((requirement) => (
                            <li key={requirement.id}>
                                <span>{requirementLabel(requirement.id)}</span>

                                <button
                                    className="allocation-button"
                                    type="button"
                                    disabled={busy}
                                    aria-label={`Remove ${requirementLabel(requirement.id)}`}
                                    onClick={() => toggleRequirement(requirement)}
                                >
                                    Remove
                                </button>
                            </li>
                        ))}
                    </ul>
                )}

                <div className="allocation-actions">
                    <button
                        className="allocation-button allocation-primary"
                        type="button"
                        disabled={busy || selected.size === 0}
                        onClick={() => void handleGenerate()}
                    >
                        {busy ? 'Generating…' : 'Generate preview'}
                    </button>

                    <button
                        className="allocation-button"
                        type="button"
                        disabled={busy || selected.size === 0}
                        onClick={clearSelection}
                    >
                        Clear selection
                    </button>
                </div>

                {busy && <p role="status">Preparing your allocation preview…</p>}

                {previewError && (
                    <p className="allocation-error" role="alert">
                        {previewError}
                    </p>
                )}
            </section>

            {preview && (
                <section className="allocation-panel">
                    <h2>Save your draft</h2>

                    <p>
                        Saving generates a fresh allocation using current campus data.
                        Review the saved results below.
                    </p>

                    <form
                        className="allocation-draft-form"
                        onSubmit={(event) => {
                            event.preventDefault()
                            void handleSaveDraft()
                        }}
                    >
                        <label htmlFor="allocation-draft-name">
                            Draft name
                        </label>

                        <input
                            id="allocation-draft-name"
                            type="text"
                            placeholder="BCA Semester III - Draft 1"
                            value={draftName}
                            onChange={(event) => {
                                setDraftName(event.target.value)
                                setSavedDraft(null)
                            }}
                            maxLength={120}
                            required
                            disabled={busy}
                        />

                        <button
                            className="allocation-button allocation-primary"
                            type="submit"
                            disabled={busy || !draftName.trim() || savedDraft !== null}
                        >
                            {saving
                                ? 'Saving draft…'
                                : savedDraft
                                    ? 'Draft saved'
                                    : 'Save draft'}
                        </button>
                    </form>

                    {savedDraft && (
                        <p className="allocation-success" role="status">
                            Saved “{savedDraft.name}” as draft #{savedDraft.id}.
                        </p>
                    )}
                </section>
            )}

            {preview && (
                <section
                    className="allocation-panel"
                    aria-labelledby="preview-heading"
                >
                    <h2 id="preview-heading">Preview results</h2>

                    <p className="allocation-result-summary" role="status">
                        {preview.allocations.length} sessions allocated ·{' '}
                        {preview.unallocated.length} sessions unallocated
                    </p>

                    {preview.allocations.length > 0 && (
                        <div className="allocation-table-scroll">
                            <table className="allocation-table">
                                <caption>Proposed classroom assignments</caption>

                                <thead>
                                    <tr>
                                        <th scope="col">Class</th>
                                        <th scope="col">Day</th>
                                        <th scope="col">Time</th>
                                        <th scope="col">Room ID</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {preview.allocations.map((session, index) => (
                                        <tr
                                            key={`${session.requirementId}-${session.dayOfWeek}-${session.startTime}-${index}`}
                                        >
                                            <td>{requirementLabel(session.requirementId)}</td>
                                            <td>{weekdays[session.dayOfWeek]}</td>
                                            <td>
                                                {session.startTime.slice(0, 5)}–{session.endTime.slice(0, 5)}
                                            </td>
                                            <td>#{session.roomId}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {preview.unallocated.length > 0 && (
                        <div className="allocation-unallocated">
                            <h3>Sessions needing attention</h3>

                            <ul>
                                {preview.unallocated.map((session) => (
                                    <li
                                        key={`${session.requirementId}-${session.sessionNumber}`}
                                    >
                                        <strong>
                                            {requirementLabel(session.requirementId)} · Session{' '}
                                            {session.sessionNumber}
                                        </strong>
                                        <p>{session.reason}</p>
                                    </li>
                                ))}
                            </ul>

                            <p>
                                Another arrangement may work. Review room suitability,
                                teaching periods and teacher workload limits.
                            </p>
                        </div>
                    )}
                </section>
            )}
        </main>
    )
}