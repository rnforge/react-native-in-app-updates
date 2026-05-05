import { InAppUpdatesError } from '../types'

describe('InAppUpdatesError', () => {
  it('sets message, code, and name via constructor', () => {
    const error = new InAppUpdatesError('Something went wrong', 'native-error')

    expect(error.message).toBe('Something went wrong')
    expect(error.code).toBe('native-error')
    expect(error.name).toBe('InAppUpdatesError')
  })

  it('is an instance of InAppUpdatesError', () => {
    const error = new InAppUpdatesError('Test', 'invalid-input')

    expect(error).toBeInstanceOf(InAppUpdatesError)
  })

  it('is an instance of Error', () => {
    const error = new InAppUpdatesError('Test', 'bridge-error')

    expect(error).toBeInstanceOf(Error)
  })

  it('preserves stack trace', () => {
    const error = new InAppUpdatesError('Stack test', 'unexpected')

    expect(error.stack).toBeDefined()
    expect(error.stack).toContain('InAppUpdatesError')
  })

  it('accepts all defined error codes', () => {
    const codes: InAppUpdatesError['code'][] = [
      'invalid-input',
      'bridge-error',
      'native-error',
      'unexpected',
    ]

    codes.forEach((code) => {
      const error = new InAppUpdatesError(`Test ${code}`, code)
      expect(error.code).toBe(code)
    })
  })
})
