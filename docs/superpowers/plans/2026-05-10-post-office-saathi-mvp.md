# Post Office Saathi MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first usable Post Office Saathi app slice with forms discovery/download architecture and a guided document-to-PDF flow scaffold.

**Architecture:** Use MVVM-style state holders with focused domain classes for search, parsing, file naming, and layout definitions. Keep GitHub forms fetching, local file caching, and search feedback behind repository interfaces so real network/storage behavior can evolve without rewriting UI.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Android app storage, Android intents, JUnit tests.

---

### Task 1: Tested Domain Core

**Files:**
- Create: `app/src/test/java/roy/ij/postofficesaathi/forms/FormSearchEngineTest.kt`
- Create: `app/src/test/java/roy/ij/postofficesaathi/forms/FormsIndexParserTest.kt`
- Create: `app/src/test/java/roy/ij/postofficesaathi/pdf/PdfDomainTest.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/domain/forms/FormItem.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/domain/forms/FormSearchEngine.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/data/forms/FormsIndexParser.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/domain/pdf/PdfLayoutType.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/domain/pdf/PdfFileNameFactory.kt`

- [ ] Write tests for forgiving form search, JSON parsing, layout document labels, and sanitized PDF names.
- [ ] Run `.\gradlew.bat testDebugUnitTest` and verify tests fail because production classes are missing.
- [ ] Implement the minimal domain/data classes.
- [ ] Run `.\gradlew.bat testDebugUnitTest` and verify the tests pass.

### Task 2: App Shell and Design System UI

**Files:**
- Modify: `app/src/main/java/roy/ij/postofficesaathi/MainActivity.kt`
- Modify: `app/src/main/java/roy/ij/postofficesaathi/ui/theme/Color.kt`
- Modify: `app/src/main/java/roy/ij/postofficesaathi/ui/theme/Theme.kt`
- Modify: `app/src/main/java/roy/ij/postofficesaathi/ui/theme/Type.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/ui/components/SaathiComponents.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/ui/home/HomeScreen.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsScreen.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowScreens.kt`

- [ ] Replace the template greeting with a multi-screen Compose app.
- [ ] Apply the `DESIGN.md` postal red, warm surfaces, large typography, 48dp touch targets, flat cards, and no shadows.
- [ ] Add Home, Forms, PDF layout, capture guidance, corner adjustment, preview, name input, and success screens.

### Task 3: Repository and Local Behavior Scaffold

**Files:**
- Create: `app/src/main/java/roy/ij/postofficesaathi/data/forms/FormsRepository.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/data/forms/GitHubFormsRepository.kt`
- Create: `app/src/main/java/roy/ij/postofficesaathi/data/forms/SearchFeedbackRepository.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] Add internet and camera permissions.
- [ ] Fetch only the JSON index from GitHub raw content.
- [ ] Cache the index and downloaded files in app storage.
- [ ] Expose open/share-ready file references without broad storage permissions.
- [ ] Keep failed search feedback as a no-op interface for later Google Sheet integration.

### Task 4: Verification

- [ ] Run `.\gradlew.bat testDebugUnitTest`.
- [ ] Run `.\gradlew.bat assembleDebug`.
- [ ] Report exact verification results and remaining gaps.
