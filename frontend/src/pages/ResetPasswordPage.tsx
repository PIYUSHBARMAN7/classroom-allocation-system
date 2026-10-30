import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { resetPassword } from '../services/recoveryService'
import '../styles/login.css'

export default function ResetPasswordPage() {
  const location = useLocation()
  const navigate = useNavigate()

  const [token, setToken] = useState(() => {
    const params = new URLSearchParams(location.hash.slice(1))
    return params.get('token') ?? ''
  })

  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const hasValidFormat = /^[A-Za-z0-9_-]{43}$/.test(token)

  useEffect(() => {
    if (location.hash) {
      // Keep the token in component memory; remove it from the URL.
      void navigate(
        { pathname: location.pathname, search: '', hash: '' },
        { replace: true },
      )
    }
  }, [location.hash, location.pathname, navigate])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy || !hasValidFormat) return

    const form = event.currentTarget
    const data = new FormData(form)

    const password = String(data.get('newPassword') ?? '')
    const confirmation = String(data.get('confirmation') ?? '')

    setError('')

    if (password !== confirmation) {
      setError('The passwords do not match.')
      return
    }

    if (
      password.trim().length === 0 ||
      Array.from(password).length < 15 ||
      new TextEncoder().encode(password).length > 72
    ) {
      setError(
        'Use at least 15 characters and no more than 72 UTF-8 bytes.',
      )
      return
    }

    setBusy(true)

    try {
      await resetPassword(token, password)
      form.reset()
      setToken('')
      setSuccess(true)
    } catch (err) {
      setError(
        err instanceof TypeError
          ? 'Could not confirm the result because the connection failed. Try signing in with your new password before requesting another link.'
          : err instanceof Error
            ? err.message
            : 'Unable to reset your password. Please try again.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="login-page">
      <Link className="back-link" to="/login">
        ← Back to sign in
      </Link>

      <section className="neo-panel login-card" aria-busy={busy}>
        <span className="sample-badge">ACCOUNT RECOVERY</span>
        <h1>Choose a new password.</h1>

        {success ? (
          <div className="recovery-success" role="status">
            <strong>Password changed.</strong>
            <p>Sign in using your new password.</p>
            <Link className="neo-button yellow" to="/login">
              Go to sign in
            </Link>
          </div>
        ) : !hasValidFormat ? (
          <div>
            <p className="login-error" role="alert">
              This page needs a valid reset link. Open the link from
              your email.
            </p>
            <Link to="/forgot-password">Request a new link</Link>
          </div>
        ) : (
          <>
            <p id="password-requirements">
              Use at least 15 characters. Your password must differ
              from your current one and use no more than 72 UTF-8 bytes.
            </p>

            {error && (
              <p className="login-error" role="alert">
                {error}
              </p>
            )}

            <form onSubmit={handleSubmit}>
              <div className="field">
                <label htmlFor="reset-password">New password</label>
                <input
                  id="reset-password"
                  name="newPassword"
                  type="password"
                  autoComplete="new-password"
                  aria-describedby="password-requirements"
                  required
                  disabled={busy}
                />
              </div>

              <div className="field">
                <label htmlFor="reset-confirmation">
                  Confirm new password
                </label>
                <input
                  id="reset-confirmation"
                  name="confirmation"
                  type="password"
                  autoComplete="new-password"
                  required
                  disabled={busy}
                />
              </div>

              <button
                className="neo-button yellow login-submit"
                type="submit"
                disabled={busy}
              >
                {busy ? 'Saving…' : 'Reset password'}
              </button>
            </form>

            <p className="login-help">
              <Link to="/forgot-password">Request a new reset link</Link>
            </p>
          </>
        )}
      </section>
    </main>
  )
}