/**
 * Minimal example source scaffold for @rnforge/react-native-in-app-updates
 *
 * IMPORTANT: This is NOT a fully generated React Native app. It does not
 * include native Android or iOS project files (android/, ios/, Pods, Gradle,
 * etc.). Use this file as a reference for how to integrate the v1 API into
 * your own React Native application.
 */

import React from 'react'
import {
  Button,
  Platform,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'
import { useInAppUpdatesExample } from './useInAppUpdatesExample'

export default function App() {
  const {
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
  } = useInAppUpdatesExample()

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scroll}>
        <Text style={styles.title}>RNForge In-App Updates</Text>
        <Text style={styles.subtitle}>Minimal example source scaffold</Text>

        <View style={styles.section}>
          <Text style={styles.label}>iOS App Store ID (optional):</Text>
          <TextInput
            style={styles.input}
            value={appStoreId}
            onChangeText={setAppStoreId}
            placeholder="e.g. 1234567890"
            keyboardType="numeric"
            autoCapitalize="none"
          />
        </View>

        <View style={styles.section}>
          <Button title="getUpdateStatus()" onPress={handleGetUpdateStatus} />
        </View>

        <View style={styles.section}>
          <Button
            title="startImmediateUpdate() (Android Play)"
            onPress={handleStartImmediateUpdate}
          />
        </View>

        <View style={styles.section}>
          <Button
            title="startFlexibleUpdate() (Android Play)"
            onPress={handleStartFlexibleUpdate}
          />
        </View>

        <View style={styles.section}>
          <Button
            title="completeFlexibleUpdate() (Android Play)"
            onPress={handleCompleteFlexibleUpdate}
          />
        </View>

        <View style={styles.section}>
          <Button title="openStorePage()" onPress={handleOpenStorePage} />
        </View>

        <View style={styles.section}>
          <Button
            title={
              listenerActive
                ? 'addInstallStateListener() (active)'
                : 'addInstallStateListener()'
            }
            onPress={handleStartListener}
          />
        </View>
        <View style={styles.section}>
          <Button
            title="removeInstallStateListener()"
            onPress={handleRemoveListener}
          />
        </View>

        <View style={styles.logSection}>
          <Text style={styles.logTitle}>Log</Text>
          {log.map((line, i) => (
            <Text key={i} style={styles.logLine}>
              {line}
            </Text>
          ))}
        </View>
      </ScrollView>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  scroll: {
    padding: 16,
    paddingBottom: 40,
  },
  title: {
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 13,
    color: '#666',
    marginBottom: 16,
  },
  section: {
    marginBottom: 10,
  },
  label: {
    fontSize: 14,
    marginBottom: 4,
    fontWeight: '500',
  },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 6,
    padding: 10,
    fontSize: 14,
  },
  logSection: {
    marginTop: 20,
    padding: 12,
    backgroundColor: '#f5f5f5',
    borderRadius: 6,
  },
  logTitle: {
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 8,
  },
  logLine: {
    fontSize: 11,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
    color: '#333',
    marginBottom: 6,
  },
})
