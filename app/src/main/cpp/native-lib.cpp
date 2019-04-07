#include <jni.h>
#include <string>
#include "JavaCallHelper.h"
#include "DNFFmpeg.h"
#include <android/native_window_jni.h>  //与 #include <android/native_window.h>的区别

//todo 等待释放
JavaCallHelper *javaCallHelper = 0;
JavaVM *javaVM = NULL;
DNFFmpeg *ffmpeg = 0;
pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;  //todo 待思考

ANativeWindow *window = 0;

/**
 * 图像渲染
 * @param data
 * @param linesize
 * @param w
 * @param h
 */
void renderFrame(uint8_t *data, int linesize, int w, int h) {
    pthread_mutex_lock(&mutex);
    if (!window) {
        pthread_mutex_unlock(&mutex);
        return;
    }
    ANativeWindow_setBuffersGeometry(window, w, h, WINDOW_FORMAT_RGBA_8888);
    ANativeWindow_Buffer window_buffer;
    if (ANativeWindow_lock(window, &window_buffer, 0)) {  //todo 待理解
        ANativeWindow_release(window);
        window = 0;
        pthread_mutex_unlock(&mutex);
        return;
    }
    uint8_t *dst_data = static_cast<uint8_t *>(window_buffer.bits);

    int dst_linesize = window_buffer.stride * 4;
    uint8_t *src_data = data;
    int src_linesize = linesize;
    for (int i = 0; i < window_buffer.height; ++i) {
        memcpy(dst_data + i * dst_linesize, src_data + i * src_linesize, dst_linesize);
    }
    ANativeWindow_unlockAndPost(window);
    pthread_mutex_unlock(&mutex);
}

//方法自动调用
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    javaVM = vm;
    return JNI_VERSION_1_4;
}


/**
 * 播放器准备
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_ap_player_APlayer_native_1prepare(JNIEnv *env, jobject instance,
                                                jstring dataSource_) {
    const char *dataSource = env->GetStringUTFChars(dataSource_, 0);
    javaCallHelper = new JavaCallHelper(javaVM, env, instance);
    LOGE("TANG-- javaCallHelper created");
    ffmpeg = new DNFFmpeg(javaCallHelper, dataSource);
    ffmpeg->setRenderCallback(renderFrame);
    ffmpeg->prepare();
    env->ReleaseStringUTFChars(dataSource_, dataSource);
}


/**
 * 播放
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_ap_player_APlayer_native_1start(JNIEnv *env, jobject instance) {
    if (ffmpeg) {
        ffmpeg->start();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ap_player_APlayer_native_1set_1surface(JNIEnv *env, jobject instance,
                                                     jobject surface) {
    pthread_mutex_lock(&mutex);
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    window = ANativeWindow_fromSurface(env, surface);
    pthread_mutex_unlock(&mutex);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ap_player_APlayer_native_1stop(JNIEnv *env, jobject instance) {
    // TODO
    if (ffmpeg) {
        ffmpeg->stop();
        ffmpeg = 0;
    }
    if (javaCallHelper) {
        delete javaCallHelper;
        javaCallHelper = 0;
    }

}extern "C"
JNIEXPORT void JNICALL
Java_com_ap_player_APlayer_native_1release(JNIEnv *env, jobject instance) {
    pthread_mutex_lock(&mutex);
    if (window) {
        ANativeWindow_release(window);
        window = 0;
    }
    pthread_mutex_unlock(&mutex);
}