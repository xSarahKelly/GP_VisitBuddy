# Medical Appointment Companion

A privacy-focused Android app for recording and transcribing medical appointments using local speech-to-text processing with whisper.cpp.

## Features

- **Local-Only Processing**: All transcription happens on-device. No cloud services, no APIs.
- **Speech-to-Text**: Uses whisper.cpp for accurate medical conversation transcription.
- **Medical Information Extraction**: Automatically extracts medications, diagnoses, instructions, and follow-ups.
- **Secure Storage**: All data stored locally on the device.
- **Native Performance**: C++ native layer for efficient model inference.

## Project Structure

```
app/src/main/
├── java/com/example/medicalappointmentcompanion/
│   ├── MainActivity.kt           # Main entry point
│   ├── ui/                       # UI layer (Compose)
│   │   ├── MainScreen.kt         # Main UI composable
│   │   ├── MainViewModel.kt      # ViewModel for state management
│   │   └── theme/                # Material theme
│   ├── audio/                    # Audio recording layer
│   │   ├── AudioRecorder.kt      # 16kHz audio capture
│   │   └── WaveHelper.kt         # WAV file handling
│   ├── whisper/                  # Whisper integration layer
│   │   ├── WhisperLib.kt         # JNI bindings
│   │   ├── WhisperContext.kt     # High-level API
│   │   └── WhisperCpuConfig.kt   # CPU optimization
│   ├── model/                    # Data models
│   │   ├── Appointment.kt        # Appointment data classes
│   │   └── AppState.kt           # UI state
│   ├── storage/                  # Local storage
│   │   └── LocalStorage.kt       # JSON-based persistence
│   └── extraction/               # Medical info extraction
│       └── MedicalExtractor.kt   # Pattern-based extraction
└── cpp/                          # Native C++ layer
    ├── CMakeLists.txt            # CMake build config
    ├── native_bridge/            # JNI bridge
    │   └── whisper_jni.cpp       # JNI implementation
    └── whisper/                  # Whisper extensions
        └── whisper_wrapper.h     # Project-specific headers
```

## Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Android NDK 26.1.10909125 or compatible
- CMake 3.22.1+
- Min SDK: 26 (Android 8.0)
- Target SDK: 35

## Setup

### 1. Clone whisper.cpp

The project expects whisper.cpp to be in the project root:

```bash
cd MedicalAppointmentCompanion
git clone https://github.com/ggerganov/whisper.cpp.git
```

### 2. Download a Whisper Model

Download a GGML model file. Recommended for mobile:

```bash
# Tiny model (~75MB) - Fast, lower accuracy
cd whisper.cpp/models
./download-ggml-model.sh tiny

# Base model (~142MB) - Good balance
./download-ggml-model.sh base

# Small model (~466MB) - Better accuracy, slower
./download-ggml-model.sh small
```

### 3. Place the Model

Copy the model to the device's app data directory:
- `/data/data/com.example.medicalappointmentcompanion/files/models/`
- Or use `adb push` to copy the model file

### 4. Build the Project

1. Open the project in Android Studio
2. Sync Gradle files
3. Build > Make Project
4. Run on a device/emulator

## Usage

1. **Load Model**: Tap the model status indicator and enter the path to your .bin model file
2. **Grant Permissions**: Allow microphone access when prompted
3. **Record**: Tap the microphone button to start recording
4. **Stop**: Tap the stop button to finish and transcribe
5. **Review**: View the transcription and extracted medical information

## Architecture

### Layers

1. **UI Layer** (Kotlin/Compose)
   - Material 3 dark theme
   - State management via ViewModel
   - Reactive UI with StateFlow

2. **Audio Layer** (Kotlin)
   - AudioRecord API at 16kHz
   - WAV file encoding/decoding
   - Float array conversion for whisper

3. **Whisper Layer** (Kotlin + JNI)
   - Thread-safe context management
   - Coroutine-based async API
   - Architecture-specific optimizations

4. **Native Layer** (C++)
   - JNI bridge to whisper.cpp
   - ARM NEON optimizations
   - FP16 support on compatible devices

5. **Storage Layer** (Kotlin)
   - JSON serialization
   - File-based persistence
   - No external database dependencies

### Data Flow

```
Audio Input → AudioRecorder → FloatArray → WhisperContext → Transcription
                                                              ↓
                                                       MedicalExtractor
                                                              ↓
                                                       MedicalInfo
                                                              ↓
                                                       LocalStorage
```

## Model Compatibility

The app automatically selects the optimal library variant:

| Architecture | Library | Optimizations |
|-------------|---------|---------------|
| arm64-v8a | whisper_v8fp16_va | ARMv8.2-A FP16 |
| armeabi-v7a | whisper_vfpv4 | NEON VFPv4 |
| x86_64/x86 | whisper | Standard |

## Privacy

- **No Network**: App requires no internet permission
- **Local Storage**: All data stays on device
- **No Analytics**: No tracking or telemetry
- **No Cloud**: No external API calls

## Technical Notes

### NDK Configuration

The project uses:
- C++17 standard
- STL: c++_shared
- CMake 3.22.1

### Performance Tips

1. Use the smallest model that meets your accuracy needs
2. Keep recordings under 5 minutes for best performance
3. Ensure good microphone positioning
4. Use a device with ARM64 for best performance

### Troubleshooting

**Model won't load:**
- Check the file path is correct
- Ensure the model file is not corrupted
- Verify sufficient device storage

**Recording fails:**
- Check microphone permission is granted
- Ensure no other app is using the microphone
- Try restarting the app

**Slow transcription:**
- Use a smaller model (tiny or base)
- Close background apps
- Ensure device is not in battery saver mode

## License

This project uses whisper.cpp which is licensed under MIT.

