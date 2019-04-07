package com.ap.live;


import com.ap.live.list.LiveList;
import com.ap.live.room.Room;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Lance
 * @date 2018/9/7
 */
public class LiveManager {

    private final LiveService liveService;
    private static LiveManager instance;

    private LiveManager() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY); // BuildConfig.DEBUG
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new UnauthorisedInterceptor())
                .readTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.m.panda.tv/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        liveService = retrofit.create(LiveService.class);
    }

    public static LiveManager getInstance() {
        if (null == instance) {
            synchronized (LiveService.class) {
                if (null == instance) {
                    instance = new LiveManager();
                }
            }
        }
        return instance;
    }


    public Flowable<LiveList> getLiveList(String cate) {
        return liveService.getLiveList(cate, 1, 10, "3.3.1.5978");
    }

    public Flowable<Room> getLiveRoom(String id) {
        return liveService.getLiveRoom(id, "3.3.1.5978", 1, "json", "android");
    }
}
