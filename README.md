# GP VisitBuddy

An edge AI Android app for recording and transcribing GP consultations. All processing happens on-device, no cloud, no APIs. Designed to improve patient recall of medical instructions while keeping data private.

## Features

- **Local-only processing**: Transcription and extraction run entirely on the device. No internet required after setup.
- **On-device speech-to-text**: Uses whisper.cpp for accurate transcription of doctor-patient conversations.
- **Schema-guided extraction**: Pulls out medications, tests, referrals, follow-up steps, and safety advice, aligned with the Calgary-Cambridge consultation model. Only extracts what is explicitly stated (no diagnoses).
- **Encrypted storage**: Auth data and appointments are encrypted (EncryptedSharedPreferences, EncryptedFile). GDPR-aligned.
- **Multi-account**: Patient and Carer accounts. Carers can add patient accounts and switch between them (with password verification).
- **GP consent flow**: Consent dialog before recording to address clinician concerns.
- **Accessibility**: Date picker for DOB, contrast, typography, and labelled buttons.
- **Offline-first**: No network permission for core use. Model can be bundled in the APK or placed in Downloads.

## Project Structure

```
app/src/main/
├── java/com/example/medicalappointmentcompanion/
│   ├── MainActivity.kt
│   ├── auth/
│   │   └── AuthRepository.kt          # Multi-account auth, EncryptedSharedPreferences
│   ├── ui/
│   │   ├── MainScreen.kt              # Navigation, auth flow
│   │   ├── MainViewModel.kt            # State, recording, transcription
│   │   ├── HomeScreen.kt               # Home, account, footer
│   │   ├── AuthScreens.kt             # Login, sign up
│   │   ├── RecordingScreen.kt
│   │   ├── ProcessingScreen.kt
│   │   ├── ReviewScreen.kt             # Edit transcript before save
│   │   ├── SummaryScreen.kt            # Extracted instructions
│   │   ├── PastSummariesScreen.kt
│   │   ├── DatePickerField.kt
│   │   ├── ModelSetupDialog.kt
│   │   ├── SettingsDialog.kt
│   │   └── theme/
│   ├── audio/
│   │   ├── AudioRecorder.kt
│   │   └── WaveHelper.kt
│   ├── whisper/
│   │   ├── WhisperLib.kt
│   │   ├── WhisperContext.kt
│   │   └── WhisperCpuConfig.kt
│   ├── model/
│   │   ├── Appointment.kt
│   │   ├── MedicalSchema.kt
│   │   ├── UserSession.kt
│   │   └── AppState.kt
│   ├── storage/
│   │   ├── LocalStorage.kt            # EncryptedFile for appointments
│   │   └── ExtractionStorage.kt
│   └── extraction/
│       ├── SchemaGuidedExtractor.kt
│       └── WordVariations.kt          # ASR error correction (aliases, normalisation)
└── cpp/
    ├── CMakeLists.txt
    ├── native_bridge/whisper_jni.cpp
    └── whisper/
```

## Requirements

- Android Studio (recent version)
- Android NDK (25.x or 26.x recommended)
- CMake 3.22.1+
- Min SDK: 26 (Android 8.0)
- Target SDK: 36

## Setup

### 1. Clone whisper.cpp

The native layer expects whisper.cpp at the project root:

```bash
cd GP_VisitBuddy
git clone https://github.com/ggerganov/whisper.cpp.git
```

### 2. Add a Whisper model

The app looks for a model in this order: app files dir → assets → `/sdcard/Download/`.

**Option A: Bundle in APK (recommended)**  
Place a model file in `app/src/main/assets/`:

- `ggml-small.en.bin` (~466 MB), better accuracy
- `ggml-base.en.bin` (~142 MB), good balance
- `ggml-tiny.bin`, faster, lower accuracy

Download from [whisper.cpp models](https://huggingface.co/ggerganov/whisper.cpp/tree/main) or:

```bash
cd whisper.cpp/models
./download-ggml-model.sh small   # or base, tiny
# Copy ggml-small.en.bin to app/src/main/assets/
```

**Option B: Manual placement**  
Put the model in the device’s Download folder (e.g. `/sdcard/Download/ggml-small.en.bin`). The app will detect it on startup.

### 3. Build and run

1. Open the project in Android Studio
2. Sync Gradle
3. Build > Make Project
4. Run on a device or emulator

## Usage

1. **Sign up / log in**: Create a Patient or Carer account (local only).
2. **Account setup**: Enter display name, DOB, and current medications.
3. **Model**: On first run, the app loads the model from assets or files. If missing, it shows setup instructions.
4. **Permissions**: Grant microphone access when prompted.
5. **Record**: Tap “Start Recording” → confirm GP consent → record → stop.
6. **Review**: Edit the transcript if needed, then save.
7. **Summary**: View extracted medications, tests, follow-up, and safety advice.
8. **Past summaries**: Access previous appointment summaries from the home screen.

Carers can add patient accounts and switch between them (password required) to record and view summaries for each person.

## Architecture

### Layers

1. **UI** (Jetpack Compose): Accessibility-focused.
2. **ViewModel**: StateFlow, coroutines for async work.
3. **Auth**: EncryptedSharedPreferences, multi-account, Patient/Carer.
4. **Audio**: AudioRecord at 16 kHz, WAV handling.
5. **Whisper**: JNI to whisper.cpp, coroutine-based API.
6. **Extraction**: SchemaGuidedExtractor + WordVariations (phrase normalisation, medication aliases).
7. **Storage**: EncryptedFile for appointments, migration from legacy plain JSON.

### Data flow

```
Audio → AudioRecorder → WAV → WhisperContext → Transcription
                                                    ↓
                                          SchemaGuidedExtractor
                                                    ↓
                                          MedicalExtraction
                                                    ↓
                                          LocalStorage (encrypted)
```

## Privacy

- **No cloud**: No network permission for core features.
- **Encrypted storage**: Auth and appointments encrypted at rest.
- **Local only**: Audio and transcripts never leave the device.
- **No analytics**: No tracking or telemetry.
- **Backup**: Encrypted data excluded from Android backup (backup_rules.xml).

## Troubleshooting

**Model not found**  

- Ensure `ggml-small.en.bin` (or base/tiny) is in `app/src/main/assets/` before building.  
- Or place the model in `/sdcard/Download/` on the device.

**Recording fails**  

- Check microphone permission.  
- Ensure no other app is using the microphone.

**Slow transcription**  

- Use a smaller model (base or tiny).  
- Keep recordings under ~5 minutes.  
- Close other apps; avoid battery saver.

## License

Uses [whisper.cpp](https://github.com/ggerganov/whisper.cpp) (MIT).