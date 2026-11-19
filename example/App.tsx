import React, { useCallback, useEffect, useState } from 'react';
import {
  Button,
  StyleSheet,
  Text,
  View,
  Image,
  StatusBar,
  Linking,
} from 'react-native';
import { shareScreenshot, shareImageFromUri } from 'react-native-share-image';

const REMOTE_IMAGE_URL = 'https://reactnative.dev/img/tiny_logo.png';

// Opening shareimageexample://share-image?uri=<encoded uri> shares that image.
// The end-to-end tests use it to share a local file they place on the device.
const DEEP_LINK_PREFIX = 'shareimageexample://share-image?uri=';

function App(): React.JSX.Element {
  const [status, setStatus] = useState('Ready');

  // Shows the outcome of each share so tests (and people) can see what happened.
  const track = useCallback((share: Promise<void>) => {
    setStatus('Sharing');
    share
      .then(() => setStatus('Shared'))
      .catch((error: { code?: string; message?: string }) =>
        setStatus(`Error: ${error.code ?? error.message ?? 'unknown'}`),
      );
  }, []);

  const shareFromUri = useCallback(
    (imageUri: string) =>
      track(
        shareImageFromUri({
          imageUri,
          message: 'Check out this image!',
          shareTitle: 'Share Image',
        }),
      ),
    [track],
  );

  useEffect(() => {
    const handleUrl = (url?: string | null) => {
      if (url?.startsWith(DEEP_LINK_PREFIX)) {
        shareFromUri(decodeURIComponent(url.slice(DEEP_LINK_PREFIX.length)));
      }
    };
    Linking.getInitialURL().then(handleUrl);
    const subscription = Linking.addEventListener('url', ({ url }) =>
      handleUrl(url),
    );
    return () => subscription.remove();
  }, [shareFromUri]);

  const handleShareScreenshot = () => {
    track(
      shareScreenshot({
        message: 'Check out this screenshot!',
        filename: 'screenshot',
        shareTitle: 'Share Screenshot',
      }),
    );
  };

  const handleSharePartialScreenshot = () => {
    track(
      shareScreenshot({
        id: 'captureArea',
        message: 'Check out this view!',
        filename: 'partialScreen',
        shareTitle: 'Share View',
      }),
    );
  };

  const handleShareImage = () => shareFromUri(REMOTE_IMAGE_URL);

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <View style={styles.header}>
        <Text style={styles.title}>Share Image Example</Text>
        <Text style={styles.subtitle}>React Native Share Image Library</Text>
      </View>

      <View nativeID="captureArea" style={styles.captureArea}>
        <Image source={{ uri: REMOTE_IMAGE_URL }} style={styles.logo} />
        <Text style={styles.captureText}>This view can be captured</Text>
        <Text style={styles.captureSubtext}>Using nativeID="captureArea"</Text>
      </View>

      <View style={styles.buttonContainer}>
        <View style={styles.buttonWrapper}>
          <Button
            testID="share-full-screenshot"
            title="Share Full Screenshot"
            onPress={handleShareScreenshot}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            testID="share-partial-screenshot"
            title="Share Partial Screenshot"
            onPress={handleSharePartialScreenshot}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            testID="share-image-from-uri"
            title="Share Image from URI"
            onPress={handleShareImage}
          />
        </View>
      </View>

      <Text testID="share-status" style={styles.status}>
        {status}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    paddingTop: 50,
  },
  header: {
    padding: 20,
    alignItems: 'center',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  subtitle: {
    fontSize: 14,
    color: '#666',
    marginTop: 4,
  },
  captureArea: {
    margin: 20,
    padding: 30,
    backgroundColor: '#fff',
    borderRadius: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  logo: {
    width: 80,
    height: 80,
    marginBottom: 16,
  },
  captureText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  captureSubtext: {
    fontSize: 12,
    color: '#999',
    marginTop: 4,
  },
  buttonContainer: {
    padding: 20,
  },
  buttonWrapper: {
    marginVertical: 8,
  },
  status: {
    textAlign: 'center',
    fontSize: 14,
    color: '#666',
  },
});

export default App;
