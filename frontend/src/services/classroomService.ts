import { getCsrf } from './authService'
import type {
  Classroom,
  ClassroomInput,
  ClassroomPage,
} from '../types/classroom'

export class ClassroomApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ClassroomApiError'
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
      400: 'Check the classroom details and try again.',
      401: 'Your session has expired. Please sign in again.',
      403: 'You do not have permission, or your session could not be verified.',
      404: 'This classroom could not be found. Reload the list.',
      409: 'The room code may already exist, or this classroom has changed. Cancel editing and reload before trying again.',
    }

    throw new ClassroomApiError(
      messages[response.status] ??
        `Classroom request failed (HTTP ${response.status}).`,
      response.status,
    )
  }

  return response.json() as Promise<T>
}

export function getClassrooms(
  page: number,
  enabled?: boolean,
  signal?: AbortSignal,
): Promise<ClassroomPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: '10',
  })

  if (enabled !== undefined) {
    params.set('enabled', String(enabled))
  }

  return request<ClassroomPage>(
    `/api/classrooms?${params}`,
    'GET',
    undefined,
    signal,
  )
}

export function createClassroom(
  input: ClassroomInput,
): Promise<Classroom> {
  return request<Classroom>('/api/classrooms', 'POST', input)
}

export function updateClassroom(
  id: number,
  input: ClassroomInput,
  version: number,
): Promise<Classroom> {
  return request<Classroom>(`/api/classrooms/${id}`, 'PUT', {
    ...input,
    version,
  })
}

export function setClassroomStatus(
  id: number,
  enabled: boolean,
  version: number,
): Promise<Classroom> {
  return request<Classroom>(
    `/api/classrooms/${id}/status`,
    'PATCH',
    { enabled, version },
  )
}