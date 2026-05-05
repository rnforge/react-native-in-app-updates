const mockObject = {
  startImmediateUpdate: jest.fn(),
  startFlexibleUpdate: jest.fn(),
  completeFlexibleUpdate: jest.fn(),
  getUpdateStatus: jest.fn(),
  addInstallStateListener: jest.fn(),
  removeInstallStateListener: jest.fn(),
}

export const NitroModules = {
  createHybridObject: jest.fn((_name: string): any => mockObject),
}

export function resetMockObject() {
  mockObject.startImmediateUpdate.mockReset()
  mockObject.startFlexibleUpdate.mockReset()
  mockObject.completeFlexibleUpdate.mockReset()
  mockObject.getUpdateStatus.mockReset()
  mockObject.addInstallStateListener.mockReset()
  mockObject.removeInstallStateListener.mockReset()
}

export { mockObject }
