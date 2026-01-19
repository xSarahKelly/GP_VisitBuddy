# Troubleshooting: "[BLANK_AUDIO]" Issue

## Problem
Seeing "[BLANK_AUDIO] [BLANK_AUDIO]" in the transcription instead of actual text.

## Causes

### 1. Microphone Permission Not Granted
**Check:**
- Go to your phone's Settings → Apps → GP VisitBuddy → Permissions
- Make sure "Microphone" permission is **Allowed**
- If denied, grant it and try again

**In the app:**
- The app should request permission automatically on first launch
- If denied, need to grant it manually in Settings

### 2. Microphone Not Working
**Test:**
- Try recording with another app (voice recorder, camera video)
- If other apps also fail, it's a device issue, not the app

### 3. Audio Too Quiet/Silent
**Symptoms:**
- Recording appears to work (timer counts)
- But transcription is blank
- This means audio was recorded but it's silent/too quiet

**Check Logcat:**
Look for these log messages:
```
Audio diagnostics:
  - Avg absolute: [value]
  - Non-zero samples: [percentage]
WARNING: Audio appears to be silent or very quiet!
```

**If you see the warning:**
- Speak louder and closer to the microphone
- Record in a quiet environment
- Check microphone isn't blocked

### 4. Recording Too Short
**Issue:**
- If you stop recording immediately, there might not be enough audio
- Whisper needs at least 1-2 seconds of audio

**Fix:**
- Record for at least 5-10 seconds
- Speak clearly throughout the recording

### 5. Whisper Model Not Loaded
**Check:**
- Does the app show "Model loaded" or similar?
- If model isn't loaded, transcription will fail

**Fix:**
- Make sure Whisper model file is in the correct location
- Check app logs for model loading errors

## How to Debug

### Step 1: Check Logcat Output

In Android Studio:
1. Connect your device
2. Open Logcat (View → Tool Windows → Logcat)
3. Filter by: `AudioRecorder` or `MainViewModel`
4. Start a recording
5. Look for these messages:

**Good signs:**
```
Recording started at 16000Hz
Recording... 1s, max amp: [number > 1000]
Recording finished. Max amplitude: [number > 1000]
Audio diagnostics:
  - Avg absolute: [number > 0.01]
  - Non-zero samples: [percentage > 10%]
```

**Bad signs:**
```
WARNING: Audio appears to be silent or very quiet!
Max amplitude: 0
Avg absolute: 0.0
Non-zero samples: 0%
```