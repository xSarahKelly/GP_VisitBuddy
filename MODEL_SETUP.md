# Automatic Model Setup Guide

The app now automatically loads the Whisper model from assets on first run. Here's how to set it up:

## Option 1: Bundle Model in App (Recommended)

### Step 1: Create Assets Folder
1. In Android Studio, right-click on `app/src/main`
2. Select **New → Folder → Assets Folder**
3. Click **Finish**

### Step 2: Add Model to Assets
1. Download a Whisper model:
   - **ggml-tiny.bin** (~75MB) - Fastest, lower accuracy
   - **ggml-base.bin** (~142MB) - Balanced
   - **ggml-small.bin** (~466MB) - Better accuracy, slower

2. Download from: https://huggingface.co/ggerganov/whisper.cpp/tree/main

3. Copy the model file to: `app/src/main/assets/`
   - Name it exactly: `ggml-tiny.bin`, `ggml-base.bin`, or `ggml-small.bin`

### Step 3: Build and Install
- The model will be automatically copied to internal storage on first app launch
- No user interaction needed!

## Option 2: Manual Installation (Fallback)

If you don't bundle the model, users can manually place it:

1. **Via ADB:**
   ```bash
   adb push ggml-tiny.bin /sdcard/Download/
   ```
   The app will auto-detect it.

2. **Via File Manager:**
   - Place model in `/sdcard/Download/`
   - App will find it automatically

## How It Works

1. **First Launch:**
   - App checks internal storage for model
   - If not found, checks assets folder
   - If found in assets, copies to internal storage
   - Loads model automatically

2. **Subsequent Launches:**
   - App loads model from internal storage (faster)
   - No copying needed

## Model Priority

The app tries models in this order:
1. `ggml-tiny.bin` (smallest, fastest)
2. `ggml-base.bin` (balanced)
3. `ggml-small.bin` (best accuracy)

## File Size Considerations

- **ggml-tiny.bin**: ~75MB - Good for testing
- **ggml-base.bin**: ~142MB - Recommended for production
- **ggml-small.bin**: ~466MB - Best accuracy, larger APK

**Note:** Bundling the model increases APK size. Consider using App Bundle (AAB) format for Play Store to reduce download size.

## Important: Before Building APK

**⚠️ CRITICAL:** The model file must be in `app/src/main/assets/` when you build the APK. 

- If you build on a different machine or from a clean git clone, the model won't be in the APK (it's gitignored)
- Users will then see a download prompt
- **Solution:** Always ensure the model file is in assets before building

### Quick Check Before Building:
```bash
# Verify model is present
ls app/src/main/assets/ggml-tiny.bin
```

If missing, download and place it before building.

## Testing

After adding model to assets:
1. Clean and rebuild project (`Build → Clean Project`, then `Build → Rebuild Project`)
2. Install on device
3. App should automatically load model on first launch
4. No user interaction required!

## For Distribution

If distributing the APK:
- **Option A:** Include model in assets (APK will be ~75MB+ larger)
- **Option B:** Don't include model, users download it manually (smaller APK)

