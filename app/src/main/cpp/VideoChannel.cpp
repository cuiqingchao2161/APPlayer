//
// Created by tang on 2018/12/13.
//
#include "VideoChannel.h"

/*void* (*__start_routine)(void*) 的实现*/
void *decode(void *args) {
    VideoChannel *videoChannel = static_cast<VideoChannel *>(args);
    videoChannel->decodePacket();
    return 0;
}

/*void *(*synchronize)(void *);*/
void *synchronize(void *args) {
    VideoChannel *videoChannel = static_cast<VideoChannel *>(args);
    videoChannel->synchronizeFrame();
    return 0;
}

void dropFrame(queue<AVFrame *> &q) {
    if (!q.empty()) {
        AVFrame *frame = q.front();
        q.pop();
        BaseChannel::releaseAvFrame(frame);
    }
}

VideoChannel::VideoChannel(int i, JavaCallHelper *pHelper, AVCodecContext *pContext,
                           AVRational rational, int i1) : BaseChannel(i, pHelper, pContext,
                                                                      rational), fps(fps) {
    frame_queue.setReleaseHandle(releaseAvFrame);
    frame_queue.setSyncHandle(dropFrame);
}

VideoChannel::~VideoChannel() {
}

void VideoChannel::setRenderCallback(RenderFrame renderFrame) {
    this->renderFrame = renderFrame;
}


void VideoChannel::play() {
    startWork();
    isPlaying = true;
    pthread_create(&pid_video_play, NULL, decode, this);
    pthread_create(&pid_synchronize, NULL, synchronize, this);
}


void VideoChannel::stop() {
    isPlaying = 0;
    javaCallHelper = 0;
    stopWork();
    pthread_join(pid_synchronize, 0);
    pthread_join(pid_video_play, 0);
}

void VideoChannel::decodePacket() {
    AVPacket *packet = 0;
    while (isPlaying) {
        int ret = pkt_queue.deQueue(packet);
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }
        ret = avcodec_send_packet(avCodecContext, packet);
        releaseAvPacket(packet);
        if (ret == AVERROR(EAGAIN)) {
            continue;
        } else if (ret < 0) {
            break;
        }
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(avCodecContext, frame);
        if (ret == AVERROR(EAGAIN)) {
            continue;
        } else if (ret < 0) {
            break;
        }
        while (frame_queue.size() > 100 && isPlaying) {
            av_usleep(1000 * 10);
            //todo 为什么这里直接continue了？
            continue;
        }
        frame_queue.enQueue(frame);
    }
    releaseAvPacket(packet);
}

/**
 * 视频播放
 */
void VideoChannel::synchronizeFrame() {
    //转换rgba
    SwsContext *sws_ctx = sws_getContext(avCodecContext->width, avCodecContext->height,
                                         avCodecContext->pix_fmt, avCodecContext->width,
                                         avCodecContext->height, AV_PIX_FMT_RGBA,
                                         SWS_BILINEAR, 0, 0, 0);
    double frame_delay = 1.0 / fps;
    uint8_t *dst_data[4];
    int dst_linesize[4];
    av_image_alloc(dst_data, dst_linesize, avCodecContext->width, avCodecContext->height,
                   AV_PIX_FMT_RGBA, 1);
    AVFrame *frame = 0;
    while (isPlaying) {
        int ret = frame_queue.deQueue(frame);
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }

#if 1
        //todo 视频与音频同步(待理解)
        if ((clock = frame->best_effort_timestamp) == AV_NOPTS_VALUE) {
            clock = 0;
        }
        clock = clock * av_q2d(time_base);
        double repeat_pict = frame->repeat_pict;
        double extra_delay = repeat_pict / (2 * fps);
        double delay = extra_delay + frame_delay;
        if (clock == 0) {
            av_usleep(delay * 1000000);
        } else {
            double audioClock = audioChannel ? audioChannel->clock : 0;
            double diff = fabs(clock - audioClock);
            if (audioChannel) {
                if (clock > audioClock) {
                    if (diff > 1) {
                        av_usleep((delay * 2) * 1000000);
                    } else {
                        av_usleep((delay + diff) * 1000000);
                    }
                } else {
                    if (diff > 1) {

                    } else if (diff >= 0.55) {
                        releaseAvFrame(frame);
                        frame_queue.sync();
                        continue;
                    } else {

                    }
                }
            } else {
                av_usleep(delay * 1000000);
            }
        }
#endif
        if (javaCallHelper && !audioChannel) {
            javaCallHelper->onProgress(THREAD_CHILD, clock);
        }

        sws_scale(sws_ctx, reinterpret_cast<const uint8_t *const *>(frame->data), frame->linesize,
                  0,
                  frame->height, dst_data, dst_linesize);
        renderFrame(dst_data[0], dst_linesize[0], avCodecContext->width, avCodecContext->height);
        releaseAvFrame(frame);
    }
    av_freep(&dst_data[0]);
    isPlaying = false;
    releaseAvFrame(frame);
    sws_freeContext(sws_ctx);
}

void VideoChannel::stopWork() {

}

