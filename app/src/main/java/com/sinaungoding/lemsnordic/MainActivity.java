package com.sinaungoding.lemsnordic;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private MyBleManager bleManager;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private final List<BluetoothDevice> scannedDevices = new ArrayList<>();
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = findViewById(R.id.statusTextView);
        bleManager = new MyBleManager(this);
        if (hasPermissions()) {
            Log.d(TAG, "Permissions granted. Starting scan...");
            startScan();
        } else {
            Log.d(TAG, "Permissions not granted. Requesting permissions...");
            requestPermissions();
        }
    }

    private void startScan() {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        Log.d(TAG, "Starting BLE scan...");
        scanner.startScan(null, settings, scanCallback);
        updateStatus("Scanning for BLE devices...");
    }

    private void stopScan() {
        BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
        updateStatus("Scan stopped.");
    }

    private void connectToDevice(BluetoothDevice device) {
        try {
            Log.d(TAG, "Connecting to device: " + device.getName() + " [" + device.getAddress() + "]");
            updateStatus("Connecting to " + device.getName() + "...");
            bleManager.connect(device)
                    .retry(3, 100)
                    .done(connectedDevice -> {
                        Log.d(TAG, "Connected to device: " + connectedDevice.getName());
                        updateStatus("Connected to " + connectedDevice.getName());
                    })
                    .fail((failedDevice, status) -> {
                        Log.e(TAG, "Connection failed to device: " + failedDevice.getName() + ", status: " + status);
                        updateStatus("Connection failed");
                    })
                    .enqueue();
        } catch (SecurityException e) {
            Log.e(TAG, "connectToDevice: ", e);
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            try {
                BluetoothDevice device = result.getDevice();
                if (!scannedDevices.contains(device)) {
                    scannedDevices.add(device);
                    Log.d(TAG, "Found device: " + device.getName() + " [" + device.getAddress() + "]");
                    updateStatus("Found device: " + device.getName() + " [" + device.getAddress() + "]");

                    if (device.getAddress().equals("E0:37:8B:3C:4E:7B")) {
                        connectToDevice(device);
                        stopScan();
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "onScanResult: ", e);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            updateStatus("Scan failed with error: " + errorCode);
            if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                stopScan();
                startScan();
            }
        }
    };

    private boolean hasPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> {
            statusTextView.setText(message);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissions granted. Starting scan...");
                startScan();
            } else {
                Log.e(TAG, "Permissions denied.");
                updateStatus("Permissions denied.");
            }
        }
    }
}