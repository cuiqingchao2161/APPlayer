package com.ap.player;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by tang on 2018/12/11.
 */

public class APlayer implements SurfaceHolder.Callback {
    static {
        System.loadLibrary("native-lib");
    }

    private String dataSource; //播放数据源

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        native_set_surface(holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void onPrepare() {
        if (null != onPrepareListener) {
            onPrepareListener.onPrepared();
        }
    }

    /**
     * 开始
     */
    public void start() {
        native_start();
    }

    /**
     *
     */
    private native void native_start();

    /**
     * @param surface
     */
    native void native_set_surface(Surface surface);


    public void setDataSource(String url) {
        this.dataSource = url;
    }

    public void prepare() {
        native_prepare(this.dataSource);
    }

    native void native_prepare(String dataSource);

    /**
     * 停止
     */
    public void stop() {
        native_stop();
    }

    native void native_stop();

    /**
     * 释放
     */
    public void release() {
        native_release();
    }

    native void native_release();

    public interface OnPrepareListener {
        void onPrepared();
    }

    public interface OnErrorListener {
        void onError(int error);
    }

    public interface OnProgressListener {
        void onProgress(int progress);
    }


    public void onError(int errorCode) {
        //todo 停止
        // stop();
        if (null != onErrorListener) {
            onErrorListener.onError(errorCode);
        }
    }
/*
    */

    /**
     * native 回调给java 播放进去的
     *
     * @param progress
     */
    public void onProgress(int progress) {
        if (null != onProgressListener) {
            onProgressListener.onProgress(progress);
        }
    }


    private OnPrepareListener onPrepareListener;
    private SurfaceHolder surfaceHolder;
    private OnErrorListener onErrorListener;
    private OnProgressListener onProgressListener;

    /**
     * 设置surface
     *
     * @param surfaceView
     */
    public void setSurfaceView(SurfaceView surfaceView) {
        if (null != this.surfaceHolder) {
            this.surfaceHolder.removeCallback(this);
        }
        this.surfaceHolder = surfaceView.getHolder();
        native_set_surface(surfaceHolder.getSurface());
        this.surfaceHolder.addCallback(this);
    }

    public void setOnPrepareListener(OnPrepareListener onPrepareListener) {
        this.onPrepareListener = onPrepareListener;
    }

    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

}
