package com.sinaungoding.lemsnordic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;

public class MyBleManager extends BleManager {
    private static final String TAG = MyBleManager.class.getSimpleName();

    private BluetoothGattCharacteristic targetCharacteristic;

    public MyBleManager(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyBleManagerGattCallback();
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
                            String value = data.getStringValue(0);
                            Log.d(TAG, "onDataReceived: " + value);
                        });

                enableNotifications(targetCharacteristic)
                        .done(device -> Log.d(TAG, "Notifications enabled on: " + targetCharacteristic.getUuid()))
                        .fail((device, status) -> Log.e(TAG, "Failed to enable notifications, status: " + status))
                        .enqueue();

                readCharacteristic(targetCharacteristic)
                        .with((device, data) -> {
                            byte[] dataValue = data.getValue();
                            Log.d(TAG, "initialize data: " + data.toString());
                            float scalePM = 100.0f;
                            float scaleTemp = 10.0f;
                            float scaleHumidity = 1.0f;
                            float co2 = toInt(dataValue[0], dataValue[1]);
                            float pm1 = toFloat(dataValue[2], dataValue[3], scalePM);
                            float pm25 = toFloat(dataValue[4], dataValue[5], scalePM);
                            float pm4 = toFloat(dataValue[6], dataValue[7], scalePM);
                            float pm10 = toFloat(dataValue[8], dataValue[9], scalePM);
                            float temperature = toFloat(dataValue[10], dataValue[11], scaleTemp);
                            float humidity = toFloat(dataValue[12], dataValue[13], scaleHumidity);

                            Log.d(TAG, "CO2: " + co2);
                            Log.d(TAG, "PM1: " + pm1);
                            Log.d(TAG, "PM2.5: " + pm25);
                            Log.d(TAG, "PM4: " + pm4);
                            Log.d(TAG, "PM10: " + pm10);
                            Log.d(TAG, "Temperature: " + temperature);
                            Log.d(TAG, "Humidity: " + humidity);
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
                Log.d(TAG, "Found service: " + service.getUuid());
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.d(TAG, "Found characteristic: " + characteristic.getUuid());

                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        Log.d(TAG, "Characteristic supports READ: " + characteristic.getUuid());
                    }
                    if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        Log.d(TAG, "Characteristic supports NOTIFY: " + characteristic.getUuid());
                        targetCharacteristic = characteristic;
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
            targetCharacteristic = null;
        }

    }

    int toInt(byte b1, byte b2) {
        return ((b1 & 0xFF) << 8) | (b2 & 0xFF);
    }

    float toFloat(byte b1, byte b2, float scaleFactor) {
        int intValue = toInt(b1, b2);
        return intValue / scaleFactor;
    }
}