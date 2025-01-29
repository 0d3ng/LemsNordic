package com.sinaungoding.lemsnordic.api;

import com.sinaungoding.lemsnordic.SensorData;

import retrofit2.Call;
import retrofit2.http.Body;

public interface ApiService {
    Call<SensorData> insert(@Body SensorData sensorData);
}
