/**
 * Minimal example source scaffold for @rnforge/react-native-in-app-updates
 *
 * IMPORTANT: This is NOT a fully generated React Native app. It does not
 * include native Android or iOS project files (android/, ios/, Pods, Gradle,
 * etc.). Use this file as a reference for how to integrate the v1 API into
 * your own React Native application.
 *
 * Prerequisites:
 * - A working React Native app with react-native-nitro-modules installed
 * - For Android: Play Core and Play Services available (Google Play builds only)
 * - For iOS: UIApplication.shared.open() for store page navigation
 */

import { useCallback, useEffect, useRef, useState } from 'react'
import * as InAppUpdates from '@rnforge/react-native-in-app-updates'

function hasCode(value: unknown): value is { code: string } {
  return (
    typeof value === 'object' &&
    value !== null &&
    'code' in value &&
    typeof (value as Record<string, unknown>).code === 'string'
  )
}

function hasNameAndMessage(value: unknown): value is { name: string; message: string } {
  return (
    typeof value === 'object' &&
    value !== null &&
    'name' in value &&
    'message' in value &&
    typeof (value as Record<string, unknown>).name === 'string' &&
    typeof (value as Record<string, unknown>).message === 'string'
  )
}

function serialize(value: unknown): string {
  if (hasNameAndMessage(value)) {
    const base = `${value.name}: ${value.message}`
    if (hasCode(value)) {
      return `${base} (code: ${value.code})`
    }
    return base
  }
  if (typeof value === 'object' && value !== null) {
    try {
      return JSON.stringify(value, null, 2)
    } catch {
      return '[object with circular reference]'
    }
  }
  return String(value)
}

export function useInAppUpdatesExample() {
  const [log, setLog] = useState<string[]>(['Tap a button below to call an API.'])
  const [appStoreId, setAppStoreId] = useState('')
  const [listenerActive, setListenerActive] = useState(false)
  const subscriptionRef = useRef<InAppUpdates.InstallStateSubscription | null>(null)
  const mountedRef = useRef(true)

  const appendLog = useCallback((label: string, payload: unknown) => {
    if (!mountedRef.current) return
    const serialized = serialize(payload)
    const line = `[${new Date().toLocaleTimeString()}] ${label}: ${serialized}`
    setLog((prev) => [...prev.slice(-20), line])
  }, [])

  const handleGetUpdateStatus = useCallback(async () => {
    try {
      const options = appStoreId
        ? { ios: { appStoreId } }
        : undefined
      const result = await InAppUpdates.getUpdateStatus(options)
      appendLog('getUpdateStatus', result)
    } catch (err) {
      appendLog('getUpdateStatus ERROR', err)
    }
  }, [appStoreId, appendLog])

  const handleStartImmediateUpdate = useCallback(async () => {
    try {
      const result = await InAppUpdates.startImmediateUpdate()
      appendLog('startImmediateUpdate', result)
    } catch (err) {
      appendLog('startImmediateUpdate ERROR', err)
    }
  }, [appendLog])

  const handleStartFlexibleUpdate = useCallback(async () => {
    try {
      const result = await InAppUpdates.startFlexibleUpdate()
      appendLog('startFlexibleUpdate', result)
    } catch (err) {
      appendLog('startFlexibleUpdate ERROR', err)
    }
  }, [appendLog])

  const handleCompleteFlexibleUpdate = useCallback(async () => {
    try {
      const result = await InAppUpdates.completeFlexibleUpdate()
      appendLog('completeFlexibleUpdate', result)
    } catch (err) {
      appendLog('completeFlexibleUpdate ERROR', err)
    }
  }, [appendLog])

  const handleOpenStorePage = useCallback(async () => {
    try {
      const options = appStoreId
        ? { ios: { appStoreId } }
        : undefined
      await InAppUpdates.openStorePage(options)
      appendLog('openStorePage', 'resolved')
    } catch (err) {
      appendLog('openStorePage ERROR', err)
    }
  }, [appStoreId, appendLog])

  const handleStartListener = useCallback(() => {
    if (subscriptionRef.current) {
      appendLog('listener', 'Already active — remove first')
      return
    }

    subscriptionRef.current = InAppUpdates.addInstallStateListener(
      (event: InAppUpdates.InstallStateEvent) => {
        appendLog('installStateListener', event)
      }
    )

    setListenerActive(true)
    appendLog('addInstallStateListener', 'started')
  }, [appendLog])

  const handleRemoveListener = useCallback(() => {
    if (subscriptionRef.current) {
      subscriptionRef.current.remove()
      subscriptionRef.current = null
      setListenerActive(false)
      appendLog('removeInstallStateListener', 'removed')
    } else {
      appendLog('removeInstallStateListener', 'none active')
    }
  }, [appendLog])

  useEffect(() => {
    mountedRef.current = true
    return () => {
      mountedRef.current = false
      if (subscriptionRef.current) {
        subscriptionRef.current.remove()
        subscriptionRef.current = null
      }
    }
  }, [])

  return {
    appStoreId,
    setAppStoreId,
    log,
    listenerActive,
    handleGetUpdateStatus,
    handleStartImmediateUpdate,
    handleStartFlexibleUpdate,
    handleCompleteFlexibleUpdate,
    handleOpenStorePage,
    handleStartListener,
    handleRemoveListener,
  }
}
