# ArmKiller

**ArmKiller** is a powerful Android application designed to remove and dump ARM protection/obfuscation from Android APK files. This tool helps reverse engineers and security researchers analyze apps that use ARM-based protection schemes.

---

## Features

- **ARM Protection Removal**: Automatically detects and removes ARM-based app protection mechanisms
- **Config.so Support**: Handles apps with and without `config.so` files
- **Modern UI**: Clean Material Design interface with card-based layout
- **File Picker**: Built-in file picker for easy APK selection
- **Real-time Logging**: Live log output showing processing progress
- **Automatic Signing**: Automatically signs the processed APK
- **No Root Required**: Works without root access on your device

---

## What This App Does

ArmKiller is designed to **remove/dump ARM protection** from Android applications. Many app developers use ARM-based protection schemes to:

- Obfuscate the `AndroidManifest.xml`
- Encrypt DEX files
- Hide the real application entry point
- Prevent reverse engineering

This tool:
1. Extracts DEX files and assets from the protected APK
2. Decodes encrypted/obfuscated DEX files
3. Fixes the `AndroidManifest.xml` to point to the real application class
4. Rebuilds and signs a new APK without the ARM protection

**Output**: For an input APK at `/path/to/app.apk`, the tool creates `/path/to/app.kill.apk`

---

## Prerequisites

### 1. Install Termux

Download and install **Termux** from:
- [F-Droid](https://f-droid.org/en/packages/com.termux/) (Recommended)
- [GitHub Releases](https://github.com/termux/termux-app/releases)

### 2. Configure Repository (Required for Android SDK)

```bash
# Update packages
pkg update && pkg upgrade

# Install Android SDK
apt install android-sdk
```

### 3. Grant Storage Permissions

```bash
termux-setup-storage
```

---

## Building the App

### Clone the Repository

```bash
git clone https://github.com/Anon4You/ArmKiller-app.git
cd ArmKiller-app
```

### Build Debug APK

```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Or using bash
bash gradlew assembleDebug
```

### Output Location

The built APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Usage

1. **Install the APK** on your Android device
2. **Grant Storage Permissions** when prompted
3. **Grant "All Files Access"** permission (required for Android 11+)
4. **Tap "Browse"** to select the protected APK file
5. **Select the processing mode**:
   - **With Config.so**: For apps that use config.so for protection
   - **Without Config.so**: For standard ARM protection
6. **Tap "Start Processing"**
7. **Wait for completion** - logs will show the progress
8. **Find the output APK** at the path shown in logs (e.g., `/path/to/app.kill.apk`)

---

## Screenshots

| Feature | Description |
|---------|-------------|
| Modern UI | Clean Material Design with card-based layout |
| File Picker | Easy APK selection with built-in file browser |
| Log Box | Real-time processing logs and output path |

---

## Technical Details

### How It Works

1. **Extraction**: Extracts DEX files, assets, and AndroidManifest.xml from the input APK
2. **Decoding**: XOR decodes encrypted DEX files (0xFF XOR operation)
3. **Manifest Fix**: Reads config.so (if present) to find the real application class and updates AndroidManifest.xml
4. **Rebuilding**: Creates a new APK with the decoded files and fixed manifest
5. **Signing**: Signs the output APK using the built-in signer

### Supported Protection Schemes

- ARM-based stub applications
- XOR encrypted DEX files
- Manifest application class obfuscation
- Config.so based protection

---

## Requirements

- **Android Version**: Android 6.0 (API 23) or higher
- **Permissions**: 
  - Storage (READ/WRITE_EXTERNAL_STORAGE)
  - All Files Access (Android 11+)

---

## Disclaimer

This tool is intended for **educational purposes**, **security research**, and **legitimate reverse engineering** only. 

- Use only on applications you own or have explicit permission to analyze
- Do not use for piracy or copyright infringement
- The developers are not responsible for any misuse of this software

---

## Issues

If you encounter any bugs or have feature requests, please open an issue on the [GitHub Issues](https://github.com/Anon4You/ArmKiller-app/issues) page.

---

## Support

For support, questions, or suggestions:
- Open an issue on GitHub
- Contact: [Anon4You](https://github.com/Anon4You)

---

**Made with ❤️ for the Android security community**
