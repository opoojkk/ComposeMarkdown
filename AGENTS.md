# ComposeMarkdown

Compose Multiplatform Markdown rendering library (viewer, not editor). Single Gradle module with parser, model, and UI layers.

## Cursor Cloud specific instructions

### Environment prerequisites

- **JDK 17** is required (`jvmToolchain(17)` in `build.gradle.kts`). Install with `sudo apt-get install -y openjdk-17-jdk` if missing.
- **Android SDK** (platform 35) is needed for the `check` task to pass. Install via Android cmdline-tools at `/opt/android-sdk`.
- `JAVA_HOME` must point to `/usr/lib/jvm/java-17-openjdk-amd64` and `ANDROID_HOME` to `/opt/android-sdk`. Both are set in `~/.bashrc`.

### Key Gradle commands

| Task | Command |
|---|---|
| Build (desktop) | `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew compileKotlinDesktop` |
| Run desktop app | `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew run` |
| Desktop tests | `JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew desktopTest` |
| Full check | `ANDROID_HOME=/opt/android-sdk JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew check` |

### Gotchas

- The desktop app uses Skiko for rendering. On headless/cloud VMs, it logs `[SKIKO] warn: Fallback to next API` and `Cannot create Linux GL context` — this is a non-fatal warning. Skiko falls back to software rendering and the app runs fine on display `:1`.
- `./gradlew check` includes Android unit tests. It will fail if `ANDROID_HOME` is not set. Use `./gradlew desktopTest` for desktop-only checks.
- iOS targets are declared but have no source files. Gradle warns about disabled iOS/Native targets on non-macOS machines — this is expected.
- No dedicated lint tool (detekt, ktlint) is configured. `./gradlew check` is the standard verification entry point.
- No test files exist yet in the project (`commonTest` source set is empty).
