package com.sinaungoding.lemsnordic.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/v1/sensors")
    Call<Void> insert(@Body CombinedData combinedData);
    @GET("/api/v1/amedas/live")
    Call<Amedas> getLastAmedas();
}
