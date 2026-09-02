# WO Pro — Work Order Management App

Modern, secure Android app for facility work order management, energy monitoring, and safety audits.

## Features

| Module | Description |
|--------|-------------|
| **Auth** | Email/password with salted SHA-256, biometric unlock, OTP verification |
| **Dashboard** | Stats cards (open, in-progress, completed, overdue), weekly bar chart, recent work orders |
| **Work Orders** | Create, edit, filter by status, detail view, status tracking |
| **Projects** | Capital projects list, create/edit, detail view with budget tracking |
| **Audit** | 10-item safety checklist (Pass/Fail/N/A), report creation and history |
| **Utility Meters** | Chiller, Freezer, Heat Pump, Water Tank, Fuel, Gas, KWH readings with tariff |
| **AI Chat** | Demo assistant for facility management questions |
| **Settings** | Security status, biometric, logout, app info |

## Security

- **Local DB**: SQLCipher AES-256 encrypted Room database (passphrase generated per device, stored in Android Keystore via EncryptedSharedPreferences)
- **Passwords**: Salted SHA-256 hash (never plaintext)
- **Network**: HTTPS enforced via `network_security_config.xml` (cleartext blocked)
- **Secrets**: All tokens/preferences in EncryptedSharedPreferences (AES/GCM)
- **Backup**: `allowBackup=false` — app data cannot be pulled via ADB

## How to Build

### Prerequisites
- Android Studio Hedgehog+ (2023.1+) or command-line Gradle
- JDK 17
- Gradle 8.9 (wrapper included)

### Local Build
```bash
./gradlew assembleRelease
```
APK output: `app/build/outputs/apk/release/app-release.apk`

### GitHub Actions (recommended)
1. Push to GitHub
2. The workflow in `.github/workflows/build-apk.yml` automatically builds
3. Download the APK artifact from the Actions tab

### Signing (optional)
Set these repository secrets for a signed release:
- `KS_BASE64` — base64-encoded keystore file
- `KS_PASS` — keystore password
- `KS_ALIAS` — key alias
- `KS_ALIAS_PASS` — key password

Without secrets, the APK is signed with the debug key (still installable).

## Architecture

```
MVVM + Room + Manual DI
├── data/
│   ├── local/     Entities, DAOs, SQLCipher database
│   ├── remote/    Retrofit + OkHttp (ready for real backend)
│   └── repository Single source of truth
├── security/      EncryptionManager, BiometricHelper
└── ui/            Compose screens, ViewModels, Theme
```

## Demo Mode

The app ships in **demo mode** — all data is stored locally in the encrypted Room database. No external backend is needed. To connect to a real API:

1. Edit `API_BASE_URL` in `app/build.gradle.kts` (buildConfigField)
2. Implement the server-side API matching `WOService.kt`
3. Set `DEMO_MODE = false` and wire up Retrofit calls in the repository

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| DI | Manual (AppContainer) |
| Database | Room + SQLCipher AES-256 |
| Network | Retrofit 2 + OkHttp 4 |
| Security | Android Keystore, EncryptedSharedPreferences, Biometric |
| Build | AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9 |
| Target | Android 24+ (minSdk=24, targetSdk=35) |