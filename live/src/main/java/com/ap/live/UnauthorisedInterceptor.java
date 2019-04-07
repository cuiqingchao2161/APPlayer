package com.ap.live;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by tang on 2018/12/13.
 */

class UnauthorisedInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = null;
        try {
            response = chain.proceed(chain.request());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
