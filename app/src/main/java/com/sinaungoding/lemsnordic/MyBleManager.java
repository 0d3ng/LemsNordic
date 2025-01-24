package com.sinaungoding.lemsnordic;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import no.nordicsemi.android.ble.BleManager;

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
                        .with((device, data) -> Log.d(TAG, "Initial read value: " + data.toString()))
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
}