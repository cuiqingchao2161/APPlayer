//
// Created by tang on 2018/12/13.
//

#ifndef MYPLAYER_AUDIOCHANNEL_H
#define MYPLAYER_AUDIOCHANNEL_H


#include <SLES/OpenSLES_Android.h>
#include "BaseChannel.h"

extern "C" {
#include <libswresample/swresample.h>
};

class AudioChannel : public BaseChannel {
public:
    AudioChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,
                 const AVRational &base);

    ~AudioChannel();

    void stop();

    void play();

    void initOpenSL();

    void releaseOpenSL();

    void decode();

    int getPcm();


private:
    pthread_t pid_audio_play;
    pthread_t pid_audio_decode;

    SLObjectItf engineObject = NULL;
    SLEngineItf engineInterface = NULL;

    SLObjectItf outputMixObject = NULL;

    SLObjectItf bqPlayerObject = NULL;
    SLPlayItf bqPlayerInterface = NULL;

    SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue = NULL;

    SwrContext *swr_ctx = NULL;
    int out_channels;
    int out_samplesize;
    int out_sample_rate;

public:
    uint8_t *buffer;

};


#endif //MYPLAYER_AUDIOCHANNEL_H
