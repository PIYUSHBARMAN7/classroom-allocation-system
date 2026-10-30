export type SectionInput = {
  code: string
  name: string
  programCode: string
  department: string
  semester: number
  academicYearStart: number
  studentCount: number
}

export type Section = SectionInput & {
  id: number
  enabled: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type SectionPage = {
  content: Section[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}