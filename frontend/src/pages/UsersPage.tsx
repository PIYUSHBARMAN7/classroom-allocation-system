import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { createUser, UserRequestError } from '../services/userService'
import type { CurrentUser } from '../services/authService'
import '../styles/login.css'

export default function UsersPage() {
  const navigate = useNavigate()

  const [role, setRole] = useState<CurrentUser['role']>('TEACHER')
  const [showPassword, setShowPassword] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (busy) return

    const form = event.currentTarget
    const data = new FormData(form)

    const displayName = String(data.get('displayName') ?? '').trim()
    const email = String(data.get('email') ?? '').trim()
    const password = String(data.get('password') ?? '')

    setError('')
    setSuccess('')

    if (!displayName) {
      setError('Enter a name.')
      return
    }

    if (
      Array.from(password).length < 15 ||
      new TextEncoder().encode(password).length > 72
    ) {
      setError(
        'Use a password with at least 15 characters and at most 72 UTF-8 bytes.',
      )
      return
    }

    setBusy(true)

    try {
      const user = await createUser({
        displayName,
        email,
        password,
        role,
      })

      form.reset()
      setRole('TEACHER')
      setShowPassword(false)

      setSuccess(
        `Created ${user.displayName} (${user.email}) with role ${user.role}.`,
      )
    } catch (err) {
      if (err instanceof UserRequestError && err.status === 401) {
        navigate('/login', { replace: true })
        return
      }

      setError(
        err instanceof TypeError
          ? 'Cannot confirm account creation. Check the connection before retrying.'
          : err instanceof Error
            ? err.message
            : 'Unable to create the account.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="login-page">
      <Link className="back-link" to="/dashboard">
        ← Back to dashboard
      </Link>

      <section className="neo-panel login-card" aria-busy={busy}>
        <span className="sample-badge">ADMINISTRATION</span>
        <h1>Create an account.</h1>
        <p>Assign the access this person needs for their work.</p>

        {error && (
          <p className="login-error" role="alert">
            {error}
          </p>
        )}

        {success && (
          <p className="login-notice" role="status">
            {success}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="user-name">Full name</label>
            <input
              id="user-name"
              name="displayName"
              type="text"
              maxLength={100}
              required
              disabled={busy}
            />
          </div>

          <div className="field">
            <label htmlFor="user-email">Institution email</label>
            <input
              id="user-email"
              name="email"
              type="email"
              maxLength={254}
              autoCapitalize="none"
              spellCheck={false}
              required
              disabled={busy}
            />
          </div>

          <div className="field">
            <label htmlFor="user-role">Account role</label>
            <select
              id="user-role"
              value={role}
              onChange={(event) =>
                setRole(event.target.value as CurrentUser['role'])
              }
              disabled={busy}
            >
              <option value="TEACHER">Teacher</option>
              <option value="STUDENT">Student</option>
              <option value="COORDINATOR">Coordinator</option>
              <option value="ADMIN">Administrator</option>
            </select>

            {role === 'ADMIN' && (
              <p className="login-help">
                Administrators can create accounts and assign roles.
              </p>
            )}
          </div>

          <div className="field">
            <label htmlFor="user-password">Initial password</label>

            <div className="password-field">
              <input
                id="user-password"
                name="password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                minLength={15}
                maxLength={72}
                aria-describedby="user-password-help"
                required
                disabled={busy}
              />

              <button
                type="button"
                onClick={() => setShowPassword((value) => !value)}
                aria-controls="user-password"
                aria-pressed={showPassword}
                aria-label={
                  showPassword ? 'Hide password' : 'Show password'
                }
                disabled={busy}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>

            <p className="login-help" id="user-password-help">
              Use a unique password of at least 15 characters.
              This form does not email credentials.
            </p>
          </div>

          <button
            className="neo-button yellow login-submit"
            type="submit"
            disabled={busy}
          >
            {busy ? 'Creating account…' : 'Create Account'}
          </button>
        </form>
      </section>
    </main>
  )
}