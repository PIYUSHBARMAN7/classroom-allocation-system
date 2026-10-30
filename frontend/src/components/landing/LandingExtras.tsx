import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import Reveal from '../common/Reveal'
type Room = {
  name: string
  capacity: number
  type: 'Lecture Room' | 'Computer Lab'
}

const rooms: Room[] = [
  { name: 'Room 101', capacity: 30, type: 'Lecture Room' },
  { name: 'Room 201', capacity: 60, type: 'Lecture Room' },
  { name: 'Lecture Hall', capacity: 120, type: 'Lecture Room' },
  { name: 'Computer Lab 01', capacity: 30, type: 'Computer Lab' },
  { name: 'Computer Lab 02', capacity: 50, type: 'Computer Lab' },
]

const roles = [
  {
    name: 'Admin',
    description: 'Manage campus data, user access and timetable publishing.',
  },
  {
    name: 'Coordinator',
    description: 'Prepare allocations, review conflicts and adjust schedules.',
  },
  {
    name: 'Teacher',
    description: 'View assigned classes, rooms and teaching times.',
  },
  {
    name: 'Student',
    description: 'Find the published timetable for your section.',
  },
]

export default function LandingExtras() {
  const [classSize, setClassSize] = useState('45')
  const [roomType, setRoomType] =
    useState<Room['type']>('Computer Lab')

  // undefined = not searched; null = no match
  const [result, setResult] = useState<Room | null | undefined>()

  function findRoom(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const students = Number(classSize)

    if (!Number.isInteger(students) || students < 1 || students > 1000) {
      return
    }

    const matchingRoom = rooms
      .filter(
        (room) =>
          room.type === roomType && room.capacity >= students,
      )
      .sort((a, b) => a.capacity - b.capacity)[0]

    setResult(matchingRoom ?? null)
  }

  return (
    <>
      <section className="roles-section" id="roles">
        <p className="eyebrow">ONE CAMPUS. DIFFERENT RESPONSIBILITIES.</p>
        <h2>Built for every campus role.</h2>

        <div className="role-grid">
  {roles.map((role, index) => (
    <Reveal key={role.name} delay={index * 80}>
      <article className="neo-panel role-card">
        <span className="role-number">0{index + 1}</span>
        <h3>{role.name}</h3>
        <p>{role.description}</p>
      </article>
    </Reveal>
  ))}
</div>
      </section>

      <section className="neo-panel allocation-demo" id="allocation-demo">
        <div className="demo-heading">
          <span className="sample-badge">SAMPLE DEMO</span>
          <h2>Find a room that fits.</h2>
          <p>
            Try a class size and room type. This example selects
            the smallest fictional room with enough seats.
          </p>
        </div>

        <form className="demo-form" onSubmit={findRoom}>
          <div className="field">
            <label htmlFor="demo-size">Number of students</label>
            <input
              id="demo-size"
              type="number"
              min="1"
              max="1000"
              step="1"
              required
              value={classSize}
              onChange={(event) => {
                setClassSize(event.target.value)
                setResult(undefined)
              }}
            />
          </div>

          <div className="field">
            <label htmlFor="demo-type">Room type</label>
            <select
              id="demo-type"
              value={roomType}
              onChange={(event) => {
                setRoomType(event.target.value as Room['type'])
                setResult(undefined)
              }}
            >
              <option>Lecture Room</option>
              <option>Computer Lab</option>
            </select>
          </div>

          <button className="neo-button yellow" type="submit">
            Find a matching room ↗
          </button>
        </form>

        <div className="demo-feedback" role="status" aria-live="polite">
          {result === undefined && (
            <p>Choose your requirements, then find a matching room.</p>
          )}

          {result === null && (
            <div className="demo-result pink">
              <strong>No matching room in the sample data.</strong>
              <p>
                No {roomType.toLowerCase()} has enough seats for
                {' '}{classSize} students. Try a smaller class size
                or another room type.
              </p>
            </div>
          )}

          {result && (
            <div className="demo-result light-blue">
              <strong>Match found: {result.name}</strong>
              <p>
                {result.type} · {result.capacity} seats ·
                {' '}{Number(classSize)} students
              </p>
              <p>
                Why this room? It matches the room type and leaves
                {' '}{result.capacity - Number(classSize)} spare seats,
                the fewest among the suitable sample rooms.
              </p>
            </div>
          )}
        </div>

        <p className="sample-note">
          Fictional rooms only. This demo checks capacity and room type;
          it does not check dates, availability, or teacher and section
          clashes, and it does not save a booking.
        </p>
      </section>

      <section className="neo-panel final-banner">
        <div>
          <h2>Ready to organise your campus timetable?</h2>
          <p>One place to plan rooms, review allocations and view schedules.</p>
        </div>

        <Link className="neo-button yellow" to="/login">
          Sign In ↗
        </Link>
      </section>
    </>
  )
}