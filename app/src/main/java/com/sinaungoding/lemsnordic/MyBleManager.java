package com.sinaungoding.lemsnordic;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

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
                            receivedValue.postValue(data.toString());
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

    int toInt(byte b1, byte b2) {
        return ((b1 & 0xFF) << 8) | (b2 & 0xFF);
    }

    float toFloat(byte b1, byte b2, float scaleFactor) {
        int intValue = toInt(b1, b2);
        return intValue / scaleFactor;
    }
}