package com.ap.player;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.WindowManager;


/**
 * Created by qingchao.cui on 2019/3/14.
 */

public class APlayerApp extends Application{
    private static APlayerApp mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
//        ToastUtils.init(this);
//        HttpUtils.init(this);
//        DensityHelper.init(this, 1920);
        if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_LANDSCAPE) {
            // land donothing is ok
//            DensityHelper.init(this, 1920);
        } else if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT) {
            // port donothing is ok
//            DensityHelper.init(this, 1080);
        }
    }

    public static APlayerApp getApp(){
        return mApp;
    }


//    private HttpProxyCacheServer proxy;


//    public static HttpProxyCacheServer getProxy(Context context) {
//        QfAdTvApp app = (QfAdTvApp) context.getApplicationContext();
//        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
//    }
//
//    private HttpProxyCacheServer newProxy() {
//        HttpProxyCacheServer proxy = new HttpProxyCacheServer.Builder(this)
//                .fileNameGenerator(new MyFileNameGenerator())
//                .build();
//        return proxy;
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation ==Configuration.ORIENTATION_LANDSCAPE) {
            // land donothing is ok
//            DensityHelper.init(this, 1920);
        } else if(newConfig.orientation ==Configuration.ORIENTATION_PORTRAIT) {
            // port donothing is ok
//            DensityHelper.init(this, 1080);
        }

    }

    public void resetDensity(int w){
        Point size = new Point();
        ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
        getResources().getDisplayMetrics().xdpi = size.x/w*72f;
    }

}
