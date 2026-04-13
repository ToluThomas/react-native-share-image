import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  shareScreenshot(
    id: string | null,
    message: string,
    filename: string,
    shareTitle: string
  ): void;

  shareImageFromUri(
    imageUri: string,
    message: string,
    shareTitle: string
  ): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RNShareImage');
