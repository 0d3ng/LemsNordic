package com.sinaungoding.lemsnordic;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MyBleViewModel extends AndroidViewModel {
    private final MyBleManager bleManager;


    public MyBleViewModel(@NonNull Application application) {
        super(application);
        bleManager = new MyBleManager(application);
    }

    public LiveData<String> getReceivedValue() {
        return bleManager.getReceivedValue();
    }

}
