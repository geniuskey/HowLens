# Camera module verification

Run the app build and unit tests from `android/` after the App integrator applies
the dependency and manifest snippets in `INTEGRATION.md`:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

The camera pane reports a callback URI only for a nonempty JPEG no larger than
10 MiB. Its CameraX resolution selector excludes sizes above 20 MP. UI or
device verification is separate from these build and unit-test results.
