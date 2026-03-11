Share2Save
=========

Share2Save is a small Android utility that receives shared content (images, videos, audio, or generic files) from other apps and saves it to the user's Pictures folder using MediaStore when possible. A short Toast notification confirms each successful save.

Status
- Vibe coded — implemented quickly with a focus on practicality and minimal surface area.

Use case
- Save files (photos, videos, or other shared content) from the Android share menu directly into a dedicated folder (Pictures/Share2Save). Useful for quickly archiving media or attachments without opening a full gallery or downloads manager.

Getting started
- Open the project in Android Studio.
- Build and install on a connected device or emulator.
- From any app, choose Share → Share2Save to save the shared content.

- Build the debug APK (Windows PowerShell):

```powershell
.\gradlew.bat assembleDebug
```

Prerequisites
- Java / JDK 17 and `JAVA_HOME` configured.
- Create `local.properties` at the project root (copy `local.properties.template`) and set `sdk.dir` to your Android SDK path.
- Ensure the Gradle wrapper JAR is present or install Gradle on your system.

Notes
- On Android Q (API 29) and above the app uses MediaStore with a RELATIVE_PATH inside `Pictures/Share2Save`.
- On older Android versions, files are written to `Environment.DIRECTORY_PICTURES` directly.
- This project is intentionally minimal and focused on a single responsibility: saving shared content.