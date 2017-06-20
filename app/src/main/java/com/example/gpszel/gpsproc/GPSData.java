package com.example.gpszel.gpsproc;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.gpszel.gpsproc.Observer;
import com.example.gpszel.gpsproc.Subject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class GPSData  {
    private Thread myThreadConnectBTdevice;
    Handler gpsHandler;
    final int handlerState = 0;
    final int BT_CONNECT_SUCCESS = 1;
    final int BT_READ = 2;

    private int numberAvailableSatellites;
    private int numberConnectedSatellite;
    private int connectStatus;

    /*    private ArrayList observers;
    private Thread btThread = new ConnectedThread(btSocket)
*/
    Context context;
    public GPSData(Context context){
        this.context = context;
//        observers = new ArrayList();
//        btThread = new Thread()
        gpsHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == BT_CONNECT_SUCCESS) {                                     //if message is what we want
                    Log.d("seagull", "CONNECT_SUCCESS");
                }
                if (msg.what == BT_READ) {
                    String readMessage = (String) msg.obj;                                // msg.arg1 = bytes from connect thread
                    Log.d( "seagull", "read: " + readMessage);
//                    recDataString.append(readMessage);                                      //keep appending to string until ~
//                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
//                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
//                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
//                        txtString.setText("Data Received = " + dataInPrint);
//                        int dataLength = dataInPrint.length();                          //get length of data received
//                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));
//
//                        if (recDataString.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
//                        {
//                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
//                            String sensor1 = recDataString.substring(6, 10);            //same again...
//                            String sensor2 = recDataString.substring(11, 15);
//                            String sensor3 = recDataString.substring(16, 20);
//
//                            sensorView0.setText(" Sensor 0 Voltage = " + sensor0 + "V");    //update the textviews with sensor values
//                            sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V");
//                            sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
//                            sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
//                        }
//                        recDataString.delete(0, recDataString.length());                    //clear all string data
//                        // strIncom =" ";
//                        dataInPrint = " ";
//                    }
                }
            }
        };

    }

    public void connect(BluetoothDevice device) {
        Toast toast = Toast.makeText(context,device.getName(),Toast.LENGTH_LONG);
        toast.show();
        myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
        myThreadConnectBTdevice.start();
    }
/*
    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        if(observers.indexOf(o)>=0) observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for(int i=0;i<observers.size();i++){
            Observer observer = (Observer) observers.get(i);
            observer.update();
        }

    } */
    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    class ThreadConnectBTdevice extends Thread {
    private String dataBuffer;
    private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;
        private final String UUID_STRING_WELL_KNOWN_SPP =
                "00001101-0000-1000-8000-00805F9B34FB";
        private UUID myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);;

        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                //Log.d("seagull","bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                Log.d("seagull","connecting...");
                bluetoothSocket.connect();
                success = true;
                Log.d("seagull","connect success !!!");
                // Send the obtained bytes to the UI Activity via handler
                gpsHandler.obtainMessage(BT_CONNECT_SUCCESS).sendToTarget();

                InputStream mmInStream;
                OutputStream mmOutStream;
                try {
                    //Create I/O streams for connection
                    mmInStream = bluetoothSocket.getInputStream();
                    mmOutStream = bluetoothSocket.getOutputStream();
                    byte[] buffer = new byte[256];
                    int bytes;

                    // Keep looping to listen for received messages
                    while (true) {
                        try {
                            bytes = mmInStream.read(buffer);            //read bytes from input buffer
                            String readMessage = new String(buffer, 0, bytes);
                            dataBuffer = dataBuffer + readMessage;
                            int i = dataBuffer.indexOf("\r\n");
                            if(i>=0) {
                                // Send the obtained bytes to the UI Activity via handler
                                if(checkSum(dataBuffer.substring(0,i))) gpsHandler.obtainMessage(BT_READ, bytes, -1, dataBuffer.substring(0,i)).sendToTarget();
                                dataBuffer = dataBuffer.substring(i+2);
                            }
                        } catch (IOException e) {
                            break;
                        }
                    }
                    Log.d("seagull","read end");
                } catch (IOException e) {
                    Log.d("seagull","read failed !!! " + e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("seagull","connect failed !!! " + e.getMessage());
                final String eMessage = e.getMessage();
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        textStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage);
//                    }
//                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;

//                runOnUiThread(new Runnable(){
//
//                    @Override
//                    public void run() {
//                        textStatus.setText(msgconnected);
//
//                        listViewPairedDevice.setVisibility(View.GONE);
//                        inputPane.setVisibility(View.VISIBLE);
//                    }});

//                startThreadConnected(bluetoothSocket);
            }else{
                //fail
            }
        }

        public void cancel() {

//            Toast.makeText(getApplicationContext(),
//                    "close bluetoothSocket",
//                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        boolean checkSum(String theseChars) {
            int i;
            String str;
            if(!((i=theseChars.indexOf("*"))>=0)) return false;
            else {
                str = theseChars.substring(1,i);
                char check = 0;
                for (int c = 0; c < str.length(); c++) check ^= str.charAt(c);
                String checkSum = Integer.toHexString(check).toUpperCase();
                Log.d("seagull",checkSum+" : "+ theseChars.substring(i+1));
                if(checkSum.equals(theseChars.substring(i+1)))return true;
            }
            return false;
        }
    }

}


