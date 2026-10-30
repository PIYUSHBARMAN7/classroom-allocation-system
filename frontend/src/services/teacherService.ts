import { getCsrf } from './authService'
import type {
  Teacher,
  TeacherInput,
  TeacherPage,
  TeacherAccountPage,
} from '../types/teacher'

export class TeacherApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'TeacherApiError'
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
      400: 'Check the teacher details. Linked accounts must be enabled and have the Teacher role.',
      401: 'Your session has expired. Please sign in again.',
      403: 'You do not have permission, or your session could not be verified.',
      404: 'The teacher profile or login account could not be found.',
      409: 'The employee code or account may already be assigned, or this profile has changed. Reload before trying again.',
    }

    throw new TeacherApiError(
      messages[response.status] ??
        `Teacher request failed (HTTP ${response.status}).`,
      response.status,
    )
  }

  return response.json() as Promise<T>
}

export function getTeachers(
  page: number,
  enabled?: boolean,
  signal?: AbortSignal,
): Promise<TeacherPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: '10',
  })

  if (enabled !== undefined) {
    params.set('enabled', String(enabled))
  }

  return request<TeacherPage>(
    `/api/teachers?${params}`,
    'GET',
    undefined,
    signal,
  )
}

export function getTeacher(
  id: number,
  signal?: AbortSignal,
): Promise<Teacher> {
  return request<Teacher>(
    `/api/teachers/${id}`,
    'GET',
    undefined,
    signal,
  )
}

export function createTeacher(
  input: TeacherInput,
): Promise<Teacher> {
  return request<Teacher>('/api/teachers', 'POST', input)
}

export function updateTeacher(
  id: number,
  input: TeacherInput,
  version: number,
): Promise<Teacher> {
  return request<Teacher>(`/api/teachers/${id}`, 'PUT', {
    ...input,
    version,
  })
}

export function setTeacherStatus(
  id: number,
  enabled: boolean,
  version: number,
): Promise<Teacher> {
  return request<Teacher>(
    `/api/teachers/${id}/status`,
    'PATCH',
    { enabled, version },
  )
}

export function linkTeacherAccount(
  teacherId: number,
  userId: number,
  version: number,
): Promise<Teacher> {
  return request<Teacher>(
    `/api/teachers/${teacherId}/account`,
    'PUT',
    { userId, version },
  )
}

export function getAvailableTeacherAccounts(
  page: number,
  signal?: AbortSignal,
): Promise<TeacherAccountPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: '20',
  })

  return request<TeacherAccountPage>(
    `/api/users/available-teacher-accounts?${params}`,
    'GET',
    undefined,
    signal,
  )
}