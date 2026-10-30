import type { RoomType } from './classroom'

export type SubjectInput = {
  code: string
  name: string
  department: string
  requiredRoomType: RoomType
}

export type Subject = SubjectInput & {
  id: number
  enabled: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type SubjectPage = {
  content: Subject[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}