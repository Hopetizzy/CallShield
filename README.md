# 🛡️ CallShield AI — Autonomous Cyber Call Defense (100% Offline)

A native Android application built in Kotlin & Jetpack Compose designed specifically to eliminate predatory loan shark autodialers, virtual VoIP trunks (`+234 2...`, `02...`), and repetitive zero-spoof numbers (`+234 7000...`) **with zero internet connection required**.

---

## ⚡ Key Highlights

* **100% Offline & Private:** Operates entirely on-device without mobile data or Wi-Fi. No call data ever leaves your phone.
* **Sub-2ms Interception:** Intercepts incoming calls directly at the Android Telecom layer via `CallScreeningService` and drops them before your phone screen turns on or vibrates.
* **Pre-configured Nigerian Spam Rules:**
  * `+234 2...` / `02...` (Ibadan/VoIP Virtual Landlines & PBX trunks)
  * `0201...` / `+234 201...` (Lagos VoIP Dialers)
  * `+234 7000...` / `07000...` (Predatory loan shark multi-zero series)
  * `000` (Repetitive 3+ Zeros Spoof Heuristic)
  * `RESTRICTED / PRIVATE` (Concealed Caller IDs)
* **Contacts Whitelist Protection:** Never accidentally blocks friends, family, or business contacts saved in your address book.
* **Futuristic Cyberpunk UI:** OLED Dark mode, animated pulsing holographic radar, live telemetry stats, and a floating navigation bar.
* **In-App Neural Threat Simulator:** Interactive sandbox to test any phone number and see instant pass/fail rule breakdowns and latency stats.

---

## 🚀 How to Run and Test on Your Android Phone (Without Google Play)

### Prerequisites:
* **Android Studio** (Koala / Ladybug / Iguana or later)
* An Android Phone running **Android 10 (API 29) or higher**

### Step-by-Step:

1. **Open in Android Studio:**
   * Open Android Studio -> Click **Open** -> Select `c:\Users\HP\Documents\Scam_Calls`.
   * Wait for Gradle sync to complete.

2. **Enable USB Debugging on Your Phone:**
   * On your Android phone, go to **Settings** > **About Phone**.
   * Tap **Build Number** 7 times until it says *"You are now a developer!"*.
   * Go back to **Settings** > **Developer Options** > Turn on **USB Debugging**.

3. **Deploy to Your Phone:**
   * Plug your phone into your PC via USB.
   * Select your phone model in the Android Studio device dropdown at the top.
   * Click the **Run ▶ (Green Play Button)**.

4. **Optionally Build Standalone APK:**
   * To build an installable APK file, run in terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   * The APK will be ready at:
     ```
     app/build/outputs/apk/debug/app-debug.apk
     ```
   * Transfer this `.apk` to your phone via USB or WhatsApp and tap to install.

---

## 🎯 Verifying Protection (Testing)

1. **In-App Sandbox Tester:**
   * Open the app -> Tap the **Sandbox (Flask)** icon in the bottom bar.
   * Type any test number (e.g. `+2342018889999` or `07000000000`) and tap **EXECUTE SIMULATION**.
   * It will show you the exact rule triggered and whether the call would be dropped.
2. **Live Call Test:**
   * Add a custom test prefix for a friend's number or second SIM.
   * Have them call you: The call is instantly terminated with **zero ringing and zero screen wake**, and will appear immediately in the **Threat Ledger**.
