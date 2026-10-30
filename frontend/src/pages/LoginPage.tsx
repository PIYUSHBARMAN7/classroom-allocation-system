import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import {
  getCurrentUser,
  signIn,
  LoginRateLimitError,
} from '../services/authService'
import '../styles/login.css'

const accountRoles = [
  {
    name: 'Admin',
    description: 'Accounts and campus administration',
  },
  {
    name: 'Coordinator',
    description: 'Classroom planning and allocations',
  },
  {
    name: 'Teacher',
    description: 'Your assigned teaching timetable',
  },
  {
    name: 'Student',
    description: 'Your section’s published timetable',
  },
]

export default function LoginPage() {
  const navigate = useNavigate()

  const [selectedRole, setSelectedRole] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [checkingSession, setCheckingSession] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const [retryAt, setRetryAt] = useState<number | null>(null)
  const [retrySeconds, setRetrySeconds] = useState(0)

  useEffect(() => {
    if (retryAt === null) return

    function updateCountdown() {
      const remaining = Math.max(
        0,
        Math.ceil((retryAt! - Date.now()) / 1000),
      )

      setRetrySeconds(remaining)

      if (remaining === 0) {
        setRetryAt(null)
      }
    }

    updateCountdown()

    const intervalId = window.setInterval(updateCountdown, 250)

    return () => window.clearInterval(intervalId)
  }, [retryAt])

  useEffect(() => {
    const controller = new AbortController()

    async function checkSession() {
      try {
        const user = await getCurrentUser(controller.signal)

        if (!controller.signal.aborted && user) {
          navigate('/dashboard', { replace: true })
        }
      } catch {
        if (!controller.signal.aborted) {
          setError(
            'Cannot reach the sign-in service. Check that the backend is running.',
          )
        }
      } finally {
        if (!controller.signal.aborted) {
          setCheckingSession(false)
        }
      }
    }

    void checkSession()

    return () => controller.abort()
  }, [navigate])

  function handleRoleSelection(roleName: string) {
    setSelectedRole(roleName)
    document.getElementById('login-email')?.focus()
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (
      busy ||
      checkingSession ||
      (retryAt !== null && Date.now() < retryAt)
    ) {
      return
    }

    setBusy(true)
    setError('')

    try {
      // The backend determines the account's actual role.
      // selectedRole is a visual preference, not a permission.
      await signIn(email, password)

      setPassword('')
      setShowPassword(false)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      if (err instanceof LoginRateLimitError) {
        setError('')
        setRetrySeconds(err.retryAfterSeconds)
        setRetryAt(Date.now() + err.retryAfterSeconds * 1000)
      } else {
        setError(
          err instanceof Error
            ? err.message
            : 'Unable to sign in. Please try again.',
        )
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="login-page">
      <Link className="back-link" to="/">
        ← Back to home
      </Link>

      <section
        className="neo-panel login-card"
        aria-busy={checkingSession || busy}
      >
        <span className="sample-badge">CLASSROOM ALLOCATOR</span>

        <h1>Welcome back.</h1>

        <p>
          Choose a role card, then sign in with your institution-issued
          account.
        </p>

        <ul
          className="login-role-grid"
          aria-label="Account role preference"
        >
          {accountRoles.map((role) => (
            <li key={role.name}>
              <button
                className={`login-role-card ${selectedRole === role.name ? 'is-selected' : ''
                  }`}
                type="button"
                aria-pressed={selectedRole === role.name}
                disabled={checkingSession || busy}
                onClick={() => handleRoleSelection(role.name)}
              >
                <strong>{role.name}</strong>
                <span>{role.description}</span>
              </button>
            </li>
          ))}
        </ul>

        <p
          className="login-role-selection"
          role="status"
          aria-live="polite"
        >
          {selectedRole
            ? `Selected: ${selectedRole}. Enter your credentials below.`
            : 'You can also sign in directly using your account credentials.'}
        </p>

        <p className="login-notice" id="login-role-notice">
          Your account’s assigned role determines your workspace and
          permissions. Selecting a card does not change your access.
        </p>

        {error && (
          <p className="login-error" id="login-error" role="alert">
            {error}
          </p>
        )}

        {retrySeconds > 0 && (
          <p className="login-error" role="status">
            Too many sign-in attempts. Try again in {retrySeconds}{' '}
            {retrySeconds === 1 ? 'second' : 'seconds'}.
          </p>
        )}

        {checkingSession ? (
          <p role="status">Checking your session…</p>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="login-email">Institution email</label>

              <input
                id="login-email"
                name="email"
                type="email"
                placeholder="name@college.edu"
                autoComplete="username"
                autoCapitalize="none"
                spellCheck={false}
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                maxLength={254}
                required
                disabled={busy}
                aria-describedby={
                  error ? 'login-error' : 'login-role-notice'
                }
              />
            </div>

            <div className="field">
              <label htmlFor="login-password">Password</label>

              <div className="password-field">
                <input
                  id="login-password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                  disabled={busy}
                  aria-describedby={error ? 'login-error' : undefined}
                />

                <button
                  type="button"
                  onClick={() => setShowPassword((value) => !value)}
                  aria-controls="login-password"
                  aria-label={
                    showPassword ? 'Hide password' : 'Show password'
                  }
                  aria-pressed={showPassword}
                  disabled={busy}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <button
              className="neo-button yellow login-submit"
              type="submit"
              disabled={busy || retrySeconds > 0}
            >
              {busy
                ? 'Signing in…'
                : retrySeconds > 0
                  ? `Try again in ${retrySeconds}s`
                  : 'Sign In'}
            </button>
            <p className="login-help">
  <Link to="/forgot-password">Forgot your password?</Link>
</p>
          </form>
        )}

        <p className="login-help">
          Need an account or a role change? Contact your institution
          administrator.
        </p>
      </section>
    </main>
  )
}