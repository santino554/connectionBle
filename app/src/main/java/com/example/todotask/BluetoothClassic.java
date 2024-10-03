package com.example.todotask;



import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BluetoothClassic {
    private static final String TAG = "BluetoothHelper";
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothA2dp bluetoothA2dp;
    private BluetoothDevice connectedDevice;


    public interface ConnectionCallback {
        void onConnectionStateChange(int state);
    }

    private ConnectionCallback connectionCallback;

    public BluetoothClassic(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // Get the list of paired devices
    public Set<BluetoothDevice> getPairedDevices() {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth permissions not granted.");
            return Collections.emptySet();
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted.");
            return Collections.emptySet();
        }
        return bluetoothAdapter.getBondedDevices();
    }

    // Connect to the device using Bluetooth Classic
    public void connectToDevice(BluetoothDevice device) {
        if (!hasBluetoothPermissions()) {
            Log.e(TAG, "Bluetooth permissions not granted.");
            return;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission not granted.");
            return;
        }

        // Attempt to connect using A2DP profile for audio devices
        bluetoothAdapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP);
        connectedDevice = device;



    }


    private final BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = (BluetoothA2dp) proxy;
                //BluetoothClassic bluetoothClassic = new BluetoothClassic(context);

                // Connect to the device using A2DP
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        // Check if the device is connected
                        if (bluetoothA2dp.getConnectedDevices().contains(connectedDevice)) {
                            Toast.makeText(context, "Device is connected: " +  connectedDevice.getName(), Toast.LENGTH_SHORT).show();

                            Log.i(TAG, "Device is connected: " + connectedDevice.getName());
                            int batteryPercentage = getBatteryLevel(connectedDevice);
                            Toast.makeText(context, "battery percentage " + batteryPercentage + "%", Toast.LENGTH_SHORT).show();
                            if (connectionCallback != null) {
                                connectionCallback.onConnectionStateChange(BluetoothProfile.STATE_CONNECTED);
                            }
                        }else {
                            Log.e("device conection", "Not Connected");
                            int batteryPercentage = getBatteryLevel(connectedDevice);
                            if (batteryPercentage == -1){
                                Toast.makeText(context, "Device Not connected", Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error connecting to audio device", e);
                }
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = null;
                Log.i(TAG, "A2DP service disconnected");

                if (connectionCallback != null) {
                    connectionCallback.onConnectionStateChange(BluetoothProfile.STATE_DISCONNECTED);
                }
            }
        }
    };

    public int getBatteryLevel(BluetoothDevice pairedDevice) {

        if (pairedDevice == null) {
            return -1;
        }

        try {
            Method method = pairedDevice.getClass().getMethod("getBatteryLevel");
            return (int) method.invoke(pairedDevice);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return -1;
        }
    }



    // Check if the Bluetooth permissions are granted
    private boolean hasBluetoothPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }



}

