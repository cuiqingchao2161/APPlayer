//
// Created by tang on 2018/12/14.
//

#ifndef MYPLAYER_SAFE_QUEUE_H
#define MYPLAYER_SAFE_QUEUE_H


#include <queue>
#include <pthread.h>
#include "macro.h"

using namespace std;


template<typename T>
class SafeQueue {
    typedef void (*ReleaseHandle)(T &);

    typedef void (*SyncHandle)(queue<T> &);

public:
    SafeQueue() {
        pthread_mutex_init(&mutex, NULL);
        pthread_cond_init(&cond, NULL);
    }

    ~SafeQueue() {
        pthread_cond_destroy(&cond);
        pthread_mutex_destroy(&mutex);
    }

    void enQueue(T new_value) {
        pthread_mutex_lock(&mutex);
        if (work) {
            q.push(new_value);
            pthread_cond_signal(&cond);
            pthread_mutex_unlock(&mutex);
        } else {
            LOGE("无法加入数据====:%d", q.size());
            releaseHandle(new_value);
        }
        pthread_mutex_unlock(&mutex);
    };

    int deQueue(T &value) {
        int ret = 0;
        pthread_mutex_lock(&mutex);
        while (work && q.empty()) {
            pthread_cond_wait(&cond, &mutex);
        }
        if (!q.empty()) {
            value = q.front();
            q.pop();
            ret = 1;
        }
        pthread_mutex_unlock(&mutex);
        return ret;
    }

    void setWork(int work) {
        pthread_mutex_lock(&mutex);
        this->work = work;
        pthread_cond_signal(&cond);
        pthread_mutex_unlock(&mutex);
    };

    int empty() {
        return q.empty();
    }

    int size() {
        return q.size();
    }

    void setReleaseHandle(ReleaseHandle r) {
        releaseHandle = r;
    }

    void setSyncHandle(SyncHandle s) {
        syncHandle = s;
    }

    void clear() {
        pthread_mutex_lock(&mutex);
        int size = q.size();
        for (int i = 0; i < size; ++i) {
            T value = q.front();
            releaseHandle(value);
            q.pop();
        }
        LOGE("清空数据====:%d", q.size());
        pthread_mutex_unlock(&mutex);
    }

    void sync() {
        pthread_mutex_lock(&mutex);
        syncHandle(q);
        pthread_mutex_unlock(&mutex);
    }

private:
    pthread_mutex_t mutex;
    pthread_cond_t cond;

    queue<T> q;
    int work;
    ReleaseHandle releaseHandle;
    SyncHandle syncHandle;
};

#endif //MYPLAYER_SAFE_QUEUE_H
