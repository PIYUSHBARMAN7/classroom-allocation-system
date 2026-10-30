import { getCsrf } from './authService'
import type { CurrentUser } from './authService'

export type CreateUserRequest = {
  displayName: string
  email: string
  password: string
  role: CurrentUser['role']
}

export type CreatedUser = CurrentUser & {
  enabled: boolean
}

export class UserRequestError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'UserRequestError'
    this.status = status
  }
}

export async function createUser(
  request: CreateUserRequest,
): Promise<CreatedUser> {
  const csrf = await getCsrf()

  const response = await fetch('/api/users', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify(request),
  })

  if (response.status === 401) {
    throw new UserRequestError('Please sign in again.', 401)
  }

  if (response.status === 403) {
    throw new UserRequestError(
      'The request was denied. Check your session and permissions.',
      403,
    )
  }

  if (response.status === 409) {
    throw new UserRequestError(
      'Account could not be created. Check whether that email already exists.',
      409,
    )
  }

  if (response.status === 400) {
    throw new UserRequestError(
      'Check the name, email, password and role, then try again.',
      400,
    )
  }

  if (response.status !== 201) {
    throw new UserRequestError(
      'Account creation is unavailable. Please try again later.',
      response.status,
    )
  }

  return response.json()
}