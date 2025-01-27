package com.sinaungoding.lemsnordic;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import no.nordicsemi.android.ble.livedata.ObservableBleManager;

public class MyBleManager extends ObservableBleManager {
    private static final String TAG = MyBleManager.class.getSimpleName();
    private static final String SERVICE_BTEVS1 = "f9cc1523-4e0a-49e5-8cf3-0007e819ea1e";
    private static final String CHARACTERISTIC_BTEVS1 = "f9cc152a-4e0a-49e5-8cf3-0007e819ea1e";
    private BluetoothGattCharacteristic targetCharacteristic;
    private final MutableLiveData<String> receivedValue = new MutableLiveData<>();

    public MyBleManager(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyBleManagerGattCallback();
    }

    public MutableLiveData<String> getReceivedValue() {
        return receivedValue;
    }

    private class MyBleManagerGattCallback extends BleManagerGattCallback {
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
                            byte[] temp = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 5, 9);
                            float pm1 = parseAndRoundFloat(temp);
                            temp = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 9, 13);
                            float pm25 = parseAndRoundFloat(temp);
                            temp = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 13, 17);
                            float pm4 = parseAndRoundFloat(temp);
                            temp = Arrays.copyOfRange(Objects.requireNonNull(data.getValue()), 17, 21);
                            float pm10 = parseAndRoundFloat(temp);

                            Log.i(TAG, "onDataReceived Humidity    : " + hum);
                            Log.i(TAG, "onDataReceived Temperature : " + temperature);
                            Log.i(TAG, "onDataReceived CO2         : " + co2);
                            Log.i(TAG, "onDataReceived PM1         : " + pm1);
                            Log.i(TAG, "onDataReceived PM25        : " + pm25);
                            Log.i(TAG, "onDataReceived PM4         : " + pm4);
                            Log.i(TAG, "onDataReceived PM10        : " + pm10);
//                            receivedValue.postValue(data.toString());

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
            receivedValue.postValue(null);
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