//
// Created by tang on 2018/12/13.
//

#include <string.h>
#include <pthread.h>
#include "DNFFmpeg.h"

#include "macro.h"
#include "VideoChannel.h"

void *prepareFFmpeg_(void *args) {
    DNFFmpeg *dnfFmpeg = static_cast<DNFFmpeg *>(args);
    dnfFmpeg->prepareFFmpeg();
    return 0;
}

void *startThread(void *args) {
    DNFFmpeg *ffmpeg = static_cast<DNFFmpeg *>(args);
    ffmpeg->play();
    return 0;
}

void *async_stop(void *args) {
    DNFFmpeg *ffmpeg = static_cast<DNFFmpeg *>(args);
    pthread_join(ffmpeg->pid_prepare, 0);
    ffmpeg->isPlaying = 0;
    pthread_join(ffmpeg->pid_play, 0);
    DELETE(ffmpeg->audioChannel);
    DELETE(ffmpeg->videoChannel);
    if (ffmpeg->formatContext) {
        avformat_close_input(&ffmpeg->formatContext);
        avformat_free_context(ffmpeg->formatContext);
        ffmpeg->formatContext = NULL;
    }
    DELETE(ffmpeg);
    LOGE("释放");
    return 0;
}

DNFFmpeg::DNFFmpeg(JavaCallHelper *javaCallHelper, const char *dataSource)
        : javaCallHelper(javaCallHelper) {
    url = new char[strlen(dataSource) + 1];  //todo 为什么要进行内存复制
    strcpy(url, dataSource);
    isPlaying = false;
    pthread_mutex_init(&seekMutex, 0);
}

void DNFFmpeg::prepare() {
    pthread_create(&pid_prepare, NULL, prepareFFmpeg_, this);
}

DNFFmpeg::~DNFFmpeg() {
    pthread_mutex_destroy(&seekMutex);
    javaCallHelper = 0;
    DELETE(url);
}

void DNFFmpeg::prepareFFmpeg() {
    LOGE("TANG--CONNECT NETWORK");
    avformat_network_init();
    formatContext = avformat_alloc_context();
    av_register_all();
    AVDictionary *opts = NULL;
    av_dict_set(&opts, "timeout", "3000000", 0);

    int ret = avformat_open_input(&formatContext, url, NULL, &opts);
    LOGE("%s open %d  %s", url, ret, av_err2str(ret));

    if (ret != 0) {
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_CAN_NOT_OPEN_URL);
        }
        return;
    }
    if (avformat_find_stream_info(formatContext, NULL) < 0) {
        if (javaCallHelper) {
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_FIND_DECODER_FAIL);
        }
        return;;
    }
    duration = formatContext->duration / 1000000;
    for (int i = 0; i < formatContext->nb_streams; ++i) {
        AVCodecParameters *codecpar = formatContext->streams[i]->codecpar;
        AVCodec *dec = avcodec_find_decoder(codecpar->codec_id);
        if (!dec) {
            if (javaCallHelper) {
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_FIND_DECODER_FAIL);
            }
            return;
        }
        AVCodecContext *codecContext = avcodec_alloc_context3(dec);
        if (!codecContext) {
            if (javaCallHelper) {
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_ALLOC_CODEC_CONTEXT_FAIL);
            }
            return;
        }
        if (avcodec_parameters_to_context(codecContext, codecpar) < 0) {
            if (javaCallHelper) {
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_CODEC_CONTEXT_PARAMETERS_FAIL);
            }
            return;
        }
        if (avcodec_open2(codecContext, dec, 0) != 0) {
            if (javaCallHelper)
                javaCallHelper->onError(THREAD_CHILD, FFMPEG_OPEN_DECODER_FAIL);
            return;
        }
        //时间基
        AVRational base = formatContext->streams[i]->time_base;
        if (AVMEDIA_TYPE_VIDEO == codecpar->codec_type) {
            int fps = av_q2d(formatContext->streams[i]->avg_frame_rate);
            videoChannel = new VideoChannel(i, javaCallHelper, codecContext, base, fps);
            videoChannel->setRenderCallback(renderFrame);
        } else if (AVMEDIA_TYPE_AUDIO == codecpar->codec_type) {
            audioChannel = new AudioChannel(i, javaCallHelper, codecContext, base);
        } else {

        }
    }
    if (!videoChannel && !audioChannel) {
        if (javaCallHelper)
            javaCallHelper->onError(THREAD_CHILD, FFMPEG_NOMEDIA);
        return;
    }
    if (javaCallHelper) {
        javaCallHelper->onParpare(THREAD_CHILD);  //准备成功
    }
}

void DNFFmpeg::setRenderCallback(RenderFrame renderFrame) {
    this->renderFrame = renderFrame;
}

void DNFFmpeg::start() {
    isPlaying = true;
    if (audioChannel) {
        audioChannel->play();
    }
    if (videoChannel) {
        videoChannel->audioChannel = audioChannel;
        videoChannel->play();
    }
    pthread_create(&pid_play, NULL, startThread, this);
}

/**
 * 播放
 */
void DNFFmpeg::play() {
    int ret = 0;
    while (isPlaying) {
        //读取文件时一下子读取完了
        if (audioChannel && audioChannel->pkt_queue.size() > 100) {
            av_usleep(1000 * 10);
            continue;
        }
        if (videoChannel && videoChannel->pkt_queue.size() > 100) {
            av_usleep(1000 * 10);
            continue;
        }
        pthread_mutex_lock(&seekMutex);
        AVPacket *packet = av_packet_alloc(); //声明一个空的packet
        ret = av_read_frame(formatContext, packet);
        pthread_mutex_unlock(&seekMutex);
        if (ret == 0) {
            if (audioChannel && packet->stream_index == audioChannel->channelId) {
                audioChannel->pkt_queue.enQueue(packet);
            } else if (videoChannel && packet->stream_index == videoChannel->channelId) {
                videoChannel->pkt_queue.enQueue(packet);
            }
        } else if (ret == AVERROR_EOF) {
            //读取完毕但不一定是播放完毕
            if (videoChannel->pkt_queue.empty() && videoChannel->frame_queue.empty()
                && audioChannel->pkt_queue.empty() && audioChannel->frame_queue.empty()) {
                LOGE("播放完毕");
                break;
            }
        } else {
            break;
        }
    }
    isPlaying = 0;
    audioChannel->stop();
    videoChannel->stop();
}

void DNFFmpeg::stop() {
    javaCallHelper = 0;
    if (audioChannel) {
        audioChannel->javaCallHelper = 0;
    }
    if (videoChannel) {
        videoChannel->javaCallHelper = 0;
    }
    pthread_create(&pid_stop, 0, async_stop, this);
}





















































