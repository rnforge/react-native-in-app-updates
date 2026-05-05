import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import type { UpdateStatus } from './types'

export async function startImmediateUpdate(): Promise<UpdateStatus> {
  const result = await InAppUpdates.startImmediateUpdate()
  return mapNativeStatus(result)
}
