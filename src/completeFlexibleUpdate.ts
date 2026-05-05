import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import type { UpdateStatus } from './types'

export async function completeFlexibleUpdate(): Promise<UpdateStatus> {
  const result = await InAppUpdates.completeFlexibleUpdate()
  return mapNativeStatus(result)
}
