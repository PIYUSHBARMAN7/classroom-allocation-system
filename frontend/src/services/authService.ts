export type CurrentUser = {
  id: number
  displayName: string
  email: string
  role: 'ADMIN' | 'COORDINATOR' | 'TEACHER' | 'STUDENT'
}

type CsrfResponse = {
  headerName: string
  token: string
}

export async function getCsrf(): Promise<CsrfResponse> {
  const response = await fetch('/api/auth/csrf', {
    credentials: 'same-origin',
    cache: 'no-store',
  })

  if (!response.ok) {
    throw new Error('Unable to prepare your session. Please try again.')
  }

  return response.json()
}

export async function getCurrentUser(
  signal?: AbortSignal,
): Promise<CurrentUser | null> {
  const response = await fetch('/api/auth/me', {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (response.status === 401) {
    return null
  }

  if (!response.ok) {
    throw new Error('Unable to load your account. Please try again.')
  }

  return response.json()
}

export class LoginRateLimitError extends Error {
  retryAfterSeconds: number

  constructor(seconds: number) {
    super('Too many sign-in attempts.')
    this.name = 'LoginRateLimitError'
    this.retryAfterSeconds = seconds
  }
}

export async function signIn(
  email: string,
  password: string,
): Promise<CurrentUser> {
  const csrf = await getCsrf()

  const response = await fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      [csrf.headerName]: csrf.token,
    },
    body: new URLSearchParams({
      email: email.trim(),
      password,
    }),
  })

  // Handle rate limiting before the generic error.
  if (response.status === 429) {
  const value = Number(response.headers.get('Retry-After'))

  const seconds =
    Number.isFinite(value) && value > 0
      ? Math.ceil(value)
      : 60

  throw new LoginRateLimitError(seconds)
}

  if (response.status === 401) {
    throw new Error('Unable to sign in. Check your email and password.')
  }

  if (response.status === 403) {
    throw new Error('Your session could not be verified. Please try again.')
  }

  if (response.status !== 204) {
    throw new Error(
      `Sign-in is unavailable (HTTP ${response.status}). Please try again later.`,
    )
  }

  await getCsrf()

  const user = await getCurrentUser()

  if (!user) {
    throw new Error('Your session was not saved. Please sign in again.')
  }

  return user
}

export async function signOut(): Promise<void> {
  const csrf = await getCsrf()

  const response = await fetch('/api/auth/logout', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      [csrf.headerName]: csrf.token,
    },
  })

  if (response.status !== 204) {
    throw new Error('Unable to sign out. Please try again.')
  }
}