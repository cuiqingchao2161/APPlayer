//
// Created by tang on 2018/12/13.
//

#include "AudioChannel.h"

void *audioPlay(void *args) {
    AudioChannel *audioChannel = static_cast<AudioChannel *>(args);
    audioChannel->initOpenSL(); //播放
    return 0;
}

void *audioDecode(void *args) {
    AudioChannel *audioChannel = static_cast<AudioChannel *>(args);
    audioChannel->decode(); //解码
    return 0;
}


void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    AudioChannel *audioChannel = static_cast<AudioChannel *>(context);
    int datalen = audioChannel->getPcm();
    if (datalen > 0) {
        (*bq)->Enqueue(bq, audioChannel->buffer, datalen);
    }
}

AudioChannel::AudioChannel(int id, JavaCallHelper *javaCallHelper, AVCodecContext *avCodecContext,
                           const AVRational &base) : BaseChannel(id, javaCallHelper, avCodecContext,
                                                                 base) {
    out_channels = av_get_channel_layout_nb_channels(AV_CH_LAYOUT_STEREO);
    out_samplesize = av_get_bytes_per_sample(AV_SAMPLE_FMT_S16);
    out_sample_rate = 44100;
    buffer = (uint8_t *) malloc(out_sample_rate * out_samplesize * out_channels);
}


AudioChannel::~AudioChannel() {
    free(buffer);
    buffer = 0;
}

void AudioChannel::stop() {
    isPlaying = 0;
    javaCallHelper = 0;
    stopWork();
    pthread_join(pid_audio_play, 0);
    pthread_join(pid_audio_decode, 0);
}

//todo 待理解
void AudioChannel::play() {
    swr_ctx = swr_alloc_set_opts(0, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16, out_sample_rate,
                                 avCodecContext->channel_layout, avCodecContext->sample_fmt,
                                 avCodecContext->sample_rate, 0, 0);
    swr_init(swr_ctx);
    startWork();
    isPlaying = true;
    pthread_create(&pid_audio_play, NULL, audioPlay, this);
    pthread_create(&pid_audio_decode, NULL, audioDecode, this);
}

void AudioChannel::initOpenSL() {
    SLresult result;
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineInterface);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    result = (*engineInterface)->CreateOutputMix(engineInterface, &outputMixObject, 0, 0, 0);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        return;
    }

    SLDataLocator_AndroidBufferQueue android_queue = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                      2};
    //pcm数据格式
    SLDataFormat_PCM pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1, SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_PCMSAMPLEFORMAT_FIXED_16,
                            SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                            SL_BYTEORDER_LITTLEENDIAN};

    SLDataSource slDataSource = {&android_queue, &pcm};
    //设置混音器
    SLDataLocator_OutputMix outputMix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&outputMix, NULL};
    //需要的接口
    const SLInterfaceID ids[1] = {SL_IID_BUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    //创建播放器
    (*engineInterface)->CreateAudioPlayer(engineInterface, &bqPlayerObject, &slDataSource,
                                          &audioSnk,
                                          1, ids, req);
    (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerInterface);
    //获得播放器接口
    (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE, &bqPlayerBufferQueue);
    //设置回调
    (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue, bqPlayerCallback, this);
    //设置播放状态
    (*bqPlayerInterface)->SetPlayState(bqPlayerInterface, SL_PLAYSTATE_PLAYING);

    bqPlayerCallback(bqPlayerBufferQueue, this);
}

void AudioChannel::decode() {
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
            continue;
        }
        frame_queue.enQueue(frame);
    }
    releaseAvPacket(packet);
}


int AudioChannel::getPcm() {
    int data_size = 0;
    AVFrame *frame = 0;
    while (isPlaying) {
        int ret = frame_queue.deQueue(frame);
        if (!isPlaying) {
            break;
        }
        if (!ret) {
            continue;
        }
        uint64_t dst_nb_samples = av_rescale_rnd(
                swr_get_delay(swr_ctx, frame->sample_rate) + frame->nb_samples,
                out_sample_rate, frame->sample_rate, AV_ROUND_UP);
        int nb = swr_convert(swr_ctx, &buffer, dst_nb_samples, (const uint8_t **) frame->data,
                             frame->nb_samples);
        data_size = nb * out_channels * out_samplesize;
        clock = frame->best_effort_timestamp * av_q2d(time_base);
        if (javaCallHelper) {
            javaCallHelper->onProgress(THREAD_CHILD, clock);
        }
        break;
    }
    releaseAvFrame(frame);
    return data_size;
}






































































