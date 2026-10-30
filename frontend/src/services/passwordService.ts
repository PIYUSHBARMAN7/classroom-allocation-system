import { getCsrf, LoginRateLimitError } from './authService'

export class PasswordChangeError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'PasswordChangeError'
    this.status = status
  }
}

export async function changePassword(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  const csrf = await getCsrf()

  const response = await fetch('/api/auth/change-password', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify({
      currentPassword,
      newPassword,
    }),
  })

  if (response.status === 429) {
    const value = Number(response.headers.get('Retry-After'))
    const seconds =
      Number.isFinite(value) && value > 0 ? Math.ceil(value) : 60

    throw new LoginRateLimitError(seconds)
  }

  if (response.status === 401) {
    throw new PasswordChangeError(
      'Your session has expired. Please sign in again.',
      401,
    )
  }

  if (response.status === 400) {
    throw new PasswordChangeError(
      'Check your current password. The new password must meet the requirements and be different.',
      400,
    )
  }

  if (response.status === 403) {
    throw new PasswordChangeError(
      'Your request could not be verified. Refresh the page and try again.',
      403,
    )
  }

  if (response.status !== 204) {
    throw new PasswordChangeError(
      `Password change could not be confirmed (HTTP ${response.status}).`,
      response.status,
    )
  }
}