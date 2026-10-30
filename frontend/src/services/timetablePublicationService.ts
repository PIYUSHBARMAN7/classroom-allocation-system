import { getCsrf } from './authService'

export type TimetablePublication = {
  id: number
  sourceDraftId: number
  sourceDraftVersion: number
  name: string
  publishedAt: string
  sessionCount: number
}

export class PublicationError extends Error {
  readonly errors: string[]

  constructor(message: string, errors: string[] = []) {
    super(message)
    this.name = 'PublicationError'
    this.errors = errors
  }
}

async function responseError(response: Response): Promise<PublicationError> {
  if (response.status === 401) {
    return new PublicationError(
      'Your session has expired. Please sign in again.',
    )
  }

  if (response.status === 403) {
    return new PublicationError(
      'Access was denied or your session could not be verified.',
    )
  }

  if (response.status === 404) {
    return new PublicationError('The selected draft was not found.')
  }

  if (response.status === 409) {
    let errors: string[] = []

    try {
      const body: { errors?: unknown } = await response.json()

      if (Array.isArray(body.errors)) {
        errors = body.errors.filter(
          (value): value is string => typeof value === 'string',
        )
      }
    } catch {
      // Some conflict responses do not contain a JSON body.
    }

    return new PublicationError(
      'Publication was not completed. Refresh the checks and review the latest timetable.',
      errors,
    )
  }

  return new PublicationError(
    'The publication service is unavailable. Please try again.',
  )
}

export async function getActivePublication(
  signal?: AbortSignal,
): Promise<TimetablePublication | null> {
  const response = await fetch('/api/timetable-publications/active', {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (response.status === 204) {
    return null
  }

  if (!response.ok) {
    throw await responseError(response)
  }

  return response.json()
}

export async function publishTimetable(
  draftId: number,
  draftVersion: number,
  expectedActivePublicationId: number | null,
  signal?: AbortSignal,
): Promise<TimetablePublication> {
  const csrf = await getCsrf()

  const response = await fetch('/api/timetable-publications', {
    method: 'POST',
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify({
      draftId,
      draftVersion,
      expectedActivePublicationId,
    }),
  })

  if (!response.ok) {
    throw await responseError(response)
  }

  return response.json()
}