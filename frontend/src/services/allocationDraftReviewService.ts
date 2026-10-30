export type DraftSummary = {
  id: number
  name: string
  version: number
  createdAt: string
  updatedAt: string
}

export type DraftPage = {
  content: DraftSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export type DraftDetail = {
  id: number
  name: string
  version: number
  createdAt: string
  requestedSessions: number
  allocatedSessions: number
  unallocatedSessions: number
  allSessionsAllocated: boolean
  requirements: {
    requirementId: number
    sessionsRequested: number
    periodsPerSession: number
    allocatedSessions: number
    unallocatedSessions: number
  }[]
  sessions: {
    id: number
    requirementId: number
    sessionNumber: number
    roomId: number
    teacherId: number
    sectionId: number
    dayOfWeek: number
    startTime: string
    endTime: string
  }[]
}

function responseError(status: number): Error {
  switch (status) {
    case 401:
      return new Error('Your session has expired. Please sign in again.')
    case 403:
      return new Error('You do not have permission to review drafts.')
    case 404:
      return new Error('The selected draft was not found.')
    default:
      return new Error('Unable to load saved drafts. Please try again.')
  }
}

export async function getAllocationDrafts(
  page: number,
  signal?: AbortSignal,
): Promise<DraftPage> {
  const query = new URLSearchParams({
    page: String(page),
    size: '10',
  })

  const response = await fetch(`/api/allocation-drafts?${query}`, {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}

export async function getAllocationDraft(
  id: number,
  signal?: AbortSignal,
): Promise<DraftDetail> {
  const response = await fetch(`/api/allocation-drafts/${id}`, {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}
export type DraftValidation = {
  draftId: number
  version: number
  valid: boolean
  errors: string[]
}

export async function getDraftValidation(
  draftId: number,
  signal?: AbortSignal,
): Promise<DraftValidation> {
  const response = await fetch(
    `/api/allocation-drafts/${draftId}/validation`,
    {
      credentials: 'same-origin',
      cache: 'no-store',
      signal,
    },
  )

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}