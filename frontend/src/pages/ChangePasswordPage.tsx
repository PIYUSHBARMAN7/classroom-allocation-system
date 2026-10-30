import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import {
  changePassword,
  PasswordChangeError,
} from '../services/passwordService'
import { LoginRateLimitError } from '../services/authService'
import '../styles/login.css'

export default function ChangePasswordPage() {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [sessionExpired, setSessionExpired] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (busy) return

    const form = event.currentTarget
    const data = new FormData(form)

    const currentPassword = String(data.get('currentPassword') ?? '')
    const newPassword = String(data.get('newPassword') ?? '')
    const confirmation = String(data.get('confirmation') ?? '')

    setError('')

    if (newPassword !== confirmation) {
      setError('The new passwords do not match.')
      return
    }

    if (
      Array.from(newPassword).length < 15 ||
      new TextEncoder().encode(newPassword).length > 72
    ) {
      setError(
        'Use at least 15 characters and at most 72 UTF-8 bytes.',
      )
      return
    }

    if (newPassword === currentPassword) {
      setError('Choose a password different from your current password.')
      return
    }

    setBusy(true)

    try {
      await changePassword(currentPassword, newPassword)
      form.reset()
      setSuccess(true)
    } catch (err) {
      if (err instanceof LoginRateLimitError) {
        setError(
          `Too many attempts. Try again in ${err.retryAfterSeconds} seconds.`,
        )
      } else if (err instanceof PasswordChangeError) {
        setError(err.message)

        if (err.status === 401) {
          form.reset()
          setSessionExpired(true)
        }
      } else {
        setError(
          'Could not confirm the password change. Check your connection. If you already submitted it, try signing in with the new password before repeating.',
        )
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="login-page">
      {!success && (
        <Link className="back-link" to="/dashboard">
          ← Back to dashboard
        </Link>
      )}

      <section className="neo-panel login-card" aria-busy={busy}>
        <span className="sample-badge">ACCOUNT SECURITY</span>

        {success ? (
          <>
            <h1>Password changed.</h1>

            <p className="login-notice" role="status">
              Your password has been updated and this session has
              been signed out. Sign in again using your new password.
            </p>

            <Link className="neo-button yellow" to="/login">
              Sign In →
            </Link>
          </>
        ) : (
          <>
            <h1>Change your password.</h1>

            <p id="password-requirements">
              Choose a unique password with at least 15 characters.
              Maximum length is 72 UTF-8 bytes.
            </p>

            {error && (
              <p className="login-error" id="password-error" role="alert">
                {error}
              </p>
            )}

            {sessionExpired ? (
              <Link className="neo-button yellow" to="/login">
                Sign In Again →
              </Link>
            ) : (
              <form onSubmit={handleSubmit}>
                <div className="field">
                  <label htmlFor="current-password">
                    Current password
                  </label>

                  <input
                    id="current-password"
                    name="currentPassword"
                    type="password"
                    autoComplete="current-password"
                    maxLength={72}
                    required
                    disabled={busy}
                    aria-describedby={
                      error ? 'password-error' : undefined
                    }
                  />
                </div>

                <div className="field">
                  <label htmlFor="new-password">New password</label>

                  <input
                    id="new-password"
                    name="newPassword"
                    type="password"
                    autoComplete="new-password"
                    minLength={15}
                    maxLength={72}
                    required
                    disabled={busy}
                    aria-describedby="password-requirements"
                  />
                </div>

                <div className="field">
                  <label htmlFor="confirm-password">
                    Confirm new password
                  </label>

                  <input
                    id="confirm-password"
                    name="confirmation"
                    type="password"
                    autoComplete="new-password"
                    minLength={15}
                    maxLength={72}
                    required
                    disabled={busy}
                  />
                </div>

                <button
                  className="neo-button yellow login-submit"
                  type="submit"
                  disabled={busy}
                >
                  {busy ? 'Changing password…' : 'Change Password'}
                </button>
              </form>
            )}

            <p className="login-help">
              A successful change requires you to sign in again.
            </p>
          </>
        )}
      </section>
    </main>
  )
}