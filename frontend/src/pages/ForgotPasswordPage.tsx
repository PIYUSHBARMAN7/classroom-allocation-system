import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router'
import { requestPasswordReset } from '../services/recoveryService'
import '../styles/login.css'

export default function ForgotPasswordPage() {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy) return

    const form = event.currentTarget
    const data = new FormData(form)
    const email = String(data.get('email') ?? '')

    setBusy(true)
    setError('')

    try {
      await requestPasswordReset(email)
      form.reset()
      setSubmitted(true)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Unable to request a reset link. Please try again.',
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
        <h1>Forgot your password?</h1>
        <p>Enter the email address registered with your institution.</p>

        {error && (
          <p className="login-error" role="alert">
            {error}
          </p>
        )}

        {submitted ? (
          <div className="recovery-success" role="status">
            <strong>Check your inbox.</strong>
            <p>
              If an eligible account exists for this email, a password
              reset link will be sent. Check your spam folder too.
            </p>
            <p>Use the most recent reset email.</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="recovery-email">Registered email</label>
              <input
                id="recovery-email"
                name="email"
                type="email"
                autoComplete="email"
                autoCapitalize="none"
                spellCheck={false}
                maxLength={254}
                placeholder="name@college.edu"
                required
                disabled={busy}
              />
            </div>

            <button
              className="neo-button yellow login-submit"
              type="submit"
              disabled={busy}
            >
              {busy ? 'Requesting…' : 'Send reset link'}
            </button>
          </form>
        )}

        <p className="login-help">
          <Link to="/login">Return to sign in</Link>
        </p>
      </section>
    </main>
  )
}