<p align="center">
  <img src="https://readme-typing-svg.demolab.com?font=Inter&weight=800&size=32&duration=2600&pause=900&color=A00012&center=true&vCenter=true&width=820&lines=Post+Office+Saathi;Postal+forms%2C+PDFs%2C+and+savings+calculators;Built+for+faster+daily+post+office+work" alt="Post Office Saathi animated title" />
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
  <strong>Post Office Saathi</strong> is a frontend-only Android utility app for postal forms, document PDFs, and post office savings calculations.
</p>

<p align="center">
  <strong>Independent app.</strong> Post Office Saathi is not an official government app.
</p>

---

## Play Store

Post Office Saathi is available on Google Play:

[https://play.google.com/store/apps/details?id=roy.ij.postofficesaathi](https://play.google.com/store/apps/details?id=roy.ij.postofficesaathi)

## What It Does

Post Office Saathi is built around daily postal workflows:

- Search, download, open, and share postal form PDFs.
- Create clean PDFs from captured or imported document/card photos.
- Adjust document corners before PDF creation.
- Save created PDFs and downloaded forms to public file-manager-visible storage.
- View saved PDFs and downloaded forms in Recent Work.
- Calculate post office savings estimates for RD, TD, MIS, NSC, KVP, PPF, SSY, SCSS, SB, MSSC, and custom calculators.
- Suggest plans based on user-entered savings goals.
- Open Help, Privacy, feedback email, rating, and theme settings from the app.

## Main Features

### Download Forms

- Search forms by name or keyword.
- Download PDFs for later use.
- Open or share downloaded forms.
- Clear offline feedback when a form cannot be downloaded.
- Saved forms remain available from device storage.

### Create PDF

- Capture document/card photos with CameraX.
- Import existing images.
- Adjust corners for cleaner document output.
- Choose one, two, or three document layouts.
- Save the final PDF to public Documents storage.
- Open and share created PDFs from Recent Work.

### Interest Calculator

- Scheme calculators for common post office savings products.
- Uses bundled rate data from:

```text
app/src/main/assets/rates.json
```

- Supports current and historical rate lookup where data is available.
- Falls back to the current rate when a selected date has no matching rate.
- Keeps heavy calculation and parsing work off the UI thread.

Calculator accuracy notes:

- RD and TD use scheme-specific formulas instead of a generic compound-interest formula.
- MIS and SCSS distinguish payout, principal returned, and total received.
- Year-wise interest rows are generated per scheme where enough inputs exist.
- PPF and SSY year-wise rows are hidden for now because official-style accuracy needs monthly deposit timing, which the app does not currently collect.

## File Storage

Files are saved where users can find them from a normal file manager.

Created PDFs:

```text
Documents/PostOfficeSaathi
```

Downloaded forms:

```text
Documents/PostOfficeSaathi/Forms
```

The same files also appear in the app's Recent Work section with Open and Share actions.

## Offline Behaviour

- PDF creation works locally after photos are captured or imported.
- Downloading a new form needs internet.
- If the form list is cached, the app can still show saved form information while offline.
- When a download cannot continue offline, the app shows clear user feedback instead of silently failing.

## Privacy

- No login is required.
- Created PDFs are stored locally on the device.
- Downloaded forms are stored locally on the device.
- Firebase Analytics is used for non-sensitive product usage insights.
- Firebase Crashlytics is used for crash and stability reporting.
- Analytics events are designed to avoid personal document content, file paths, raw contact details, or sensitive user data.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- CameraX
- OpenCV
- Preferences DataStore
- Firebase Analytics
- Firebase Crashlytics
- Android MediaStore for public document storage

## Project Structure

```text
app/src/main/java/roy/ij/postofficesaathi
+-- analytics      # Firebase analytics and crash reporting helpers
+-- data           # Forms, preferences, review, PDF generation, storage, recent work
+-- domain         # Form, PDF, and calculator domain models/utilities
+-- ui             # Compose screens, navigation, view models, components
```

Important project files:

```text
app/src/main/assets/rates.json          # Bundled calculator rate data
public/rates.json                       # Public/source copy of rate data
public/agents.json                      # Agent directory source data
docs/play-store-listing.md              # Play Console listing copy
firebase-analytics-events.md            # Analytics event reference
```

## Release Build

Run the verification and release bundle commands:

```powershell
.\gradlew.bat clean
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:bundleRelease
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

Do not commit real keystore credentials.

---

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=120&color=0:A00012,55:F8EFE8,100:F4B740&section=footer&text=Made%20for%20faster%20postal%20workflows&fontColor=111827&fontSize=18&animation=twinkling" alt="Animated footer" />
</p>
