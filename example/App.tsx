import React from 'react';
import { Button, StyleSheet, Text, View, Image, StatusBar } from 'react-native';
import { shareScreenshot, shareImageFromUri } from 'react-native-share-image';

function App(): React.JSX.Element {
  const handleShareScreenshot = () => {
    shareScreenshot({
      message: 'Check out this screenshot!',
      filename: 'screenshot',
      shareTitle: 'Share Screenshot',
    });
  };

  const handleSharePartialScreenshot = () => {
    shareScreenshot({
      id: 'captureArea',
      message: 'Check out this view!',
      filename: 'partialScreen',
      shareTitle: 'Share View',
    });
  };

  const handleShareImage = () => {
    shareImageFromUri({
      imageUri: 'https://reactnative.dev/img/tiny_logo.png',
      message: 'Check out this image!',
      shareTitle: 'Share Image',
    });
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#f5f5f5" />
      <View style={styles.header}>
        <Text style={styles.title}>Share Image Example</Text>
        <Text style={styles.subtitle}>React Native Share Image Library</Text>
      </View>

      <View nativeID="captureArea" style={styles.captureArea}>
        <Image
          source={{ uri: 'https://reactnative.dev/img/tiny_logo.png' }}
          style={styles.logo}
        />
        <Text style={styles.captureText}>This view can be captured</Text>
        <Text style={styles.captureSubtext}>Using nativeID="captureArea"</Text>
      </View>

      <View style={styles.buttonContainer}>
        <View style={styles.buttonWrapper}>
          <Button
            title="Share Full Screenshot"
            onPress={handleShareScreenshot}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button
            title="Share Partial Screenshot"
            onPress={handleSharePartialScreenshot}
          />
        </View>

        <View style={styles.buttonWrapper}>
          <Button title="Share Image from URI" onPress={handleShareImage} />
        </View>
      </View>
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
});

export default App;
