import { getCsrf } from './authService'

export type AllocationRequirementOption = {
  id: number
  enabled: boolean
  sessionsPerWeek: number
  periodsPerSession: number
  section: {
    id: number
    code: string
    name: string
  }
  subject: {
    id: number
    code: string
    name: string
  }
  teacher: {
    id: number
    fullName: string
  }
}

export type AllocationRequirementPage = {
  content: AllocationRequirementOption[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export type ScheduledSession = {
  requirementId: number
  roomId: number
  teacherId: number
  sectionId: number
  dayOfWeek: number
  startTime: string
  endTime: string
}

export type UnallocatedSession = {
  requirementId: number
  sessionNumber: number
  reason: string
}

export type AllocationPreview = {
  allocations: ScheduledSession[]
  unallocated: UnallocatedSession[]
}

function responseError(status: number): Error {
  switch (status) {
    case 400:
      return new Error(
        'The preview request was rejected. Check that selected requirements and their linked records are enabled, with at most 25 requirements and 50 weekly sessions. This preview also supports at most 100 enabled rooms and 100 enabled time slots.',
      )
    case 401:
      return new Error('Your session has expired. Please sign in again.')
    case 403:
      return new Error(
        'Access was denied or your session could not be verified. Please sign in again.',
      )
    case 429:
      return new Error('Too many requests. Please wait before trying again.')
    default:
      return new Error('The allocation service is unavailable. Please try again.')
  }
}

export async function getAllocationRequirements(
  page: number,
  signal?: AbortSignal,
): Promise<AllocationRequirementPage> {
  const query = new URLSearchParams({
    page: String(page),
    size: '10',
    enabled: 'true',
  })

  const response = await fetch(`/api/requirements?${query}`, {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}

export async function generateAllocationPreview(
  requirementIds: number[],
  signal?: AbortSignal,
): Promise<AllocationPreview> {
  const csrf = await getCsrf()

  const response = await fetch('/api/allocations/preview', {
    method: 'POST',
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify({ requirementIds }),
  })

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}

export type AllocationDraftCreated = {
  id: number
  name: string
  version: number
  createdAt: string
  result: AllocationPreview
}

export async function createAllocationDraft(
  name: string,
  requirementIds: number[],
  signal?: AbortSignal,
): Promise<AllocationDraftCreated> {
  const csrf = await getCsrf()

  const response = await fetch('/api/allocation-drafts', {
    method: 'POST',
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
    headers: {
      'Content-Type': 'application/json',
      [csrf.headerName]: csrf.token,
    },
    body: JSON.stringify({
      name: name.trim(),
      requirementIds,
    }),
  })

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}