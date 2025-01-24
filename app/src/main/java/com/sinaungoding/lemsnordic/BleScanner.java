package com.sinaungoding.lemsnordic;

import static no.nordicsemi.android.support.v18.scanner.ScanSettings.*;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BleScanner {
    private static final String TAG = BleScanner.class.getSimpleName();
    private BluetoothLeScannerCompat scanner;

    public void startScan(Context context) {
        scanner = BluetoothLeScannerCompat.getScanner();

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(SCAN_MODE_LOW_LATENCY)
                .build();

        scanner.startScan(null, settings, new ScanCallback() {


            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
                try {
                    BluetoothDevice device = result.getDevice();
                    String deviceName = device.getName();
                    String macAddress = device.getAddress();
                    Log.i(TAG, String.format("Found device: %s [%s]", deviceName, macAddress));
                } catch (SecurityException e) {
                    Log.e(TAG, "onScanResult: ", e);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.e(TAG, "onScanFailed: " + errorCode);
            }
        });
    }

    public void stopScan() {
        if (scanner != null) {
            scanner.stopScan(null);
        }
    }
}

