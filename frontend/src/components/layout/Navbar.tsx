import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import '../../styles/navbar.css'
import classroomLogo from '../../assets/images/classroom-logo.jpeg'

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false)
  const menuButton = useRef<HTMLButtonElement>(null)

  function closeMenu() {
    setMenuOpen(false)
  }

  useEffect(() => {
    if (!menuOpen) return

    function handleEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setMenuOpen(false)
        menuButton.current?.focus()
      }
    }

    window.addEventListener('keydown', handleEscape)

    return () => {
      window.removeEventListener('keydown', handleEscape)
    }
  }, [menuOpen])

  return (
    <header className="site-header">
      <Link className="site-brand" to="/" onClick={closeMenu}>
        <span className="brand-symbol" aria-hidden="true">
  <img className="brand-image" src={classroomLogo} alt="" />
</span>
        <span>CLASSROOM<br />ALLOCATOR</span>
      </Link>

      <nav
        id="main-navigation"
        className={`site-nav${menuOpen ? ' is-open' : ''}`}
        aria-label="Main navigation"
      >
        <a href="#features" onClick={closeMenu}>
          Features
        </a>

        <a href="#workflow" onClick={closeMenu}>
          How it works
        </a>

        <a href="#preview" onClick={closeMenu}>
          Explore demo ↗
        </a>
      </nav>

      <div className="header-actions">
        <Link
          className="neo-button yellow header-signin"
          to="/login"
          onClick={closeMenu}
        >
          Sign In ↗
        </Link>

        <button
          ref={menuButton}
          className="mobile-menu-button"
          type="button"
          aria-expanded={menuOpen}
          aria-controls="main-navigation"
          onClick={() => setMenuOpen((value) => !value)}
        >
          {menuOpen ? 'Close ✕' : 'Menu ☰'}
        </button>
      </div>
    </header>
  )
}