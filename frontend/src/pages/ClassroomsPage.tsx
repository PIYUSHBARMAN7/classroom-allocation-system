import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'

import {
  ClassroomApiError,
  createClassroom,
  getClassrooms,
  setClassroomStatus,
  updateClassroom,
} from '../services/classroomService'

import { roomTypeLabels } from '../types/classroom'
import type {
  Classroom,
  ClassroomInput,
  ClassroomPage,
  RoomType,
} from '../types/classroom'

import '../styles/classrooms.css'

export default function ClassroomsPage() {
  const navigate = useNavigate()

  const [data, setData] = useState<ClassroomPage | null>(null)
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState('all')
  const [refresh, setRefresh] = useState(0)

  const [editing, setEditing] = useState<Classroom | null>(null)
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
        const result = await getClassrooms(
          page,
          enabled,
          controller.signal,
        )

        if (!controller.signal.aborted) {
          setData(result)
        }
      } catch (err) {
        if (controller.signal.aborted) return

        if (err instanceof ClassroomApiError && err.status === 401) {
          void navigate('/login', { replace: true })
          return
        }

        setLoadError(
          err instanceof Error
            ? err.message
            : 'Unable to load classrooms.',
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
    if (err instanceof ClassroomApiError && err.status === 401) {
      void navigate('/login', { replace: true })
      return
    }

    setError(
      err instanceof Error
        ? err.message
        : 'Could not confirm the change. Reload the list before retrying.',
    )
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy) return

    const form = event.currentTarget
    const values = new FormData(form)

    const input: ClassroomInput = {
      code: String(values.get('code') ?? '').trim().toUpperCase(),
      name: String(values.get('name') ?? '').trim(),
      building: String(values.get('building') ?? '').trim(),
      floorNumber: Number(values.get('floorNumber')),
      capacity: Number(values.get('capacity')),
      roomType: String(values.get('roomType')) as RoomType,
      hasProjector: values.get('hasProjector') === 'on',
      accessible: values.get('accessible') === 'on',
    }

    if (!input.name || !input.building) {
      setError('Room name and building cannot be blank.')
      return
    }

    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const saved = editing
        ? await updateClassroom(editing.id, input, editing.version)
        : await createClassroom(input)

      setSuccess(
        editing
          ? `${saved.code} updated successfully.`
          : `${saved.code} added successfully.`,
      )

      form.reset()
      setEditing(null)
      setPage(0)
      setRefresh((value) => value + 1)
    } catch (err) {
      handleError(err)
    } finally {
      setBusy(false)
    }
  }

  function startEditing(room: Classroom) {
    setEditing(room)
    setError('')
    setSuccess('')

    document.getElementById('classroom-form-title')?.focus()
  }

  function cancelEditing() {
    setEditing(null)
    setError('')
  }

  async function toggleStatus(room: Classroom) {
    if (busy) return

    setBusy(true)
    setError('')
    setSuccess('')

    try {
      const saved = await setClassroomStatus(
        room.id,
        !room.enabled,
        room.version,
      )

      if (editing?.id === room.id) {
        setEditing(null)
      }

      setSuccess(
        `${saved.code} ${saved.enabled ? 'activated' : 'deactivated'}.`,
      )

      setRefresh((value) => value + 1)
    } catch (err) {
      handleError(err)
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="classrooms-page">
      <header className="rooms-header">
        <div>
          <Link to="/dashboard">← Dashboard</Link>
          <p className="rooms-eyebrow">CAMPUS MANAGEMENT</p>
          <h1>Classrooms</h1>
          <p>Manage room details, capacity and facilities.</p>
        </div>
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
        <h2 id="classroom-form-title" tabIndex={-1}>
          {editing ? `Edit ${editing.code}` : 'Add a classroom'}
        </h2>

        <form
          key={editing ? `${editing.id}-${editing.version}` : 'new'}
          onSubmit={handleSubmit}
        >
          <fieldset disabled={busy} className="rooms-fieldset">
            <legend className="rooms-sr-only">Classroom details</legend>

            <div className="rooms-form-grid">
              <label>
                Room code
                <input
                  name="code"
                  defaultValue={editing?.code ?? ''}
                  placeholder="BLOCK-A-201"
                  pattern="[A-Za-z0-9][A-Za-z0-9_\-]{0,29}"
                  title="Use letters, numbers, hyphens or underscores."
                  maxLength={30}
                  required
                />
              </label>

              <label>
                Room name
                <input
                  name="name"
                  defaultValue={editing?.name ?? ''}
                  placeholder="Classroom 201"
                  maxLength={100}
                  required
                />
              </label>

              <label>
                Building
                <input
                  name="building"
                  defaultValue={editing?.building ?? ''}
                  placeholder="Block A"
                  maxLength={100}
                  required
                />
              </label>

              <label>
                Floor
                <input
                  name="floorNumber"
                  type="number"
                  defaultValue={editing?.floorNumber ?? 0}
                  min={-10}
                  max={200}
                  step={1}
                  required
                />
              </label>

              <label>
                Capacity
                <input
                  name="capacity"
                  type="number"
                  defaultValue={editing?.capacity ?? 60}
                  min={1}
                  max={2000}
                  step={1}
                  required
                />
              </label>

              <label>
                Room type
                <select
                  name="roomType"
                  defaultValue={editing?.roomType ?? 'LECTURE_HALL'}
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

            <div className="rooms-checkboxes">
              <label>
                <input
                  name="hasProjector"
                  type="checkbox"
                  defaultChecked={editing?.hasProjector ?? false}
                />
                Projector available
              </label>

              <label>
                <input
                  name="accessible"
                  type="checkbox"
                  defaultChecked={editing?.accessible ?? false}
                />
                Accessible room
              </label>
            </div>

            <div className="rooms-actions">
              <button className="rooms-button yellow" type="submit">
                {busy
                  ? 'Saving…'
                  : editing
                    ? 'Save changes'
                    : 'Add classroom'}
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

      <section className="rooms-panel" aria-busy={loading || busy}>
        <div className="rooms-toolbar">
          <h2>Room directory</h2>

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
                <option value="all">All rooms</option>
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

        {loading && <p role="status">Loading classrooms…</p>}

        {!loading && data && (
          <>
            <p role="status">
              {data.totalElements} classroom
              {data.totalElements === 1 ? '' : 's'} found
            </p>

            {data.content.length === 0 ? (
              <p>
                No rooms on this page. Add a classroom, change the
                filter, or go to the previous page.
              </p>
            ) : (
              <div className="rooms-table-scroll">
                <table className="rooms-table">
                  <caption className="rooms-sr-only">
                    Classroom details and management actions
                  </caption>
                  <thead>
                    <tr>
                      <th scope="col">Room</th>
                      <th scope="col">Location</th>
                      <th scope="col">Capacity</th>
                      <th scope="col">Type</th>
                      <th scope="col">Facilities</th>
                      <th scope="col">Status</th>
                      <th scope="col">Actions</th>
                    </tr>
                  </thead>

                  <tbody>
                    {data.content.map((room) => (
                      <tr key={room.id}>
                        <td>
                          <strong>{room.code}</strong>
                          <div>{room.name}</div>
                        </td>
                        <td>
                          {room.building}
                          <div>Floor {room.floorNumber}</div>
                        </td>
                        <td>{room.capacity}</td>
                        <td>{roomTypeLabels[room.roomType]}</td>
                        <td>
                          {[
                            room.hasProjector ? 'Projector' : '',
                            room.accessible ? 'Accessible' : '',
                          ]
                            .filter(Boolean)
                            .join(', ') || 'None listed'}
                        </td>
                        <td>
                          <span
                            className={`rooms-status ${
                              room.enabled ? 'is-active' : ''
                            }`}
                          >
                            {room.enabled ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td>
                          <div className="rooms-actions">
                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`Edit ${room.code}`}
                              onClick={() => startEditing(room)}
                            >
                              Edit
                            </button>
                            <button
                              className="rooms-button"
                              type="button"
                              disabled={busy}
                              aria-label={`${
                                room.enabled ? 'Deactivate' : 'Activate'
                              } ${room.code}`}
                              onClick={() => void toggleStatus(room)}
                            >
                              {room.enabled ? 'Deactivate' : 'Activate'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <nav className="rooms-pagination" aria-label="Classroom pages">
              <button
                className="rooms-button"
                type="button"
                disabled={busy || page === 0}
                onClick={() => setPage((value) => Math.max(0, value - 1))}
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