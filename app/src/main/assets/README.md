# Whisper Model Assets

## How to Add the Model

1. **Download a Whisper model:**
   - Go to: https://huggingface.co/ggerganov/whisper.cpp/tree/main
   - Download one of these files:
     - `ggml-tiny.bin` (~75MB) - Recommended for testing
     - `ggml-base.bin` (~142MB) - Better accuracy
     - `ggml-small.bin` (~466MB) - Best accuracy

2. **Place the file here:**
   - Copy the downloaded `.bin` file to this folder (`app/src/main/assets/`)
   - Name it exactly: `ggml-tiny.bin`, `ggml-base.bin`, or `ggml-small.bin`

3. **Rebuild the app:**
   - The model will be automatically copied to internal storage on first launch
   - No user interaction needed!

## Model Priority

The app will try to load models in this order:
1. `ggml-tiny.bin` (smallest, fastest)
2. `ggml-base.bin` (balanced)
3. `ggml-small.bin` (best accuracy)

## File Size Note

Bundling the model increases APK size. For production, consider:
- Using `ggml-tiny.bin` for smaller APK
- Or using App Bundle (AAB) format for Play Store

