export type MyTeacherProfileResponse = {
  teacher: {
    id: number
    employeeCode: string
    fullName: string
    department: string
    maxPeriodsPerDay: number
    maxPeriodsPerWeek: number
    enabled: boolean
  } | null
}

export async function getMyTeacherProfile(
  signal?: AbortSignal,
): Promise<MyTeacherProfileResponse> {
  const response = await fetch('/api/teacher/me/profile', {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (response.status === 401) {
    throw new Error('Your session has expired. Please sign in again.')
  }

  if (response.status === 403) {
    throw new Error('Your account cannot access the Teacher workspace.')
  }

  if (!response.ok) {
    throw new Error('Unable to load your teaching profile. Please try again.')
  }

  return response.json()
}