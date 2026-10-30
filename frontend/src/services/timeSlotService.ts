import { getCsrf } from './authService'
import type {
  TimeSlot,
  TimeSlotInput,
  TimeSlotPage,
} from '../types/timeSlot'

export class TimeSlotApiError extends Error {
  status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'TimeSlotApiError'
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
      400: 'Check the slot details. End time must be after start time, and times must use whole minutes.',
      401: 'Your session has expired. Please sign in again.',
      403: 'You do not have permission, or your session could not be verified.',
      404: 'This time slot could not be found. Reload the directory.',
      409: 'This slot may duplicate or overlap another period, or it has changed since you loaded it. Cancel editing and reload before retrying.',
    }

    throw new TimeSlotApiError(
      messages[response.status] ??
        `Time slot request failed (HTTP ${response.status}).`,
      response.status,
    )
  }

  return response.json() as Promise<T>
}

export function getTimeSlots(
  page: number,
  enabled?: boolean,
  signal?: AbortSignal,
): Promise<TimeSlotPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: '10',
  })

  if (enabled !== undefined) {
    params.set('enabled', String(enabled))
  }

  return request<TimeSlotPage>(
    `/api/time-slots?${params}`,
    'GET',
    undefined,
    signal,
  )
}

export function createTimeSlot(
  input: TimeSlotInput,
): Promise<TimeSlot> {
  return request<TimeSlot>('/api/time-slots', 'POST', input)
}

export function updateTimeSlot(
  id: number,
  input: TimeSlotInput,
  version: number,
): Promise<TimeSlot> {
  return request<TimeSlot>(`/api/time-slots/${id}`, 'PUT', {
    ...input,
    version,
  })
}

export function setTimeSlotStatus(
  id: number,
  enabled: boolean,
  version: number,
): Promise<TimeSlot> {
  return request<TimeSlot>(
    `/api/time-slots/${id}/status`,
    'PATCH',
    { enabled, version },
  )
}