import { getCsrf } from './authService'

async function recoveryRequest(
  path: string,
  body: Record<string, string>,
): Promise<void> {
  const csrf = await getCsrf()

  const response = await fetch(path, {
    method: 'POST',
    credentials: 'same-origin',
    cache: 'no-store',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(body),
  })

  if (response.status === 429) {
    const value = Number(response.headers.get('Retry-After'))
    const seconds =
      Number.isFinite(value) && value > 0 ? Math.ceil(value) : 60

    throw new Error(
      `Too many requests. Try again in ${seconds} seconds.`,
    )
  }

  if (response.status === 403) {
    throw new Error(
      'Your session could not be verified. Please try again.',
    )
  }

  if (response.status === 400) {
    throw new Error(
      path.endsWith('/reset-password')
        ? 'The link may be invalid, expired or already used. Your new password must also meet the requirements and differ from your current password.'
        : 'Enter a valid email address.',
    )
  }

  const expectedStatus = path.endsWith('/forgot-password') ? 202 : 204

  if (response.status !== expectedStatus) {
  throw new Error(
    `Password recovery failed (HTTP ${response.status}). Please try again.`,
  )
}
}

export function requestPasswordReset(email: string): Promise<void> {
  return recoveryRequest('/api/auth/forgot-password', {
    email: email.trim(),
  })
}

export function resetPassword(
  token: string,
  newPassword: string,
): Promise<void> {
  return recoveryRequest('/api/auth/reset-password', {
    token,
    newPassword,
  })
}