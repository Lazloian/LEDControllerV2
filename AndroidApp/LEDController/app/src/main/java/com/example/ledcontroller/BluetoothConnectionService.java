package com.example.ledcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionService"; // TAG for debugging

    // Info for bluetooth connection
    private static final String appName = "MYAPP";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // uuid from main activity

    private final BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    // accepts a connection
    private AcceptThread mInsecureAcceptThread;
    // connects to a device
    private ConnectThread mConnectThread;
    // handles data reading and writing for bluetooth connection
    private ConnectedThread mConnectedThread;

    // device to make a connection with
    private BluetoothDevice mmDevice;
    private  UUID deviceUUID;

    public BluetoothConnectionService(Context context) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mContext = context;
        start();
    }

    // runs on another thread until an incoming connection is started (waiting for another device to connect to this one)
    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket mmServerSocket;

        // starts the server socket
        public AcceptThread()
        {
            // create temporary socket until it is started successfully
            BluetoothServerSocket tmp = null;

            // start server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: Setting up server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            // set new socket to mmServerSocket
            mmServerSocket = tmp;
        }

        // this will run until a connection is made
        public void run()
        {
            Log.d(TAG, "run: Accept Thread Running");
            BluetoothSocket socket = null;

            // wait until a connection is made
            try {
                Log.d(TAG, "run: RFCOM server socket start");
                socket = mmServerSocket.accept();
                Log.d(TAG, "run: RFCOM server socket accepted connection");
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
            }

            if(socket != null)
            {
                connected(socket, mmDevice);
            }
            Log.i(TAG, "run: END mAcceptThread");
        }

        // closes the server socket
        public void cancel()
        {
            Log.d(TAG, "cancel: Cancelling Accept Thread");
            try{
                mmServerSocket.close();
            }catch (IOException e)
            {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed" + e.getMessage());
            }
        }

    }

    // Connects this device with another device's bluetooth socket
    private class ConnectThread extends Thread
    {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid)
        {
            Log.d(TAG, "ConnectThread: started");
            mmDevice = device; // device to connect to
            deviceUUID = uuid; // UUID for connection
        }

        // tries to connect to other device after init
        public void run()
        {
            BluetoothSocket tmp = null;
            Log.i(TAG, "run: mConnectThread");

            // create a commSocket to use to connect to the device later
            try {
                Log.d(TAG, "run: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "run: Could not create InsecureRfcommSocket" + e.getMessage());
            }
            // set socket to successful connection
            mmSocket = tmp;

            // cancel discovery just in case
            mBluetoothAdapter.cancelDiscovery();

            // try and connect to other device. If connection fails, try and close the socket
            try {
                mmSocket.connect();
                Log.d(TAG, "run: Connection Successful");
            } catch (IOException e) {
                try {
                    mmSocket.close();
                    Log.e(TAG, "run: Closed Socket");
                } catch (IOException ex) {
                    Log.e(TAG, "run: Unable to close connection in socket" + ex.getMessage());
                }
                Log.d(TAG, "run: Could not connect to UUID: " + MY_UUID_INSECURE);
                EventBus.getDefault().post(new MainMessage(MainMessage.CONNECTION_FAILURE));
            }

            // run connected to handle reading and writing
            connected(mmSocket, mmDevice);
        }

        // closes the server socket
        public void cancel()
        {
            Log.d(TAG, "cancel: Closing Client Socket");
            try{
                mmSocket.close();
            }catch (IOException e)
            {
                Log.e(TAG, "cancel: Close of ConnectThread Socket failed" + e.getMessage());
            }
        }
    }
    // starts the bluetooth connection service. This starts a connection in server mode (it waits for an incoming connection)
    public synchronized void start()
    {
        Log.d(TAG, "start");
        // cancel any thread attempting to make a connection to another bluetooth device (we want to be the server)
        if (mConnectThread != null) // original canceled it if it exists, we want to start it
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mInsecureAcceptThread == null) // change: trying to connect first instead of waiting to connect
        {
            mInsecureAcceptThread = new AcceptThread();
            mInsecureAcceptThread.start();
        }
    }

    // AcceptThread waits for the incoming connection (started above) then ConnectThread waits to make a connection with the other device
    public void startClient(BluetoothDevice device, UUID uuid)
    {
        Log.d(TAG, "startClient: Started");

        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start(); // start connection to other device
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        // sets variables above to appropriate objects
        public ConnectedThread(BluetoothSocket socket)
        {
            Log.d(TAG, "ConnectThread: Starting");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // try to get input and output streams from socket connection
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "ConnectedThread: BT Connection Successful");
            EventBus.getDefault().post(new MainMessage(MainMessage.CONNECTION_SUCCESS));

            // set data streams
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        // read data from input stream
        public void run()
        {
            byte[] buffer = new byte[1024]; // stores data for the stream
            int bytes; // number of bytes from read()

            // listens to input stream until an exception occurs
            while (true)
            {
                // Read data from input stream
                try {
                    bytes = mmInStream.read(buffer);
                    // takes the first (bytes) number of bytes from the buffer and converts it to a string with 0 offset
                    String incomingMessage = new String(buffer, 0, bytes);
                    //Log.d(TAG, "InputStream: " + incomingMessage);

                    // Send incomingMessage to MainActivity through EventBus
                    EventBus.getDefault().post(new BTMessage(incomingMessage));

                } catch (IOException e) {
                    Log.d(TAG, "run: Error reading input stream: " + e.getMessage());
                    // stop listening to input stream
                    break; // REEEEEEEEEEEEEEEEEEEEE NO BREAK STATEMENT REEEEEEEEEEEEEEEE
                }
            }
        }

        // write a buffer to the output steam
        public void write(byte[] buffer)
        {
            String text = new String(buffer, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to output stream: " + text);

            try{
                mmOutStream.write(buffer);
            }catch (IOException e){
                Log.d(TAG, "write: Write failed: " + text);
                Log.e(TAG, "write: Error writing to output stream: " + e.getMessage());
            }
        }

        public void cancel()
        {
            try {
                Log.d(TAG, "cancel: Closing Connected Thread");
                mmSocket.close();
            }catch (IOException e){
                Log.d(TAG, "cancel: Closing of Connected Thread Failed");
            }
        }
    }

    // starts the connectedThread with the socket and device
    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice)
    {
        Log.d(TAG, "connected: Starting");

        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    // write method that can be accessed from main activity
    public void write(byte[] out)
    {
        Log.d(TAG, "write: Write Called");
        mConnectedThread.write(out);
    }
}
