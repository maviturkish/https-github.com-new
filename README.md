# 🍅 Pomodoro

A simple Pomodoro timer for Android.

- 25 min focus → 5 min short break, long break (15 min) after every 4 focus sessions
- Start / Pause / Resume, Reset, Skip
- Keeps counting when the app is closed; notification with sound + vibration when time is up
- Progress ring, session counter (long-press the counter to reset it)

## Install on your phone

1. Open the [**latest release**](../../releases/latest) on your Android phone.
2. Download **Pomodoro.apk** and tap it.
3. If asked, allow "Install unknown apps" for your browser / Files app, then tap **Install**.

Every push to `main` builds a new APK automatically (GitHub Actions) and publishes it as a new release.
New builds install over the old one as an update.

## Build locally

Requires JDK 17 and the Android SDK:

```
gradle assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`
