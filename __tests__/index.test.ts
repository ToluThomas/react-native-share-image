import { describe, it, expect, beforeEach, jest } from "@jest/globals";
import { shareScreenshot, shareImageFromUri } from "../src/index";
import { NativeModules } from "react-native";

const mockModule = NativeModules.RNShareImage;

describe("react-native-share-image", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe("shareScreenshot", () => {
    it("should call native module with default parameters when no options provided", async () => {
      await shareScreenshot();

      expect(mockModule.shareScreenshot).toHaveBeenCalledWith(
        null,
        "Screenshot",
        expect.any(String),
        "Screenshot",
      );
    });

    it("should call native module with custom id", async () => {
      await shareScreenshot({ id: "myViewId" });

      expect(mockModule.shareScreenshot).toHaveBeenCalledWith(
        "myViewId",
        "Screenshot",
        expect.any(String),
        "Screenshot",
      );
    });

    it("should call native module with custom message", async () => {
      await shareScreenshot({ message: "Custom message" });

      expect(mockModule.shareScreenshot).toHaveBeenCalledWith(
        null,
        "Custom message",
        expect.any(String),
        "Screenshot",
      );
    });

    it("should call native module with custom filename", async () => {
      await shareScreenshot({ filename: "my-screenshot" });

      expect(mockModule.shareScreenshot).toHaveBeenCalledWith(
        null,
        "Screenshot",
        "my-screenshot",
        "Screenshot",
      );
    });

    it("should call native module with custom shareTitle", async () => {
      await shareScreenshot({ shareTitle: "Share via" });

      expect(mockModule.shareScreenshot).toHaveBeenCalledWith(
        null,
        "Screenshot",
        expect.any(String),
        "Share via",
      );
    });

    it("should call native module with all custom options", async () => {
      await shareScreenshot({
        id: "captureArea",
        message: "Check this out!",
        filename: "my-screenshot",
        shareTitle: "Share via",
      });

      expect(mockModule.shareScreenshot).toHaveBeenCalledWith(
        "captureArea",
        "Check this out!",
        "my-screenshot",
        "Share via",
      );
    });

    it("should use timestamp as default filename", async () => {
      const before = Date.now();
      await shareScreenshot();
      const after = Date.now();

      const call = mockModule.shareScreenshot.mock.calls[0];
      const filename = parseInt(call[2], 10);

      expect(filename).toBeGreaterThanOrEqual(before);
      expect(filename).toBeLessThanOrEqual(after);
    });
  });

  describe("shareImageFromUri", () => {
    it("should call native module with required imageUri and defaults", async () => {
      await shareImageFromUri({ imageUri: "file:///path/to/image.png" });

      expect(mockModule.shareImageFromUri).toHaveBeenCalledWith(
        "file:///path/to/image.png",
        "Screenshot",
        "Image",
      );
    });

    it("should call native module with custom message", async () => {
      await shareImageFromUri({
        imageUri: "https://example.com/image.png",
        message: "Check this out!",
      });

      expect(mockModule.shareImageFromUri).toHaveBeenCalledWith(
        "https://example.com/image.png",
        "Check this out!",
        "Image",
      );
    });

    it("should call native module with custom shareTitle", async () => {
      await shareImageFromUri({
        imageUri: "https://example.com/image.png",
        shareTitle: "Share Image",
      });

      expect(mockModule.shareImageFromUri).toHaveBeenCalledWith(
        "https://example.com/image.png",
        "Screenshot",
        "Share Image",
      );
    });

    it("should call native module with all custom options", async () => {
      await shareImageFromUri({
        imageUri: "https://example.com/image.png",
        message: "Check this out!",
        shareTitle: "Share via",
      });

      expect(mockModule.shareImageFromUri).toHaveBeenCalledWith(
        "https://example.com/image.png",
        "Check this out!",
        "Share via",
      );
    });

    it("should handle local file URIs", async () => {
      await shareImageFromUri({ imageUri: "file:///storage/image.jpg" });

      expect(mockModule.shareImageFromUri).toHaveBeenCalledWith(
        "file:///storage/image.jpg",
        "Screenshot",
        "Image",
      );
    });

    it("should handle content URIs", async () => {
      await shareImageFromUri({
        imageUri: "content://media/external/images/123",
      });

      expect(mockModule.shareImageFromUri).toHaveBeenCalledWith(
        "content://media/external/images/123",
        "Screenshot",
        "Image",
      );
    });
  });

  describe("error handling", () => {
    it("should reject when native module shareScreenshot fails", async () => {
      const error = new Error("Native error");
      mockModule.shareScreenshot.mockRejectedValueOnce(error);

      await expect(shareScreenshot()).rejects.toThrow("Native error");
    });

    it("should reject when native module shareImageFromUri fails", async () => {
      const error = new Error("Download failed");
      mockModule.shareImageFromUri.mockRejectedValueOnce(error);

      await expect(
        shareImageFromUri({ imageUri: "https://example.com/image.png" }),
      ).rejects.toThrow("Download failed");
    });
  });
});
