import { getCsrf } from './authService'

export type SectionOption = {
  id: number
  code: string
  name: string
  programCode: string
  semester: number
  academicYearStart: number
  enabled: boolean
}

export type StudentSection = {
  userId: number
  displayName: string
  email: string
  section: SectionOption | null
  version: number | null
}

export type SectionOptionPage = {
  content: SectionOption[]
  totalPages: number
  last: boolean
}

export class StudentSectionError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'StudentSectionError'
    this.status = status
  }
}

function responseError(status: number): StudentSectionError {
  const messages: Record<number, string> = {
    400: 'Choose an enabled Student account and an enabled section.',
    401: 'Your session has expired. Please sign in again.',
    403: 'Access was denied or your session could not be verified.',
    404: 'The Student account or section was not found.',
    409: 'The assignment changed. Look up the Student again before saving.',
    429: 'Too many requests. Please wait before trying again.',
  }

  return new StudentSectionError(
    status,
    messages[status] ?? 'The service is unavailable. Please try again.',
  )
}

export async function lookupStudentSection(
  email: string,
  signal?: AbortSignal,
): Promise<StudentSection> {
  const query = new URLSearchParams({
    email: email.trim(),
  })

  const response = await fetch(
    `/api/student-section-assignments/lookup?${query}`,
    {
      credentials: 'same-origin',
      cache: 'no-store',
      signal,
    },
  )

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}

export async function getAssignmentSections(
  page: number,
  signal?: AbortSignal,
): Promise<SectionOptionPage> {
  const query = new URLSearchParams({
    page: String(page),
    size: '20',
    enabled: 'true',
  })

  const response = await fetch(`/api/sections?${query}`, {
    credentials: 'same-origin',
    cache: 'no-store',
    signal,
  })

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}

export async function assignStudentSection(
  userId: number,
  sectionId: number,
  version: number | null,
  signal?: AbortSignal,
): Promise<StudentSection> {
  const csrf = await getCsrf()

  const response = await fetch(
    `/api/student-section-assignments/${userId}`,
    {
      method: 'PUT',
      credentials: 'same-origin',
      cache: 'no-store',
      signal,
      headers: {
        'Content-Type': 'application/json',
        [csrf.headerName]: csrf.token,
      },
      body: JSON.stringify({
        sectionId,
        version,
      }),
    },
  )

  if (!response.ok) {
    throw responseError(response.status)
  }

  return response.json()
}