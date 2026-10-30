export type MySectionResponse = {
  section: {
    id: number
    code: string
    name: string
    programCode: string
    semester: number
    academicYearStart: number
    enabled: boolean
  } | null
}

export async function getMySection(
  signal?: AbortSignal,
): Promise<MySectionResponse> {
  const response = await fetch('/api/student/me/section', {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (response.status === 401) {
    throw new Error('Your session has expired. Please sign in again.')
  }

  if (response.status === 403) {
    throw new Error('Your account cannot access the Student workspace.')
  }

  if (!response.ok) {
    throw new Error('Unable to load your section. Please try again.')
  }

  return response.json()
}