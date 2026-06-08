<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Inter&weight=700&size=30&duration=2800&pause=900&color=0F6B4B&center=true&vCenter=true&width=760&lines=Post+Office+Saathi;Forms%2C+PDFs%2C+and+counter+workflows;Built+for+real+post+office+use" alt="Post Office Saathi animated title" />
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=roy.ij.postofficesaathi">
    <img src="https://img.shields.io/badge/Get%20it%20on-Google%20Play-0F9D58?style=for-the-badge&logo=googleplay&logoColor=white" alt="Get it on Google Play" />
  </a>
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Firebase-Analytics%20%2B%20Crashlytics-FFCA28?style=for-the-badge&logo=firebase&logoColor=111111" alt="Firebase Analytics and Crashlytics" />
</p>

<p align="center">
  <strong>Post Office Saathi</strong> is a frontend-only Android app that helps post office staff and agents quickly find forms, download official PDFs, and create customer-ready document PDFs from captured card photos.
</p>

---

## What It Does

Post Office Saathi focuses on practical counter workflows:

- Download and open post office forms.
- Search forms by common keywords and categories.
- Capture card/document photos with CameraX.
- Correct image corners before PDF creation.
- Arrange one, two, or three documents on a PDF page.
- Save created PDFs directly to public device storage.
- Show Recent Work for saved PDFs and downloaded forms.
- Provide clear offline feedback when forms cannot be downloaded.

## Available On Play Store

The app is available here:

[https://play.google.com/store/apps/details?id=roy.ij.postofficesaathi](https://play.google.com/store/apps/details?id=roy.ij.postofficesaathi)

## File Storage

Files are saved where users can find them from a normal file manager:

```text
Documents/PostOfficeSaathi
```

Downloaded forms are saved under:

```text
Documents/PostOfficeSaathi/Forms
```

Created PDFs and downloaded forms also appear in the app's Recent Work section, with Open and Share actions.

## Offline Behaviour

PDF creation works locally after photos are captured.

Form downloads need internet. When the user is offline, the app shows a clear message and avoids leaving the user guessing. If a cached form index is available, saved forms can still be shown.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- CameraX
- OpenCV
- Firebase Analytics
- Firebase Crashlytics
- Android MediaStore for public document storage

## Project Structure

```text
app/src/main/java/roy/ij/postofficesaathi
+-- analytics      # Firebase analytics and crash reporting helpers
+-- data           # Forms, PDF generation, public storage, recent work
+-- domain         # Form and PDF domain models/utilities
+-- ui             # Compose screens, navigation, view models, components
```

## Release Build

Build the Play Store release bundle:

```powershell
.\gradlew.bat clean
.\gradlew.bat testDebugUnitTest lintDebug
.\gradlew.bat bundleRelease
```

Release artifacts:

```text
app/build/outputs/bundle/release/app-release.aab
app/build/outputs/mapping/release/mapping.txt
app/build/outputs/native-debug-symbols/release/native-debug-symbols.zip
```

Crashlytics mapping upload:

```powershell
.\gradlew.bat uploadCrashlyticsMappingFileRelease
```

## Signing

Release signing is read from `keystore.properties` at the project root:

```properties
storeFile=<release-keystore-path>
storePassword=<release-keystore-password>
keyAlias=<release-key-alias>
keyPassword=<release-key-password>
```

## Privacy

Analytics are used to understand feature usage and app stability. The app is designed to avoid sending sensitive personal data through analytics events.

---

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=120&color=0:0F6B4B,100:F4B740&section=footer&text=Built%20for%20faster%20post%20office%20workflows&fontColor=ffffff&fontSize=18&animation=twinkling" alt="Animated footer" />
</p>
