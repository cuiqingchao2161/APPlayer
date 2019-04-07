//
// Created by tang on 2018/12/14.
//

#ifndef MYPLAYER_BASECHANNEL_H
#define MYPLAYER_BASECHANNEL_H


#include "JavaCallHelper.h"
#include "macro.h"
#include "safe_queue.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavutil/time.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>
#include <libavutil/rational.h>
};


class BaseChannel {
public:
    BaseChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,
                AVRational base) : channelId(id),
                                   javaCallHelper(javaCallHelper),
                                   avCodecContext(avCodecContext),
                                   time_base(base) {
        pkt_queue.setReleaseHandle(releaseAvPacket);
        frame_queue.setReleaseHandle(releaseAvFrame);
    };


    static void releaseAvFrame(AVFrame *&frame) {
        if (frame) {
            av_frame_free(&frame);
            frame = 0;
        }
    }

    static void releaseAvPacket(AVPacket *&packet) {
        if (packet) {
            av_packet_free(&packet);
            packet = 0;
        }
    }

    virtual ~BaseChannel() {
        if (avCodecContext) {
            avcodec_close(avCodecContext);
            avcodec_free_context(&avCodecContext);
            avCodecContext = 0;
        }
        //todo
        pkt_queue.clear();
        frame_queue.clear();
        LOGE("释放channel:%d %d", pkt_queue.size(), frame_queue.size());
    };

    void startWork() {
        pkt_queue.setWork(1);
        frame_queue.setWork(1);
    }

    void stopWork() {
        pkt_queue.setWork(0);
        frame_queue.setWork(0);
    }

public:
    SafeQueue<AVPacket *> pkt_queue;
    SafeQueue<AVFrame *> frame_queue;
    double clock = 0;
    volatile int channelId;
    volatile bool isPlaying = false;
    AVRational time_base;
    AVCodecContext *avCodecContext;
    JavaCallHelper *javaCallHelper;
};

#endif //MYPLAYER_BASECHANNEL_H
