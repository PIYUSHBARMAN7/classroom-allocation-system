export type SlotType = 'TEACHING' | 'BREAK'

export const weekdays = [
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' },
  { value: 7, label: 'Sunday' },
]

export const slotTypeLabels: Record<SlotType, string> = {
  TEACHING: 'Teaching period',
  BREAK: 'Break',
}

export type TimeSlotInput = {
  label: string
  dayOfWeek: number
  startTime: string
  endTime: string
  slotType: SlotType
}

export type TimeSlot = TimeSlotInput & {
  id: number
  enabled: boolean
  version: number
  createdAt: string
  updatedAt: string
}

export type TimeSlotPage = {
  content: TimeSlot[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}