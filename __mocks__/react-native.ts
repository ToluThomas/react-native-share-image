import { jest } from "@jest/globals";

export const NativeModules = {
  RNShareImage: {
    shareScreenshot: jest
      .fn<() => Promise<void>>()
      .mockResolvedValue(undefined),
    shareImageFromUri: jest
      .fn<() => Promise<void>>()
      .mockResolvedValue(undefined),
  },
};

export const TurboModuleRegistry = {
  getEnforcing: jest.fn(() => ({
    shareScreenshot: jest
      .fn<() => Promise<void>>()
      .mockResolvedValue(undefined),
    shareImageFromUri: jest
      .fn<() => Promise<void>>()
      .mockResolvedValue(undefined),
  })),
};
