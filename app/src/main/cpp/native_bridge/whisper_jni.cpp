/**
 * Medical Appointment Companion - Whisper JNI Bridge
 * 
 * Native C++ layer for whisper.cpp integration with Android.
 * Handles model initialization, transcription, and resource management.
 */

#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include <cstdlib>
#include <cstring>
#include <sys/sysinfo.h>
#include "whisper.h"
#include "ggml.h"

#define UNUSED(x) (void)(x)
#define TAG "WhisperJNI"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// ============================================================================
// Input Stream Context for loading models from Java streams
// ============================================================================

struct input_stream_context {
    size_t offset;
    JNIEnv *env;
    jobject thiz;
    jobject input_stream;
    jmethodID mid_available;
    jmethodID mid_read;
};

static size_t input_stream_read(void *ctx, void *output, size_t read_size) {
    struct input_stream_context *is = (struct input_stream_context *)ctx;
    
    jint avail_size = is->env->CallIntMethod(is->input_stream, is->mid_available);
    jint size_to_copy = read_size < (size_t)avail_size ? (jint)read_size : avail_size;
    
    jbyteArray byte_array = is->env->NewByteArray(size_to_copy);
    jint n_read = is->env->CallIntMethod(is->input_stream, is->mid_read, byte_array, 0, size_to_copy);
    
    if (size_to_copy != (jint)read_size || size_to_copy != n_read) {
        LOGI("Partial read: Requested=%zu, Copied=%d, Read=%d", read_size, size_to_copy, n_read);
    }
    
    jbyte *byte_array_elements = is->env->GetByteArrayElements(byte_array, nullptr);
    memcpy(output, byte_array_elements, size_to_copy);
    is->env->ReleaseByteArrayElements(byte_array, byte_array_elements, JNI_ABORT);
    is->env->DeleteLocalRef(byte_array);
    
    is->offset += size_to_copy;
    return size_to_copy;
}

static bool input_stream_eof(void *ctx) {
    struct input_stream_context *is = (struct input_stream_context *)ctx;
    jint result = is->env->CallIntMethod(is->input_stream, is->mid_available);
    return result <= 0;
}

static void input_stream_close(void *ctx) {
    UNUSED(ctx);
}

// ============================================================================
// Asset Manager helpers for loading models from APK assets
// ============================================================================

static size_t asset_read(void *ctx, void *output, size_t read_size) {
    return AAsset_read((AAsset *)ctx, output, read_size);
}

static bool asset_is_eof(void *ctx) {
    return AAsset_getRemainingLength64((AAsset *)ctx) <= 0;
}

static void asset_close(void *ctx) {
    AAsset_close((AAsset *)ctx);
}

static struct whisper_context *whisper_init_from_asset(
        JNIEnv *env,
        jobject assetManager,
        const char *asset_path) {
    LOGI("Loading model from asset: %s", asset_path);
    
    AAssetManager *asset_manager = AAssetManager_fromJava(env, assetManager);
    if (!asset_manager) {
        LOGE("Failed to get AssetManager from Java");
        return nullptr;
    }
    
    AAsset *asset = AAssetManager_open(asset_manager, asset_path, AASSET_MODE_STREAMING);
    if (!asset) {
        LOGW("Failed to open asset: %s", asset_path);
        return nullptr;
    }
    
    whisper_model_loader loader = {
        .context = asset,
        .read = &asset_read,
        .eof = &asset_is_eof,
        .close = &asset_close
    };
    
    return whisper_init_with_params(&loader, whisper_context_default_params());
}

// ============================================================================
// JNI Functions - Context Management
// ============================================================================

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_initContextFromInputStream(
        JNIEnv *env, jobject thiz, jobject input_stream) {
    UNUSED(thiz);
    
    struct whisper_context *context = nullptr;
    struct whisper_model_loader loader = {};
    struct input_stream_context inp_ctx = {};
    
    inp_ctx.offset = 0;
    inp_ctx.env = env;
    inp_ctx.thiz = thiz;
    inp_ctx.input_stream = input_stream;
    
    jclass cls = env->GetObjectClass(input_stream);
    inp_ctx.mid_available = env->GetMethodID(cls, "available", "()I");
    inp_ctx.mid_read = env->GetMethodID(cls, "read", "([BII)I");
    
    loader.context = &inp_ctx;
    loader.read = input_stream_read;
    loader.eof = input_stream_eof;
    loader.close = input_stream_close;
    
    loader.eof(loader.context);
    
    context = whisper_init(&loader);
    return (jlong)context;
}

JNIEXPORT jlong JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_initContextFromAsset(
        JNIEnv *env, jobject thiz, jobject assetManager, jstring asset_path_str) {
    UNUSED(thiz);
    
    const char *asset_path_chars = env->GetStringUTFChars(asset_path_str, nullptr);
    struct whisper_context *context = whisper_init_from_asset(env, assetManager, asset_path_chars);
    env->ReleaseStringUTFChars(asset_path_str, asset_path_chars);
    
    return (jlong)context;
}

JNIEXPORT jlong JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_initContext(
        JNIEnv *env, jobject thiz, jstring model_path_str) {
    UNUSED(thiz);
    
    const char *model_path_chars = env->GetStringUTFChars(model_path_str, nullptr);
    LOGI("Loading model from file: %s", model_path_chars);
    
    struct whisper_context *context = whisper_init_from_file_with_params(
        model_path_chars, 
        whisper_context_default_params()
    );
    
    env->ReleaseStringUTFChars(model_path_str, model_path_chars);
    return (jlong)context;
}

