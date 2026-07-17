import { describe, it, expect } from 'vitest'
import { formatDateToYMD } from './invigilation'

describe('formatDateToYMD', () => {
  it('格式化日期为 YYYY-MM-DD', () => {
    expect(formatDateToYMD(new Date(2026, 0, 5))).toBe('2026-01-05')
    expect(formatDateToYMD(new Date(2026, 11, 25))).toBe('2026-12-25')
  })
})
