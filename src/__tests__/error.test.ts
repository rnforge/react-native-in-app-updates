import { InAppUpdatesError } from '../types'
import { normalizeNativeError } from '../internal/normalizeNativeError'

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

  it('can carry android diagnostics', () => {
    const error = new InAppUpdatesError('Test', 'native-error', {
      packageName: 'com.example.app',
      playCore: {
        taskErrorCode: -1,
      },
    })

    expect(error.android?.playCore?.taskErrorCode).toBe(-1)
  })

  it('normalizes existing InAppUpdatesError without wrapping', () => {
    const error = new InAppUpdatesError('Already normalized', 'bridge-error')

    expect(normalizeNativeError(error)).toBe(error)
  })

  it('normalizes primitive native failures', () => {
    const error = normalizeNativeError('Primitive failure')

    expect(error).toMatchObject({
      name: 'InAppUpdatesError',
      code: 'native-error',
      message: 'Primitive failure',
    })
  })

  it('normalizes empty native failures to default message', () => {
    const error = normalizeNativeError('')

    expect(error).toMatchObject({
      name: 'InAppUpdatesError',
      code: 'native-error',
      message: 'Native failure',
    })
  })

  it('uses default invalid-input message when native payload has no message', () => {
    const error = normalizeNativeError(new Error('INVALID_INPUT|code=bad'))

    expect(error).toMatchObject({
      name: 'InAppUpdatesError',
      code: 'invalid-input',
      message: 'Invalid input',
    })
  })

  it('uses default Play Core message when native payload has no message', () => {
    const error = normalizeNativeError(new Error('PLAY_CORE_TASK_FAILURE|taskErrorCode=7'))

    expect(error).toMatchObject({
      name: 'InAppUpdatesError',
      code: 'native-error',
      message: 'Play Core task failed',
      android: {
        playCore: {
          taskErrorCode: 7,
        },
      },
    })
  })

  it('ignores non-numeric Play Core task error code', () => {
    const error = normalizeNativeError(
      new Error('PLAY_CORE_TASK_FAILURE|message=Failed|taskErrorCode=abc')
    )

    expect(error.android?.playCore?.taskErrorCode).toBeUndefined()
  })
})
