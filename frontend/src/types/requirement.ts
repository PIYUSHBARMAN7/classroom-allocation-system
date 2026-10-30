import type { RoomType } from './classroom'

export type RequirementInput = {
  sectionId: number
  subjectId: number
  teacherId: number
  sessionsPerWeek: number
  periodsPerSession: number
  requiresProjector: boolean
  requiresAccessibleRoom: boolean
}

export type RequirementSection = {
  id: number
  code: string
  name: string
  programCode: string
  semester: number
  academicYearStart: number
  studentCount: number
  enabled: boolean
}

export type RequirementSubject = {
  id: number
  code: string
  name: string
  requiredRoomType: RoomType
  enabled: boolean
}

export type RequirementTeacher = {
  id: number
  employeeCode: string
  fullName: string
  enabled: boolean
}

export type ClassRequirement = {
  id: number
  section: RequirementSection
  subject: RequirementSubject
  teacher: RequirementTeacher
  sessionsPerWeek: number
  periodsPerSession: number
  weeklyPeriods: number
  requiresProjector: boolean
  requiresAccessibleRoom: boolean
  enabled: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type RequirementPage = {
  content: ClassRequirement[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}