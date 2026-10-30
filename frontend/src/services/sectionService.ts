import { getCsrf } from './authService'
import type {
  Section,
  SectionInput,
  SectionPage,
} from '../types/section'

export class SectionApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'SectionApiError'
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
      400: 'Check the section details and try again.',
      401: 'Your session has expired. Please sign in again.',
      403: 'You do not have permission, or your session could not be verified.',
      404: 'This section could not be found. Reload the directory.',
      409: 'This section may already exist, or its details have changed. Cancel editing and reload before retrying.',
    }

    throw new SectionApiError(
      messages[response.status] ??
        `Section request failed (HTTP ${response.status}).`,
      response.status,
    )
  }

  return response.json() as Promise<T>
}

export function getSections(
  page: number,
  enabled?: boolean,
  signal?: AbortSignal,
): Promise<SectionPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: '10',
  })

  if (enabled !== undefined) {
    params.set('enabled', String(enabled))
  }

  return request<SectionPage>(
    `/api/sections?${params}`,
    'GET',
    undefined,
    signal,
  )
}

export function createSection(
  input: SectionInput,
): Promise<Section> {
  return request<Section>('/api/sections', 'POST', input)
}

export function updateSection(
  id: number,
  input: SectionInput,
  version: number,
): Promise<Section> {
  return request<Section>(`/api/sections/${id}`, 'PUT', {
    ...input,
    version,
  })
}

export function setSectionStatus(
  id: number,
  enabled: boolean,
  version: number,
): Promise<Section> {
  return request<Section>(
    `/api/sections/${id}/status`,
    'PATCH',
    { enabled, version },
  )
}