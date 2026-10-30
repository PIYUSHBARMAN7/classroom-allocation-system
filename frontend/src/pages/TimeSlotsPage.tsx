import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'

import {
  TimeSlotApiError,
  createTimeSlot,
  getTimeSlots,
  setTimeSlotStatus,
  updateTimeSlot,
} from '../services/timeSlotService'

import {
  slotTypeLabels,
  weekdays,
} from '../types/timeSlot'

import type {
  SlotType,
  TimeSlot,
  TimeSlotInput,
  TimeSlotPage,
} from '../types/timeSlot'

import '../styles/classrooms.css'

export default function TimeSlotsPage() {
  const navigate = useNavigate()
  const mutationLock = useRef(false)
  const headingRef = useRef<HTMLHeadingElement>(null)

  const [data, setData] = useState<TimeSlotPage | null>(null)
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState('all')
  const [refresh, setRefresh] = useState(0)

  const [editing, setEditing] = useState<TimeSlot | null>(null)
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
        const result = await getTimeSlots(
          page,
          enabled,
          controller.signal,
        )

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (controller.signal.aborted) return

        if (err instanceof TimeSlotApiError && err.status === 401) {
          void navigate('/login', { replace: true })
          return
        }

        setLoadError(
          err instanceof Error
            ? err.message
            : 'Unable to load time slots.',
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
    if (err instanceof TimeSlotApiError && err.status === 401) {
      void navigate('/login', { replace: true })
      return
    }

    setError(
      err instanceof TypeError
        ? 'Could not confirm the change because the connection failed. Reload the directory before retrying.'
        : err instanceof Error
          ? err.message
          : 'Unable to save the time slot.',
    )
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (mutationLock.current) return

    const form = event.currentTarget
    const values = new FormData(form)

    const input: TimeSlotInput = {
      label: String(values.get('label') ?? '').trim(),
      dayOfWeek: Number(values.get('dayOfWeek')),
      startTime: String(values.get('startTime') ?? ''),
      endTime: String(values.get('endTime') ?? ''),
      slotType: String(values.get('slotType')) as SlotType,
    }

    setError('')
    setSuccess('')

    if (!input.label) {
      setError('Enter a period or break label.')
      return
    }

    if (
      !/^\d{2}:\d{2}$/.test(input.startTime) ||
      !/^\d{2}:\d{2}$/.test(input.endTime)
    ) {
      setError('Choose start and end times using whole minutes.')
      return
    }

    if (input.startTime >= input.endTime) {
      setError('End time must be after start time.')
      return
    }

    mutationLock.current = true
    setBusy(true)

    try {
      const saved = editing
        ? await updateTimeSlot(editing.id, input, editing.version)
        : await createTimeSlot(input)

      setSuccess(
        `${saved.label} ${editing ? 'updated' : 'added'} successfully.`,
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

  function startEditing(slot: TimeSlot) {
    setEditing(slot)
    setError('')
    setSuccess('')
    headingRef.current?.focus()
  }

  async function toggleStatus(slot: TimeSlot) {
    if (mutationLock.current) return

    mutationLock.current = true
    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const saved = await setTimeSlotStatus(
        slot.id,
        !slot.enabled,
        slot.version,
      )

      if (editing?.id === slot.id) {
        setEditing(null)
      }

      setSuccess(
        `${saved.label} ${saved.enabled ? 'activated' : 'deactivated'}.`,
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
        <p className="rooms-eyebrow">SCHEDULE MANAGEMENT</p>
        <h1>Time slots</h1>
        <p>Define your shared weekly teaching periods and breaks.</p>
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
          {editing ? `Edit ${editing.label}` : 'Add a time slot'}
        </h2>

        <form
          key={editing ? `${editing.id}-${editing.version}` : 'new'}
          onSubmit={handleSubmit}
        >
          <fieldset className="rooms-fieldset" disabled={busy}>
            <legend className="rooms-sr-only">Time slot details</legend>

            <div className="rooms-form-grid">
              <label>
                Label
                <input
                  name="label"
                  defaultValue={editing?.label ?? ''}
                  placeholder="Period 1 or Lunch break"
                  maxLength={60}
                  required
                />
              </label>

              <label>
                Day
                <select
                  name="dayOfWeek"
                  defaultValue={editing?.dayOfWeek ?? 1}
                  required
                >
                  {weekdays.map((day) => (
                    <option key={day.value} value={day.value}>
                      {day.label}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Start time
                <input
                  name="startTime"
                  type="time"
                  defaultValue={
                    editing?.startTime.slice(0, 5) ?? '09:00'
                  }
                  min="00:00"
                  max="23:59"
                  step={60}
                  required
                />
              </label>

              <label>
                End time
                <input
                  name="endTime"
                  type="time"
                  defaultValue={
                    editing?.endTime.slice(0, 5) ?? '10:00'
                  }
                  min="00:00"
                  max="23:59"
                  step={60}
                  required
                />
              </label>

              <label>
                Slot type
                <select
                  name="slotType"
                  defaultValue={editing?.slotType ?? 'TEACHING'}
                  required
                >
                  <option value="TEACHING">Teaching period</option>
                  <option value="BREAK">Break</option>
                </select>
              </label>
            </div>

            <p>
              Use campus local time. Active periods cannot overlap on
              the same day. Breaks will be excluded from allocation.
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
                    : 'Add time slot'}
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
          <h2>Weekly period directory</h2>

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
                <option value="all">All slots</option>
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

        {loading && <p role="status">Loading time slots…</p>}

        {!loading && data && (
          <>
            <p role="status">
              {data.totalElements} time slot
              {data.totalElements === 1 ? '' : 's'} found
            </p>

            {data.content.length === 0 ? (
              <p>
                No slots on this page. Add a period, change the filter,
                or return to the previous page.
              </p>
            ) : (
              <div className="rooms-table-scroll">
                <table className="rooms-table">
                  <caption className="rooms-sr-only">
                    Weekly teaching periods and breaks
                  </caption>

                  <thead>
                    <tr>
                      <th scope="col">Day</th>
                      <th scope="col">Label</th>
                      <th scope="col">Start</th>
                      <th scope="col">End</th>
                      <th scope="col">Type</th>
                      <th scope="col">Status</th>
                      <th scope="col">Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {data.content.map((slot) => (
                      <tr key={slot.id}>
                        <td>
                          {weekdays.find(
                            (day) => day.value === slot.dayOfWeek,
                          )?.label ?? 'Unknown day'}
                        </td>
                        <th scope="row">{slot.label}</th>
                        <td>{slot.startTime.slice(0, 5)}</td>
                        <td>{slot.endTime.slice(0, 5)}</td>
                        <td>{slotTypeLabels[slot.slotType]}</td>

                        <td>
                          <span
                            className={`rooms-status ${
                              slot.enabled ? 'is-active' : ''
                            }`}
                          >
                            {slot.enabled ? 'Active' : 'Inactive'}
                          </span>
                        </td>

                        <td>
                          <div className="rooms-actions">
                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`Edit ${slot.label}, day ${slot.dayOfWeek}`}
                              onClick={() => startEditing(slot)}
                            >
                              Edit
                            </button>

                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`${
                                slot.enabled ? 'Deactivate' : 'Activate'
                              } ${slot.label}, day ${slot.dayOfWeek}`}
                              onClick={() => void toggleStatus(slot)}
                            >
                              {slot.enabled ? 'Deactivate' : 'Activate'}
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
              aria-label="Time slot directory pages"
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