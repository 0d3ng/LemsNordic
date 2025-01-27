package com.sinaungoding.lemsnordic;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MyBleViewModel extends AndroidViewModel {
    private final MyBleManager bleManager;


    public MyBleViewModel(@NonNull Application application) {
        super(application);
        bleManager = new MyBleManager(application);
    }

    public LiveData<SensorData> getReceivedValue() {
        return bleManager.getReceivedValue();
    }

    public void connectDevice(BluetoothDevice device) {
        bleManager.connect(device).enqueue();
    }

}
