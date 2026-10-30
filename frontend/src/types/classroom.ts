export type RoomType =
  | 'LECTURE_HALL'
  | 'COMPUTER_LAB'
  | 'SCIENCE_LAB'
  | 'SEMINAR_ROOM'

export const roomTypeLabels: Record<RoomType, string> = {
  LECTURE_HALL: 'Lecture hall',
  COMPUTER_LAB: 'Computer lab',
  SCIENCE_LAB: 'Science lab',
  SEMINAR_ROOM: 'Seminar room',
}

export type ClassroomInput = {
  code: string
  name: string
  building: string
  floorNumber: number
  capacity: number
  roomType: RoomType
  hasProjector: boolean
  accessible: boolean
}

export type Classroom = ClassroomInput & {
  id: number
  enabled: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type ClassroomPage = {
  content: Classroom[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}