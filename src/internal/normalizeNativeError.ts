import { InAppUpdatesError } from '../types'

const PLAY_CORE_TASK_FAILURE_PREFIX = 'PLAY_CORE_TASK_FAILURE|'
const INVALID_INPUT_PREFIX = 'INVALID_INPUT|'

export function normalizeNativeError(error: unknown): InAppUpdatesError {
  if (error instanceof InAppUpdatesError) {
    return error
  }

  const message = error instanceof Error ? error.message : String(error)

  const invalidInput = parseInvalidInput(message)
  if (invalidInput != null) {
    return new InAppUpdatesError(invalidInput.message, 'invalid-input')
  }

  const parsed = parsePlayCoreTaskFailure(message)
  if (parsed != null) {
    return new InAppUpdatesError(parsed.message, 'native-error', {
      playCore: {
        taskErrorCode: parsed.taskErrorCode,
      },
    })
  }

  return new InAppUpdatesError(message || 'Native failure', 'native-error')
}

function parsePlayCoreTaskFailure(message: string):
  | { message: string; taskErrorCode?: number }
  | null {
  if (!message.startsWith(PLAY_CORE_TASK_FAILURE_PREFIX)) {
    return null
  }

  const fields = message.slice(PLAY_CORE_TASK_FAILURE_PREFIX.length).split('|')
  const result: { message: string; taskErrorCode?: number } = {
    message: 'Play Core task failed',
  }

  for (const field of fields) {
    const [key, ...rest] = field.split('=')
    const value = rest.join('=')

    if (key === 'message' && value) {
      result.message = decodeURIComponent(value)
    } else if (key === 'taskErrorCode' && value) {
      const parsed = Number.parseInt(value, 10)
      if (!Number.isNaN(parsed)) {
        result.taskErrorCode = parsed
      }
    }
  }

  return result
}

function parseInvalidInput(message: string): { message: string } | null {
  if (!message.startsWith(INVALID_INPUT_PREFIX)) {
    return null
  }

  const fields = message.slice(INVALID_INPUT_PREFIX.length).split('|')
  const result: { message: string } = {
    message: 'Invalid input',
  }

  for (const field of fields) {
    const [key, ...rest] = field.split('=')
    const value = rest.join('=')

    if (key === 'message' && value) {
      result.message = decodeURIComponent(value)
    }
  }

  return result
}
