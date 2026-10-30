export type TeacherInput = {
  employeeCode: string
  fullName: string
  department: string
  maxPeriodsPerDay: number
  maxPeriodsPerWeek: number
}

export type Teacher = TeacherInput & {
  id: number
  userId: number | null
  enabled: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type TeacherPage = {
  content: Teacher[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export type TeacherAccountOption = {
  id: number
  displayName: string
  email: string
}

export type TeacherAccountPage = {
  content: TeacherAccountOption[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}