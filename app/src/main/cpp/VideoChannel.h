//
// Created by tang on 2018/12/13.
//

#ifndef MYPLAYER_VIDEOCHANNEL_H
#define MYPLAYER_VIDEOCHANNEL_H

#include <stdint.h>
#include "JavaCallHelper.h"
#include "BaseChannel.h"
#include "AudioChannel.h"


typedef void (*RenderFrame)(uint8_t *, int, int, int);

class VideoChannel : public BaseChannel {

public:
    VideoChannel(int i, JavaCallHelper *pHelper, AVCodecContext *pContext, AVRational rational,
                 int i1);

    void setRenderCallback(RenderFrame pFunction);

    ~VideoChannel();

    void play();

    void stop();


public:
    int fps = 0;
    RenderFrame renderFrame;
    AudioChannel *audioChannel;
    pthread_t pid_video_play;
    pthread_t pid_synchronize;

    void decodePacket();

    void synchronizeFrame();

    void stopWork();
};


#endif //MYPLAYER_VIDEOCHANNEL_H
