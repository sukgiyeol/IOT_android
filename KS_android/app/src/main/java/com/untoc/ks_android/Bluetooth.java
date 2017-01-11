package com.untoc.ks_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private final static int DEVICES_DIALOG = 1;
    private final static int ERROR_DIALOG = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    static BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
    private OutputStream outputStream;
    private InputStream inputStream;

    volatile boolean stopWorker;
    int readBufferPosition;
    Thread workerThread;
    byte[] readBuffer;

    public static Context mContext;
    public static AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = this;
        activity = this;

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            finish();
            return;
        }

        if (bluetoothAdapter == null) {
            ErrorDialog("This device is not implement Bluetooth.");
        }

        if(bluetoothAdapter.isEnabled()){
            DeviceDialog();
        }
    }
    static public Set<BluetoothDevice> getPairedDevices() {
        return bluetoothAdapter.getBondedDevices();
    }

    public void doConnect(BluetoothDevice device){
        bluetoothDevice = device;
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try{
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothAdapter.cancelDiscovery();
            new ConnectTask().execute();
        }catch (IOException e) {
            Log.e("", e.toString(), e);
        }
    }

    private class ConnectTask extends AsyncTask<Void, Void, Object> {
        @Override
        protected Object doInBackground(Void... params) {
            try{
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
            } catch (Throwable t) {
                Log.e("", "connect?"+t.getMessage());
                return t;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result){
            if(result instanceof Throwable){
                Log.d("", "ConnectTask "+result.toString());
                ErrorDialog("ConnectTask "+result.toString());
            }
        }
    }

    public void DeviceDialog() {
        if (isFinishing()) return;
        FragmentManager fm = Bluetooth.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(DEVICES_DIALOG, "");
        alertDialog.show(fm, "");
    }


    public void ErrorDialog(String text){
        if (activity.isFinishing()) return;
        FragmentManager fm = Bluetooth.this.getSupportFragmentManager();
        MyDialogFragment alertDialog = MyDialogFragment.newInstance(ERROR_DIALOG, text);
        alertDialog.show(fm, "");
    }

    void sendData() throws IOException {
        String msg = "a";
        outputStream.write(msg.getBytes());
    }

    void beginListenForData() {
        final Handler handler = new Handler(Looper.getMainLooper());

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable(){
            public void run(){
                while(!Thread.currentThread().isInterrupted() && !stopWorker){
                    try{
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0){
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for(int i=0; i<bytesAvailable; i++){
                                byte b = packetBytes[i];
                                if(b == '\n'){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");

                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run()
                                        {
                                        }
                                    });
                                }else{
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex){
                        stopWorker = true;
                    }
                }
            }
        });
        workerThread.start();
    }
}


