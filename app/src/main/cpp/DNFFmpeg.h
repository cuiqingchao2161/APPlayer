//
// Created by tang on 2018/12/13.
//

#ifndef MYPLAYER_DNFFMPEG_H
#define MYPLAYER_DNFFMPEG_H


#include "JavaCallHelper.h"
#include "VideoChannel.h"
#include "AudioChannel.h"

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/time.h>
};

class DNFFmpeg {
public:
    DNFFmpeg(JavaCallHelper *javaCallHelper, const char *dataSource);

    ~DNFFmpeg();

    void start();

    void prepare();

    void setRenderCallback(RenderFrame param);

    void play();

    void prepareFFmpeg();

public:
    char *url;  //数据源
    JavaCallHelper *javaCallHelper;

    bool isPlaying;


    AVFormatContext *formatContext = 0;
    RenderFrame renderFrame;
    int64_t duration; //时长
    VideoChannel *videoChannel;
    AudioChannel *audioChannel;


    pthread_mutex_t seekMutex;
    pthread_t pid_prepare; //
    pthread_t pid_play;

    void stop();
    pthread_t pid_stop;
};


#endif //MYPLAYER_DNFFMPEG_H
