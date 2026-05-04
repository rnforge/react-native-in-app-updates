const mockObject = {
  startImmediateUpdate: jest.fn(),
  getUpdateStatus: jest.fn(),
}

export const NitroModules = {
  createHybridObject: jest.fn((_name: string): any => mockObject),
}

export function resetMockObject() {
  mockObject.startImmediateUpdate.mockReset()
  mockObject.getUpdateStatus.mockReset()
}

export { mockObject }
