const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');
const path = require('path');

const packageRoot = path.resolve(__dirname, '..');

/**
 * Metro configuration
 * https://reactnative.dev/docs/metro
 *
 * @type {import('@react-native/metro-config').MetroConfig}
 */

/**
 * NOTE: This metro config is specific to this example project which references
 * the parent directory as a local package (via "file:.." in package.json).
 *
 * If you're using react-native-share-image in your own project, you don't need
 * to copy this config. Just install the package normally with npm/yarn and use
 * the default metro config.
 */
const config = {
  watchFolders: [packageRoot],
  resolver: {
    nodeModulesPaths: [
      path.resolve(__dirname, 'node_modules'),
    ],
  },
};

module.exports = mergeConfig(getDefaultConfig(__dirname), config);
