import { useState } from 'react'
import Navbar from '../components/layout/Navbar'
import LandingExtras from '../components/landing/LandingExtras'
import AnimatedBackground from '../components/layout/AnimatedBackground'
import '../styles/landing.css'
import AnnouncementTicker from '../components/layout/AnnouncementTicker'
import Reveal from '../components/common/Reveal'
import ClassroomMascot from '../components/landing/ClassroomMascot'
import Footer from '../components/layout/Footer'

const sampleTimetables = {
  'Section A': [
    ['09:00–10:00', 'Java · Room 201', 'Maths · Room 104', 'DBMS · Room 203'],
    ['10:00–11:00', 'DBMS · Room 203', 'Free period', 'Java · Room 201'],
    ['11:00–12:00', 'Java Lab · Lab 02', 'Java · Room 201', 'Free period'],
  ],
  'Section B': [
    ['09:00–10:00', 'Maths · Room 104', 'Java · Room 201', 'Free period'],
    ['10:00–11:00', 'Java · Room 201', 'DBMS · Room 203', 'Maths · Room 104'],
    ['11:00–12:00', 'DBMS · Room 203', 'Free period', 'Java Lab · Lab 02'],
  ],
}

type Section = keyof typeof sampleTimetables

export default function LandingPage() {
  const [section, setSection] = useState<Section>('Section A')

  return (
    <div className="landing">
      <AnimatedBackground />
      <AnnouncementTicker />
      <Navbar />

      <main id="home">
        <section className="hero-banner">
  <div className="hero-copy">
    <span className="hero-label">
      ROOMS. CLASSES. SORTED.
    </span>

    <h1>
      Every class deserves
      <br />
      the right classroom.
    </h1>

    <p>
      Plan classroom allocations, check timetable conflicts,
      and keep your campus schedule organised in one place.
    </p>

    <div className="hero-actions">
      <a className="neo-button yellow" href="#preview">
        Explore sample timetable ↗
      </a>

      <a className="hero-link" href="#workflow">
        How it works ↓
      </a>
    </div>
  </div>

  <ClassroomMascot />
</section>

        <section id="preview" className="preview-section">
          <p className="eyebrow">A CLEARER WEEK AHEAD</p>
          <h2>Your schedule, at a glance.</h2>

          <div className="preview-layout">
            <aside className="neo-panel preview-controls">
              <h3 className="panel-heading">Timetable preview</h3>

              <div className="panel-body">
                <label htmlFor="section-selector">Choose a section</label>
                <select
                  id="section-selector"
                  value={section}
                  onChange={(event) =>
                    setSection(event.target.value as Section)
                  }
                >
                  <option>Section A</option>
                  <option>Section B</option>
                </select>

                <div className="info-card yellow">
                  <strong>Built around your classes</strong>
                  <p>
                    Room capacity, facilities and availability
                    guide classroom selection.
                  </p>
                </div>

                <div className="info-card pink">
                  <strong>Keep conflicts visible</strong>
                  <p>
                    Review room, teacher and section clashes
                    before publishing a timetable.
                  </p>
                </div>
              </div>
            </aside>

            <div className="neo-panel timetable-panel">
              <div className="timetable-heading">
                <h3>Weekly timetable</h3>
                <span className="sample-badge">SAMPLE DATA</span>
              </div>

              <div className="panel-body">
                <p className="timetable-description" aria-live="polite">
                  BCA · Semester III · {section}
                </p>

                <div className="table-scroll">
                  <table>
                    <caption className="visually-hidden">
                      Fictional weekly timetable for {section}
                    </caption>

                    <thead>
                      <tr>
                        <th scope="col">Time</th>
                        <th scope="col">Monday</th>
                        <th scope="col">Tuesday</th>
                        <th scope="col">Wednesday</th>
                      </tr>
                    </thead>

                    <tbody>
                      {sampleTimetables[section].map((row) => (
                        <tr key={row[0]}>
                          <th scope="row">{row[0]}</th>

                          {row.slice(1).map((cell, index) => (
                            <td
                              key={`${row[0]}-${index}`}
                              className={
                                cell === 'Free period'
                                  ? 'free-period'
                                  : ['yellow', 'pink', 'light-blue'][index]
                              }
                            >
                              {cell}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                <p className="sample-note">
                  Fictional data for demonstration. Actual schedules
                  will be available after signing in.
                </p>
              </div>
            </div>
          </div>
        </section>

        <section id="features" className="features-section">
          <p className="eyebrow">BETTER PLANNING STARTS HERE</p>
          <h2>Make room for what matters.</h2>

          <div className="feature-grid">
          <Reveal>
            <article className="neo-panel feature-card yellow">
              <span className="feature-number">01</span>
              <h3>Suitable rooms</h3>
              <p>
                Match each class with enough seats and the facilities
                it needs, from lecture rooms to computer labs.
              </p>
            </article>
              </Reveal>

            <article className="neo-panel feature-card pink">
              <span className="feature-number">02</span>
              <h3>Clear conflict checks</h3>
              <p>
                Identify overlapping room, teacher and section
                bookings before confirming an allocation.
              </p>
            </article>

            <article className="neo-panel feature-card blue">
              <span className="feature-number">03</span>
              <h3>Organised timetables</h3>
              <p>
                Review allocations and publish schedules that
                teachers and students can easily follow.
              </p>
            </article>
          </div>
        </section>

        <section id="workflow" className="neo-panel workflow">
          <div>
            <p className="eyebrow">HOW IT WORKS</p>
            <h2>Plan. Review.<br />Publish.</h2>
            <p>From campus information to a clear classroom schedule.</p>
          </div>

          <ol>
            <li>
              <strong>Add campus data</strong>
              <p>Enter rooms, teachers, subjects, sections and time slots.</p>
            </li>
            <li>
              <strong>Allocate and review</strong>
              <p>Find suitable rooms and resolve scheduling conflicts.</p>
            </li>
            <li>
              <strong>Publish the timetable</strong>
              <p>Share the approved schedule with teachers and students.</p>
            </li>
          </ol>
        </section>
        <LandingExtras />
      </main>
      
      <Footer />
    </div>
  )
}