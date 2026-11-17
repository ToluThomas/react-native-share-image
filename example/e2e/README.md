# End-to-end tests

[Maestro](https://maestro.mobile.dev) flows that tap the example app's share buttons and check that the system share sheet opens with the right image. They run on every pull request (`.github/workflows/e2e.yml`) for Android and iOS.

| Flow | Checks |
|---|---|
| `share-full-screenshot.yaml` | `shareScreenshot()` of the whole screen |
| `share-partial-screenshot.yaml` | `shareScreenshot()` of a view found by `nativeID` |
| `share-remote-image.yaml` | `shareImageFromUri()` with an `http://` image, downloaded from a local web server so the test doesn't depend on the internet |
| `share-local-file.yaml` | `shareImageFromUri()` with a local `file://` image |

The image flows open the example's `shareimageexample://share-image?uri=…` deep link, which shares the given URI. Each flow also checks the app's status line, which shows whether the share promise resolved (`Shared`) or rejected (`Error: <code>`).

## Running locally

Run one platform at a time: an emulator and a simulator together use a lot of memory.

1. Install the [Maestro CLI](https://maestro.mobile.dev/getting-started/installing-maestro). You also need `python3`, which serves the remote test image.
2. Start Metro: `npm start` in `example/`.
3. Install a debug build on one booted emulator or simulator:
   - Android: `npm run android`
   - iOS: `npm run ios`
4. Run the flows:

   ```sh
   example/e2e/run.sh android   # or: example/e2e/run.sh ios
   ```

`run.sh` copies `fixtures/local-test.jpg` into the app's storage, serves `fixtures/` on `http://localhost:8090`, then runs every flow in `flows/`. Extra arguments go to `maestro test`, e.g. `--format JUNIT --output report.xml`.

On failure, Maestro saves screenshots and the screen hierarchy under `~/.maestro/tests/`. In CI they're uploaded as a build artifact.

## Android unit tests

The library's Android code also has Robolectric tests in `android/src/test/`. They run through the example app's Gradle build:

```sh
cd example/android && ./gradlew :react-native-share-image:testDebugUnitTest
```
