# Whisper Model Assets

## How to Add the Model

1. **Download a Whisper model:**
   - Go to: https://huggingface.co/ggerganov/whisper.cpp/tree/main
   - Download one of these files:
     - `ggml-small.en.bin` (~466MB) - English-only, better for Irish accent
     - `ggml-base.en.bin` (~142MB) - English-only, lighter option
     - `ggml-tiny.bin` (~75MB) - Multilingual, faster
     - `ggml-base.bin` (~142MB) - Multilingual

2. **Place the file here:**
   - Copy the downloaded `.bin` file to this folder (`app/src/main/assets/`)
   - Name it exactly: `ggml-small.en.bin`, `ggml-base.en.bin`, `ggml-tiny.bin`, or `ggml-base.bin`

3. **Rebuild the app:**
   - The model will be automatically copied to internal storage on first launch
   - No user interaction needed!

## Model Priority

The app will try to load models in this order:
1. `ggml-small.en.bin` (English-only, better for Irish)
2. `ggml-base.en.bin` (English-only, lighter)
3. `ggml-tiny.bin` (smallest, fastest, multilingual)
4. `ggml-base.bin` (balanced, multilingual)

## File Size Note

Bundling the model increases APK size. `ggml-small.en.bin` is ~466MB.
- Or using App Bundle (AAB) format for Play Store

