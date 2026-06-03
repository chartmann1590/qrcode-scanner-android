# 📱 QR Scanner — Modern Android QR & Barcode Reader

<div align="center">

[![Android Build](https://img.shields.io/badge/Android-Debug_Build-success?logo=android&logoColor=white&color=10B981)](https://github.com/chartmann1590/qrcode-scanner-android)
[![GitHub Pages](https://img.shields.io/badge/GitHub_Pages-Active-blue?logo=github&color=06B6D4)](https://chartmann1590.github.io/qrcode-scanner-android/)
[![SDK Target](https://img.shields.io/badge/Android_SDK-35%2B-indigo?logo=google&color=6366F1)](https://developer.android.com)
[![Platform](https://img.shields.io/badge/Platform-Android-orange?logo=android)](https://developer.android.com)
[![Support Developer](https://img.shields.io/badge/Support-Buy_Me_A_Coffee-yellow?logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/charleshartmann)

**A sleek, privacy-focused Android QR and Barcode scanner built with a modern dark cyberpunk/slate design system, dynamic transitions, and persistent offline local history.**

[**View Live Landing Page**](https://chartmann1590.github.io/qrcode-scanner-android/) • [**Download APK**](https://github.com/chartmann1590/qrcode-scanner-android/releases/latest/download/app-release.apk) • [**Download AAB**](https://github.com/chartmann1590/qrcode-scanner-android/releases/latest/download/app-release.aab) • [**Privacy Policy**](https://chartmann1590.github.io/qrcode-scanner-android/#privacy) • [**Support Development**](https://buymeacoffee.com/charleshartmann)

---

<p align="center">
  <img src="docs/index.html" width="0" height="0" alt="" />
  <!-- SVG Mockups inline display using HTML -->
  <kbd>
    <img src="https://raw.githubusercontent.com/chartmann1590/qrcode-scanner-android/master/docs/index.html" width="220" style="display:none;" />
  </kbd>
</p>

</div>

## 📥 Download Release

Get the latest signed, ad-enabled release builds:
- 📱 [**Download Latest APK**](https://github.com/chartmann1590/qrcode-scanner-android/releases/latest/download/app-release.apk)
- 📦 [**Download Latest App Bundle (AAB)**](https://github.com/chartmann1590/qrcode-scanner-android/releases/latest/download/app-release.aab)
- 🔖 [**View All Releases & Version History**](https://github.com/chartmann1590/qrcode-scanner-android/releases)

## ✨ Features

- **🌌 Modern Cyberpunk/Slate Theme**
  Overhauled default Material design. Built on a rich dark palette utilizing slate tones (`#0F172A`/`#1E293B`) paired with glowing neon-indigo and cyan highlights.
- **📂 Secure Offline History**
  Tracks and logs all scanned contents locally inside a persistent, high-performance SQLite database. Data never leaves your device.
- **⚡ Instant Copy & Share**
  One-tap copy to clipboard and share action panels integrated directly on the result details card.
- **🧭 Intuitive Bottom Navigation**
  Fluidly switch between the **Scan** tab and the **History** tab using a sleek, customized `BottomNavigationView`.
- **🗑️ History Management**
  Quickly delete specific scanned records or clear your entire log using the "Clear All" sweep panel.
- **📣 Integrated Banner & Interstitial Ads**
  Preconfigured with Google AdMob SDK configurations for banners and interstitials.

---

## 🎨 Visual Design System

| Token | Hex Value | Application |
| :--- | :--- | :--- |
| `bg_slate_900` | `#0F172A` | Window background & toolbars |
| `bg_slate_800` | `#1E293B` | Result cards & bottom navigation |
| `bg_slate_700` | `#334155` | Borders, inputs & icon containers |
| `neon_indigo` | `#6366F1` | Primary branding, tabs & key buttons |
| `neon_cyan` | `#06B6D4` | Accent highlight, details & states |
| `neon_emerald` | `#10B981` | Success scanning indication |
| `neon_rose` | `#F43F5E` | Delete & clear data indications |

---

## 🏗️ Architecture & Database Schema

The local history is powered by a zero-dependency SQLite implementation (`SQLiteOpenHelper`) ensuring database actions compile instantly without Gradle sync complications.

```sql
CREATE TABLE history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    content TEXT,
    format TEXT,
    timestamp INTEGER
);
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Flamingo (2022.2.1) or higher
- Android SDK 35
- JDK 11 or 17

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/chartmann1590/qrcode-scanner-android.git
   ```
2. Open the project in **Android Studio**.
3. Configure your local signing key details in your `local.properties`:
   ```properties
   keystore.file=c\:/Users/Charles/Key.jks
   keystore.password=YourKeystorePassword
   keystore.alias=key0
   keystore.keyPassword=YourKeyPassword
   ```
4. Build and install on your connected device:
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🔒 Privacy Policy

We care about your privacy.
- **Data Collection:** We collect **zero** personal information.
- **Local Storage:** All scans are written to your local database on-device. Nothing is uploaded to external clouds or servers.
- **Permissions:** The camera permission is only requested for reading codes, and processing is done entirely in memory.
- **Analytics:** Minimal, anonymous usage logs are sent to Firebase to monitor crash logs and enhance performance.

---

## ☕ Support the Developer

If this application saves you time and improves your workflow, feel free to support further updates:

<a href="https://buymeacoffee.com/charleshartmann" target="_blank">
  <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" width="200" />
</a>

---

*Designed and developed by [Charles Hartmann](https://github.com/chartmann1590).*
