//
// Created by tang on 2018/12/13.
//

#ifndef MYPLAYER_JAVACALLHELPER_H
#define MYPLAYER_JAVACALLHELPER_H


#include <jni.h>

class JavaCallHelper {
public:
    JavaCallHelper(JavaVM *_javaVM, JNIEnv *_env, jobject &jobj);

    ~JavaCallHelper();

    void onError(int thread, int code);

    void onParpare(int thread);

    void onProgress(int thread, int progress);

public:
    JavaVM *javaVM;
    JNIEnv *env;
    jobject jobj;
    jmethodID jmid_error;
    jmethodID jmid_prepare;
    jmethodID jmid_progress;

};

#endif //MYPLAYER_JAVACALLHELPER_H
