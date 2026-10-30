import { Link } from 'react-router'
import '../../styles/footer.css'

export default function Footer() {
  return (
    <footer className="allocator-footer">
      <div className="footer-columns">
        <div className="footer-about">
          <a className="footer-brand" href="#home">
            <span className="footer-logo" aria-hidden="true">▦</span>
            <span>CLASSROOM<br />ALLOCATOR</span>
          </a>

          <p>
            A clearer way to plan classrooms, organise timetables
            and make better use of campus spaces.
          </p>

          <span className="footer-tag">
            MAKE ROOM FOR LEARNING.
          </span>
        </div>

        <nav className="footer-links" aria-label="Footer quick links">
          <h2>QUICK LINKS</h2>
          <a href="#home">Home</a>
          <a href="#features">Features</a>
          <a href="#workflow">How it works</a>
          <a href="#roles">User roles</a>
        </nav>

        <nav className="footer-links" aria-label="Explore the platform">
          <h2>EXPLORE</h2>
          <a href="#preview">Sample timetable</a>
          <a href="#allocation-demo">Allocation demo</a>
          <Link to="/login">Sign In</Link>
        </nav>

        <div className="footer-access-card">
          <span className="footer-card-label">YOUR CAMPUS, ORGANISED</span>

          <h2>Every class.<br />In the right place.</h2>

          <p>
            Explore classroom planning and take the next step
            towards a clearer schedule.
          </p>

          <Link className="footer-signin" to="/login">
            <span>Sign In</span>
            <span className="footer-arrow" aria-hidden="true">↗</span>
          </Link>
        </div>
      </div>

      <div className="footer-bottom">
        <p>© {new Date().getFullYear()} Classroom Allocator · Academic project</p>

        <div className="footer-colour-dots" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>

        <a href="#home">Back to top ↑</a>
      </div>
    </footer>
  )
}