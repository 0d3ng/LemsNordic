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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.SingleValueDataSet;
import com.anychart.charts.CircularGauge;
import com.anychart.enums.Anchor;
import com.anychart.graphics.vector.text.HAlign;

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
    private AnyChartView anyChartView;

    private CircularGauge circularGauge;

    private MyBleViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusTextView = findViewById(R.id.statusTextView);
        anyChartView = findViewById(R.id.any_chart_view);

        viewModel = new ViewModelProvider(this).get(MyBleViewModel.class);
        viewModel.getReceivedValue().observe(this, s -> {
            if (s != null) {
                Log.i(TAG, "onChanged: " + s);
            }
        });

        createGauge();

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

    private void createGauge() {
        circularGauge = AnyChart.circular();
        circularGauge.fill("#fff")
                .stroke(null)
                .padding(0, 0, 0, 0)
                .margin(30, 30, 30, 30);
        circularGauge.startAngle(0)
                .sweepAngle(360);

        double currentValue = 13.8D;
        circularGauge.data(new SingleValueDataSet(new Double[]{currentValue}));

        circularGauge.axis(0)
                .startAngle(-150)
                .radius(80)
                .sweepAngle(300)
                .width(3)
                .ticks("{ type: 'line', length: 4, position: 'outside' }");

        circularGauge.axis(0).labels().position("outside");

        circularGauge.axis(0).scale()
                .minimum(0)
                .maximum(140);

        circularGauge.axis(0).scale()
                .ticks("{interval: 10}")
                .minorTicks("{interval: 10}");

        circularGauge.needle(0)
                .stroke(null)
                .startRadius("6%")
                .endRadius("38%")
                .startWidth("2%")
                .endWidth(0);

        circularGauge.cap()
                .radius("4%")
                .enabled(true)
                .stroke(null);

        circularGauge.label(0)
                .text("<span style=\"font-size: 25\">CO2</span>")
                .useHtml(true)
                .hAlign(HAlign.CENTER);
        circularGauge.label(0)
                .anchor(Anchor.CENTER_TOP)
                .offsetY(100)
                .padding(15, 20, 0, 0);

        circularGauge.label(1)
                .text("<span style=\"font-size: 20\">" + currentValue + "</span>")
                .useHtml(true)
                .hAlign(HAlign.CENTER);
        circularGauge.label(1)
                .anchor(Anchor.CENTER_TOP)
                .offsetY(-100)
                .padding(5, 10, 0, 0)
                .background("{fill: 'none', stroke: '#c1c1c1', corners: 3, cornerType: 'ROUND'}");

        circularGauge.range(0,
                "{\n" +
                        "    from: 0,\n" +
                        "    to: 25,\n" +
                        "    position: 'inside',\n" +
                        "    fill: 'green 0.5',\n" +
                        "    stroke: '1 #000',\n" +
                        "    startSize: 6,\n" +
                        "    endSize: 6,\n" +
                        "    radius: 80,\n" +
                        "    zIndex: 1\n" +
                        "  }");

        circularGauge.range(1,
                "{\n" +
                        "    from: 80,\n" +
                        "    to: 140,\n" +
                        "    position: 'inside',\n" +
                        "    fill: 'red 0.5',\n" +
                        "    stroke: '1 #000',\n" +
                        "    startSize: 6,\n" +
                        "    endSize: 6,\n" +
                        "    radius: 80,\n" +
                        "    zIndex: 1\n" +
                        "  }");

        anyChartView.setChart(circularGauge);
    }
}