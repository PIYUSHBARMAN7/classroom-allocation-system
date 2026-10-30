import { getCsrf } from './authService'
import type {
  Subject,
  SubjectInput,
  SubjectPage,
} from '../types/subject'

export class SubjectApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'SubjectApiError'
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
      400: 'Check the subject details and try again.',
      401: 'Your session has expired. Please sign in again.',
      403: 'You do not have permission, or your session could not be verified.',
      404: 'This subject could not be found. Reload the directory.',
      409: 'The subject code may already exist, or this subject has changed. Cancel editing and reload before retrying.',
    }

    throw new SubjectApiError(
      messages[response.status] ??
        `Subject request failed (HTTP ${response.status}).`,
      response.status,
    )
  }

  return response.json() as Promise<T>
}

export function getSubjects(
  page: number,
  enabled?: boolean,
  signal?: AbortSignal,
): Promise<SubjectPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: '10',
  })

  if (enabled !== undefined) {
    params.set('enabled', String(enabled))
  }

  return request<SubjectPage>(
    `/api/subjects?${params}`,
    'GET',
    undefined,
    signal,
  )
}

export function createSubject(
  input: SubjectInput,
): Promise<Subject> {
  return request<Subject>('/api/subjects', 'POST', input)
}

export function updateSubject(
  id: number,
  input: SubjectInput,
  version: number,
): Promise<Subject> {
  return request<Subject>(`/api/subjects/${id}`, 'PUT', {
    ...input,
    version,
  })
}

export function setSubjectStatus(
  id: number,
  enabled: boolean,
  version: number,
): Promise<Subject> {
  return request<Subject>(
    `/api/subjects/${id}/status`,
    'PATCH',
    { enabled, version },
  )
}