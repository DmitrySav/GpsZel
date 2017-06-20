package com.example.gpszel.main;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class MainBluetooth {

    BluetoothAdapter mBluetoothAdapter;
    private void checkBTState(Context context){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null){
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableBtIntent, 1);
            }

        } else {
            Toast.makeText(context,"Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }


}