JNIEXPORT void JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_freeContext(
        JNIEnv *env, jobject thiz, jlong context_ptr) {
    UNUSED(env);
    UNUSED(thiz);
    
    struct whisper_context *context = (struct whisper_context *)context_ptr;
    if (context) {
        LOGI("Freeing whisper context");
        whisper_free(context);
    }
}

// ============================================================================
// JNI Functions - Transcription
// ============================================================================

JNIEXPORT void JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_fullTranscribe(
        JNIEnv *env, jobject thiz, jlong context_ptr, jint num_threads, jfloatArray audio_data) {
    UNUSED(thiz);
    
    struct whisper_context *context = (struct whisper_context *)context_ptr;
    jfloat *audio_data_arr = env->GetFloatArrayElements(audio_data, nullptr);
    const jsize audio_data_length = env->GetArrayLength(audio_data);
    
    // Diagnostic: analyze audio data
    float min_val = 0, max_val = 0, sum_abs = 0;
    int non_zero = 0;
    for (int i = 0; i < audio_data_length; i++) {
        float v = audio_data_arr[i];
        if (v < min_val) min_val = v;
        if (v > max_val) max_val = v;
        sum_abs += (v >= 0 ? v : -v);
        if (v != 0) non_zero++;
    }
    float avg_abs = sum_abs / audio_data_length;
    
    LOGI("Audio data analysis:");
    LOGI("  Samples: %d (%.2fs)", audio_data_length, audio_data_length / 16000.0f);
    LOGI("  Range: [%.6f, %.6f]", min_val, max_val);
    LOGI("  Avg absolute: %.6f", avg_abs);
    LOGI("  Non-zero: %d (%.1f%%)", non_zero, (non_zero * 100.0f / audio_data_length));
    
    if (avg_abs < 0.001f) {
        LOGW("Audio appears silent! avg_abs=%.6f", avg_abs);
    }
    
    // Configure transcription parameters for medical conversations
    struct whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime = false;
    params.print_progress = false;
    params.print_timestamps = true;
    params.print_special = false;
    params.translate = false;
    params.language = "en";
    params.n_threads = num_threads;
    params.offset_ms = 0;
    params.no_context = true;
    params.single_segment = false;
    
    // Tune for potentially quiet audio
    params.entropy_thold = 2.8f;        // Increase from default 2.4 (less strict)
    params.logprob_thold = -1.5f;       // Increase from default -1.0 (less strict)
    params.no_speech_thold = 0.3f;      // Decrease from default 0.6 (more sensitive)
    
    whisper_reset_timings(context);
    
    LOGI("Starting transcription with %d threads", num_threads);
    
    if (whisper_full(context, params, audio_data_arr, audio_data_length) != 0) {
        LOGE("Failed to run transcription");
    } else {
        int n_segments = whisper_full_n_segments(context);
        LOGI("Transcription complete: %d segments", n_segments);
        for (int i = 0; i < n_segments && i < 5; i++) {
            const char* text = whisper_full_get_segment_text(context, i);
            LOGI("  Segment %d: %s", i, text);
        }
        whisper_print_timings(context);
    }
    
    env->ReleaseFloatArrayElements(audio_data, audio_data_arr, JNI_ABORT);
}

// ============================================================================
// JNI Functions - Result Retrieval
// ============================================================================

JNIEXPORT jint JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_getTextSegmentCount(
        JNIEnv *env, jobject thiz, jlong context_ptr) {
    UNUSED(env);
    UNUSED(thiz);
    
    struct whisper_context *context = (struct whisper_context *)context_ptr;
    return whisper_full_n_segments(context);
}

JNIEXPORT jstring JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_getTextSegment(
        JNIEnv *env, jobject thiz, jlong context_ptr, jint index) {
    UNUSED(thiz);
    
    struct whisper_context *context = (struct whisper_context *)context_ptr;
    const char *text = whisper_full_get_segment_text(context, index);
    return env->NewStringUTF(text);
}

JNIEXPORT jlong JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_getTextSegmentT0(
        JNIEnv *env, jobject thiz, jlong context_ptr, jint index) {
    UNUSED(env);
    UNUSED(thiz);
    
    struct whisper_context *context = (struct whisper_context *)context_ptr;
    return whisper_full_get_segment_t0(context, index);
}

JNIEXPORT jlong JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_getTextSegmentT1(
        JNIEnv *env, jobject thiz, jlong context_ptr, jint index) {
    UNUSED(env);
    UNUSED(thiz);
    
    struct whisper_context *context = (struct whisper_context *)context_ptr;
    return whisper_full_get_segment_t1(context, index);
}

// ============================================================================
// JNI Functions - System Info & Benchmarks
// ============================================================================

JNIEXPORT jstring JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_getSystemInfo(
        JNIEnv *env, jobject thiz) {
    UNUSED(thiz);
    
    const char *sysinfo = whisper_print_system_info();
    return env->NewStringUTF(sysinfo);
}

JNIEXPORT jstring JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_benchMemcpy(
        JNIEnv *env, jobject thiz, jint n_threads) {
    UNUSED(thiz);
    
    const char *bench_result = whisper_bench_memcpy_str(n_threads);
    return env->NewStringUTF(bench_result);
}

JNIEXPORT jstring JNICALL
Java_com_example_medicalappointmentcompanion_whisper_WhisperLib_00024Companion_benchGgmlMulMat(
        JNIEnv *env, jobject thiz, jint n_threads) {
    UNUSED(thiz);
    
    const char *bench_result = whisper_bench_ggml_mul_mat_str(n_threads);
    return env->NewStringUTF(bench_result);
}

} // extern "C"
