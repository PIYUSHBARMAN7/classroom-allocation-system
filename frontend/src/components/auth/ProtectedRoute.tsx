import { useEffect, useState } from 'react'
import { Link, Navigate, Outlet } from 'react-router'
import { getCurrentUser } from '../../services/authService'
import type { CurrentUser } from '../../services/authService'

type ProtectedRouteProps = {
  allowedRoles?: readonly CurrentUser['role'][]
}

export default function ProtectedRoute({
  allowedRoles,
}: ProtectedRouteProps) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    async function checkAccess() {
      try {
        const currentUser = await getCurrentUser(controller.signal)

        if (!controller.signal.aborted) {
          setUser(currentUser)
        }
      } catch {
        if (!controller.signal.aborted) {
          setError(true)
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    void checkAccess()

    return () => controller.abort()
  }, [])

  if (loading) {
    return (
      <main className="login-page">
        <p role="status">Checking your session…</p>
      </main>
    )
  }

  if (error) {
    return (
      <main className="login-page">
        <section className="neo-panel login-card">
          <h1>Unable to verify your session.</h1>
          <p role="alert">
            Check that the backend is running, then try again.
          </p>
          <button
            className="neo-button yellow"
            type="button"
            onClick={() => window.location.reload()}
          >
            Try Again
          </button>
        </section>
      </main>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  const knownRoles: readonly CurrentUser['role'][] = [
    'ADMIN',
    'COORDINATOR',
    'TEACHER',
    'STUDENT',
  ]

  if (
    !knownRoles.includes(user.role) ||
    (allowedRoles && !allowedRoles.includes(user.role))
  ) {
    return (
      <main className="login-page">
        <section className="neo-panel login-card">
          <h1>Access restricted.</h1>
          <p>Your account does not have permission to view this page.</p>
          <Link to="/login">Return to your account</Link>
        </section>
      </main>
    )
  }

  return <Outlet context={user} />
}