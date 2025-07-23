package com.sinaungoding.lemsnordic.api;

import android.content.Context;
import android.util.Log;

import com.sinaungoding.lemsnordic.MainActivity;
import com.sinaungoding.lemsnordic.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = ApiClient.class.getSimpleName();
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        String baseUrl = context.getString(R.string.base_url);
        String token = context.getString(R.string.app_token);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(token))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
