# Chitra AI — Complete Setup Guide

---

## WHAT YOU'VE GOT

A complete Android Studio project with:
- 5 Kotlin source files (Activities, Services, Managers, Adapter, Model)
- 5 XML layouts (Splash, Permission, Main, Session, Gallery)
- Full drawable set (buttons, icons, status dots, backgrounds)
- Animations (fade, slide, pulse)
- Firebase integration with simulation fallback
- Screen sharing via MediaProjection API
- Gallery file picker with multi-select and upload
- Dark premium UI (#0D0D0D / #00C896 / #D4AF37)

---

## STEP 1 — INSTALL ANDROID STUDIO

Download from: https://developer.android.com/studio
Install and open Android Studio. Choose "New Project" is NOT needed — you'll open the existing folder.

---

## STEP 2 — OPEN THE PROJECT

1. In Android Studio: **File → Open**
2. Navigate to the `ChitraAI` folder you extracted
3. Click **OK** — Android Studio will index the project (takes 1–3 min)

---

## STEP 3 — ADD FONTS (Required)

The app uses **Poppins** and **Inter** fonts. Download them:

1. In Android Studio, open `res/font/` folder
2. Right-click `font` folder → **New → Font Resource File** OR download manually:
   - Poppins Regular: https://fonts.google.com/specimen/Poppins → Download → pick `Poppins-Regular.ttf` and `Poppins-SemiBold.ttf`
   - Inter Regular: https://fonts.google.com/specimen/Inter → Download → pick `Inter-Regular.ttf` and `Inter-SemiBold.ttf`
3. Place the `.ttf` files in: `app/src/main/res/font/`
   - `poppins_regular.ttf`
   - `poppins_semibold.ttf`
   - `inter_regular.ttf`
   - `inter_semibold.ttf`

> **Tip:** Filenames must be all lowercase with underscores — no hyphens.

---

## STEP 4 — FIREBASE SETUP (For real backend; skip for simulation)

### Option A: Use Simulation Mode (No Firebase)
The app automatically falls back to simulation if Firebase isn't configured.
Screen sharing UI works, connection simulates in 1.5 seconds, file upload simulates progress.
**Skip to Step 5 if you just want to test the app.**

### Option B: Real Firebase (Full backend)

1. Go to https://console.firebase.google.com
2. Create a new project (e.g. "ChitraAI")
3. Add an Android app with package name: `com.chitraai.app`
4. Download `google-services.json`
5. Replace the placeholder file at: `app/google-services.json`
6. In Firebase Console:
   - Enable **Realtime Database** (set rules to allow read/write for testing)
   - Enable **Storage** (for file uploads)
   - Set Realtime Database Rules to:
     ```json
     {
       "rules": {
         ".read": true,
         ".write": true
       }
     }
     ```
   > ⚠️ Change these rules to authenticated-only before sharing publicly.

---

## STEP 5 — SYNC AND BUILD

1. In Android Studio: **File → Sync Project with Gradle Files**
2. Wait for sync to complete (green bar at bottom)
3. If you see "SDK not found": go to **File → Project Structure → SDK Location** and set your Android SDK path

---

## STEP 6 — RUN ON DEVICE OR EMULATOR

### On a Real Device (Recommended):
1. On your Android phone: **Settings → About Phone → tap "Build Number" 7 times** (enables Developer Mode)
2. Go to **Settings → Developer Options → Enable USB Debugging**
3. Connect phone via USB cable
4. In Android Studio: select your device from the dropdown (top bar)
5. Click the ▶ **Run** button

### On Emulator:
1. In Android Studio: **Tools → Device Manager → Create Device**
2. Choose Pixel 6, API 33 or higher
3. Click **Run** after creating

---

## STEP 7 — FIRST TIME APP USAGE

1. App opens to **Splash Screen** → animates in
2. **Permissions Screen** → tap "Grant Permissions" → allow Storage + Notifications
3. **Home Screen** → tap "Connect to Krish"
4. Android shows the **screen capture permission dialog** → tap "Start Now"
5. App enters **Session Screen** — screen sharing is active, timer starts
6. Tap **"Share Files"** to open gallery and send photos/videos
7. Tap **"End Session"** to stop

---

## STEP 8 — GENERATE RELEASE APK

1. In Android Studio: **Build → Generate Signed Bundle / APK**
2. Choose **APK**
3. Click **Create new keystore** (first time):
   - Set a path, password, alias, and fill in your details
   - Save the keystore file somewhere safe — you'll need it for future updates
4. Click **Next** → choose **release** → click **Finish**
5. APK appears in: `app/release/app-release.apk`

### Install APK on another device:
1. Copy the APK file to the target phone
2. On that phone: **Settings → Install Unknown Apps → Allow from Files app**
3. Open the APK file and tap Install

---

## FILE STRUCTURE REFERENCE

```
ChitraAI/
├── app/
│   ├── google-services.json          ← Replace with your real Firebase file
│   ├── build.gradle                  ← App dependencies
│   └── src/main/
│       ├── AndroidManifest.xml       ← Permissions & components declared here
│       ├── java/com/chitraai/app/
│       │   ├── ui/
│       │   │   ├── SplashActivity.kt
│       │   │   ├── PermissionExplainActivity.kt
│       │   │   ├── MainActivity.kt      ← Home screen "Connect to Krish"
│       │   │   ├── SessionActivity.kt   ← Active session screen
│       │   │   └── GalleryActivity.kt   ← File picker
│       │   ├── service/
│       │   │   ├── ScreenShareService.kt     ← MediaProjection screen capture
│       │   │   └── ChitraAccessibilityService.kt
│       │   ├── manager/
│       │   │   ├── PermissionManager.kt
│       │   │   ├── ConnectionManager.kt      ← Firebase + simulation
│       │   │   └── FileUploadManager.kt
│       │   ├── adapter/
│       │   │   └── GalleryAdapter.kt
│       │   └── model/
│       │       └── MediaItem.kt
│       └── res/
│           ├── layout/               ← All XML screen layouts
│           ├── drawable/             ← Buttons, icons, backgrounds
│           ├── values/               ← Colors, strings, themes
│           ├── anim/                 ← Fade, slide, pulse animations
│           ├── font/                 ← ⚠️ Add your .ttf font files here
│           └── xml/                  ← Accessibility service config
├── build.gradle                      ← Root build config
└── settings.gradle
```

---

## COMMON ERRORS & FIXES

| Error | Fix |
|-------|-----|
| `Font resource not found` | Add the 4 `.ttf` files to `res/font/` as described in Step 3 |
| `google-services.json not found` | Use the placeholder already included, or add your real Firebase file |
| `SDK location not found` | File → Project Structure → set SDK path |
| `Manifest merger failed` | Make sure `compileSdk = 34` in `app/build.gradle` |
| `Cannot resolve symbol 'R'` | Build → Clean Project, then Build → Rebuild Project |
| Screen share permission denied | Must tap "Start Now" in the system dialog — this is required by Android |
| No media in gallery | Grant storage permission in device Settings → Apps → Chitra AI → Permissions |

---

## CUSTOMISE "KRISH"

To change the name "Krish" to someone else:
- Open `res/values/strings.xml`
- Find all instances of "Krish" and replace with the family member's name
- Also update button text in `activity_main.xml`

---

## SECURITY NOTES

- Screen sharing ONLY starts when the user taps "Connect to Krish" AND approves the Android system dialog
- No data is recorded without user action
- All permissions are user-triggered
- Firebase data is scoped to your project only
- The accessibility service is optional and off by default

---

*Built with Kotlin · MediaProjection · Firebase · Material Design 3*
