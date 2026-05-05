import { InAppUpdates } from './native'
import { mapNativeStatus } from './internal/mapNativeStatus'
import type { UpdateStatus } from './types'

export async function startFlexibleUpdate(): Promise<UpdateStatus> {
  const result = await InAppUpdates.startFlexibleUpdate()
  return mapNativeStatus(result)
}
