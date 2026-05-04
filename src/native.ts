import { NitroModules } from 'react-native-nitro-modules'
import type { InAppUpdates as InAppUpdatesSpec } from './specs/InAppUpdates.nitro'

export const InAppUpdates =
  NitroModules.createHybridObject<InAppUpdatesSpec>('InAppUpdates')
