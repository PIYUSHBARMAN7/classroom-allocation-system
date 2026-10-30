import { getCsrf } from './authService'

import type {
  ClassRequirement,
  RequirementInput,
  RequirementPage,
} from '../types/requirement'

export class RequirementApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'RequirementApiError'
    this.status = status
  }
}

async function request<T>(
  path: string,
  method: 'GET' | 'POST' | 'PUT' | 'PATCH',
  body?: unknown,
  signal?: AbortSignal,
): Promise<T> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
  }

  if (method !== 'GET') {
    const csrf = await getCsrf()

    headers[csrf.headerName] = csrf.token
    headers['Content-Type'] = 'application/json'
  }

  const response = await fetch(path, {
    method,
    credentials: 'same-origin',
    cache: 'no-store',
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
    signal,
  })

  if (!response.ok) {
    const messages: Record<number, string> = {
      400:
        'Check the requirement details. Active requirements need an active section, subject and teacher, and must fit the teacher’s individual workload limits.',
      401:
        'Your session has expired. Please sign in again.',
      403:
        'You do not have permission, or your session could not be verified.',
      404:
        'The requirement or a selected section, subject or teacher could not be found.',
      409:
        'A requirement may already exist for this section and subject, or the record has changed. Cancel editing and reload before retrying.',
    }

    throw new RequirementApiError(
      messages[response.status] ??
        `Requirement request failed (HTTP ${response.status}).`,
      response.status,
    )
  }

  return response.json() as Promise<T>
}

export function getRequirements(
  page: number,
  enabled?: boolean,
  signal?: AbortSignal,
): Promise<RequirementPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: '10',
  })

  if (enabled !== undefined) {
    params.set('enabled', String(enabled))
  }

  return request<RequirementPage>(
    `/api/requirements?${params}`,
    'GET',
    undefined,
    signal,
  )
}

export function getRequirement(
  id: number,
  signal?: AbortSignal,
): Promise<ClassRequirement> {
  return request<ClassRequirement>(
    `/api/requirements/${id}`,
    'GET',
    undefined,
    signal,
  )
}

export function createRequirement(
  input: RequirementInput,
): Promise<ClassRequirement> {
  return request<ClassRequirement>(
    '/api/requirements',
    'POST',
    input,
  )
}

export function updateRequirement(
  id: number,
  input: RequirementInput,
  version: number,
): Promise<ClassRequirement> {
  return request<ClassRequirement>(
    `/api/requirements/${id}`,
    'PUT',
    {
      ...input,
      version,
    },
  )
}

export function setRequirementStatus(
  id: number,
  enabled: boolean,
  version: number,
): Promise<ClassRequirement> {
  return request<ClassRequirement>(
    `/api/requirements/${id}/status`,
    'PATCH',
    { enabled, version },
  )
}