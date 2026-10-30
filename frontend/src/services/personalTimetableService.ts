export type PersonalTimetable = {
  status:
    | 'LINK_REQUIRED'
    | 'PROFILE_DISABLED'
    | 'NO_PUBLICATION'
    | 'NO_SESSIONS'
    | 'AVAILABLE'
  message: string
  publication: {
    id: number
    name: string
    publishedAt: string
  } | null
  sessions: {
    id: number
    subjectName: string
    roomCode: string
    teacherName: string
    sectionCode: string
    dayOfWeek: number
    startTime: string
    endTime: string
  }[]
}

export async function getPersonalTimetable(
  signal?: AbortSignal,
): Promise<PersonalTimetable> {
  const response = await fetch('/api/me/timetable', {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (response.status === 401) {
    throw new Error('Your session has expired. Please sign in again.')
  }

  if (response.status === 403) {
    throw new Error('Your account cannot access this timetable.')
  }

  if (!response.ok) {
    throw new Error('Unable to load your timetable. Please try again.')
  }

  return response.json()
}