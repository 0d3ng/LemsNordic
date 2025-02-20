package com.sinaungoding.lemsnordic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.sinaungoding.lemsnordic.api.Amedas;
import com.sinaungoding.lemsnordic.api.ApiClient;
import com.sinaungoding.lemsnordic.api.ApiService;
import com.sinaungoding.lemsnordic.api.CombinedData;
import com.sinaungoding.lemsnordic.api.SensorData;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBleManager extends ObservableBleManager {
    private static final String TAG = MyBleManager.class.getSimpleName();
    private static final String SERVICE_BTEVS1 = "f9cc1523-4e0a-49e5-8cf3-0007e819ea1e";
    private static final String CHARACTERISTIC_BTEVS1 = "f9cc152a-4e0a-49e5-8cf3-0007e819ea1e";
    private BluetoothGattCharacteristic targetCharacteristic;
    private final MutableLiveData<SensorData> dataLiveData = new MutableLiveData<>();
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT;

    static {
        SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    private ApiService apiService;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Activity activity;
    private double latitude, longitude;

    public MyBleManager(@NonNull Context context) {
        super(context);
        this.activity = (Activity) context;
        // TODO: 1/29/2025 Please prepare end point from this function
        apiService = ApiClient.getClient(context).create(ApiService.class);
        AndroidThreeTen.init(context);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyBleManagerGattCallback();
    }

    public MutableLiveData<SensorData> getDataLiveData() {
        return dataLiveData;
    }

    private class MyBleManagerGattCallback extends BleManagerGattCallback {
        @SuppressLint("MissingPermission")
        @Override
        protected void initialize() {
            super.initialize();
            Log.d(TAG, "Initializing BLE connection...");
            requestMtu(50)
                    .done((device -> Log.d(TAG, "initialize: " + device.getAddress())))
                    .fail(((device, status) -> Log.d(TAG, "initialize: " + device + " :" + status)))
                    .enqueue();
            if (targetCharacteristic != null) {

                setNotificationCallback(targetCharacteristic)
                        .with((device, data) -> {
                            Log.d(TAG, "onDataReceived: " + data.toString());
                            String[] sensors = data.toString().split("-");
                            int hum = Integer.parseInt(sensors[2], 16);
                            String hex = sensors[1] + sensors[0];
                            hex = hex.replaceAll("[()\\s]", "").replace("0x", "");
                            float temperature = (float) Integer.parseInt(hex, 16) / 10;
                            int co2 = Integer.parseInt(sensors[4] + sensors[3], 16);
                            byte[] arr_pm1 = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 5, 9);
                            float pm1 = parseAndRoundFloat(arr_pm1);
                            byte[] arr_pm25 = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 9, 13);
                            float pm25 = parseAndRoundFloat(arr_pm25);
                            byte[] arr_pm4 = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 13, 17);
                            float pm4 = parseAndRoundFloat(arr_pm4);
                            byte[] arr_pm10 = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 17, 21);
                            float pm10 = parseAndRoundFloat(arr_pm10);
                            String timestamp = SIMPLE_DATE_FORMAT.format(new Date());
                            Log.i(TAG, "onDataReceived timestamp   : " + timestamp);
                            Log.i(TAG, "onDataReceived Humidity    : " + hum);
                            Log.i(TAG, "onDataReceived Temperature : " + temperature);
                            Log.i(TAG, "onDataReceived CO2         : " + co2);
                            Log.i(TAG, "onDataReceived PM1         : " + pm1);
                            Log.i(TAG, "onDataReceived PM25        : " + pm25);
                            Log.i(TAG, "onDataReceived PM4         : " + pm4);
                            Log.i(TAG, "onDataReceived PM10        : " + pm10);

                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, location -> {
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                } else {
                                    latitude = 0;
                                    longitude = 0;
                                }
                            });
                            fusedLocationProviderClient.getLastLocation().addOnFailureListener(activity, e -> {
                                Log.e(TAG, "onFailure: " + e.getMessage(), e);
                                latitude = 0;
                                longitude = 0;
                            });

                            SensorData sensorData = new SensorData(co2, pm1, pm25, pm4, pm10, temperature, hum, latitude, longitude, timestamp);
                            dataLiveData.postValue(sensorData);
                            Call<Amedas> amedasCall = apiService.getLastAmedas();
                            amedasCall.enqueue(new Callback<>() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onResponse(@NonNull Call<Amedas> call, @NonNull Response<Amedas> response) {
                                    Log.i(TAG, "onResponse: " + call.isExecuted());
                                    if (response.isSuccessful()) {
                                        Log.d(TAG, String.format("amedasCall success: %s %d", response.message(), response.code()));
                                        // TODO: 2/5/2025 please parsing error, the timestamp using UTC and change first to local
                                        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                                        Amedas amedas = response.body();
                                        Log.i(TAG, "onResponse: " + amedas);
                                        LocalDateTime localDateTime = LocalDateTime.parse(amedas.getTimestamp(), formatter);

                                        ZoneId localZoneId = ZoneId.of("Asia/Tokyo");
                                        ZonedDateTime zonedDateTimeUTC = localDateTime.atZone(ZoneId.of("UTC"));
                                        ZonedDateTime zonedDateTimeLocal = zonedDateTimeUTC.withZoneSameInstant(localZoneId);
                                        DateTimeFormatter localFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                        String timestamp = zonedDateTimeLocal.format(localFormatter);
                                        Log.i(TAG, "onResponse: " + timestamp);
                                        amedas.setTimestamp(timestamp);
                                        CombinedData.Data combine = new CombinedData.Data(sensorData, amedas);
                                        CombinedData combinedData = new CombinedData(SIMPLE_DATE_FORMAT.format(new Date()), combine);
                                        insertSensorData(combinedData);
                                    } else {
                                        Log.d(TAG, String.format("message: %s %d", response.message(), response.code()));
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Amedas> call, @NonNull Throwable throwable) {
                                    Log.e(TAG, String.format("amedasCall failure: %s", throwable.getMessage()), throwable);
                                }
                            });
                        });

                enableNotifications(targetCharacteristic)
                        .done(device -> Log.d(TAG, "Notifications enabled on: " + targetCharacteristic.getUuid()))
                        .fail((device, status) -> Log.e(TAG, "Failed to enable notifications, status: " + status))
                        .enqueue();

                readCharacteristic(targetCharacteristic)
                        .with((device, data) -> {
                            byte[] dataValue = data.getValue();
                            Log.d(TAG, "initialize data: " + data.toString());
                        })
                        .fail((device, status) -> Log.e(TAG, "Failed to read characteristic, status: " + status))
                        .enqueue();
            } else {
                Log.w(TAG, "Target characteristic is null, cannot initialize notifications.");
            }
        }

        private void insertSensorData(CombinedData combinedData) {
            // TODO: 1/29/2025 if end point is ready, please uncomment
            Call<Void> call = apiService.insert(combinedData);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, String.format("insert data successfully: %s %d", response.message(), response.code()));
                    } else {
                        Log.d(TAG, String.format("insert data failure: %s %d", response.message(), response.code()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable throwable) {
                    Log.e(TAG, String.format("insert failure: %s", throwable.getMessage()), throwable);
                }
            });
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            Log.d(TAG, "Discovering services...");
            List<BluetoothGattService> services = gatt.getServices();

            for (BluetoothGattService service : services) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d(TAG, String.format("Service %s characteristic %s", service.getUuid().toString(), characteristic.getUuid().toString()));

                    if ((service.getUuid().toString().equals(SERVICE_BTEVS1)) && (characteristic.getUuid().toString().equals(CHARACTERISTIC_BTEVS1))) {
                        targetCharacteristic = characteristic;
                        Log.d(TAG, String.format("Found characteristic %s service %s", service.getUuid().toString(), characteristic.getUuid().toString()));
                        break;
                    }
                }
            }

            boolean isSupported = targetCharacteristic != null;
            Log.d(TAG, "Required service supported: " + isSupported);
            return isSupported;
        }

        @Override
        protected void onServicesInvalidated() {
            Log.d(TAG, "Services invalidated. Cleaning up...");
            dataLiveData.postValue(null);
            targetCharacteristic = null;
        }

    }

    private float parseAndRoundFloat(byte[] data) {
        // Konversi byte array ke nilai float
        float value = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        // Bulatkan nilai float ke satu digit di belakang koma
        BigDecimal bd = new BigDecimal(Float.toString(value));
        bd = bd.setScale(1, RoundingMode.HALF_UP); // Pembulatan ke atas jika >= 0.5
        return bd.floatValue();
    }
}