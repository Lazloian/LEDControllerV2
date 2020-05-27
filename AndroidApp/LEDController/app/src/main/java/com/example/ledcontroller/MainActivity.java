package com.example.ledcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.Charset;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // tag for console logging
    private FrameLayout fragmentContainer; // holds fragments

    private final int btWaitTime = 10000; // the amount of time to search for esp32
    private boolean isConnected = false; // keeps track of the connection state of the bluetooth device

    // bluetooth objects
    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;

    // UUID for communication with esp32, uuid in android example will not work
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // colorPattern database
    private PatternViewModel mPatternViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // references to ui elements
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);

        // set method to run when an item is selected on the bottom navigation view
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // show the send fragment on app startup
        getSupportFragmentManager().beginTransaction().replace(fragmentContainer.getId(), new SendFragment()).commit();

        // get bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // create patternViewModel
        mPatternViewModel = new ViewModelProvider(this).get(PatternViewModel.class);
    }

    // runs when app is killed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btStateReceiver);
        unregisterReceiver(btDeviceReceiver);
        unregisterReceiver(btBondStateReceiver);
    }

    // enables EventBus communication when app is running
    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        // if the bluetooth device was previously connected, check if it is still responding
        if (isConnected)
        {
            checkBTConnection();
        }
    }

    // disables EventBus communication when app is paused (in background)
    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    // switches the active fragment when it is selected in the bottom navigation view
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null; // fragment that will be opened

                    // get id of selected item and create fragment that is selected
                    switch (item.getItemId())
                    {
                        case R.id.nav_send:
                            Log.d(TAG, "onNavigationItemSelected: Send Fragment");
                            selectedFragment = new SendFragment();
                            break;
                        case R.id.nav_colors:
                            Log.d(TAG, "onNavigationItemSelected: Patterns Fragment");
                            selectedFragment = new PatternsFragment();
                            break;
                        case R.id.nav_settings:
                            Log.d(TAG, "onNavigationItemSelected: Settings Fragment");
                            selectedFragment = new SettingsFragment();
                            break;
                    }

                    // replace current fragment in view to the new fragment
                    getSupportFragmentManager().beginTransaction().replace(fragmentContainer.getId(), selectedFragment).commit();

                    return true;
                }
            };

    // bluetooth methods

    // enables bluetooth
    public void enableBT()
    {
        // request BT enable
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBTIntent);

        // track changes to BT state
        IntentFilter BTIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btStateReceiver, BTIntentFilter);
    }

    // connects the phone to the esp32
    public void ConnectBT()
    {
        // check if the bluetooth device is already connected
        if (isConnected && mBTDevice.getBondState() == BluetoothDevice.BOND_BONDED)
        {
            Toast.makeText(this, "Device Already Connected", Toast.LENGTH_SHORT).show();
        }
        else {
            // if BT is not enabled, enable it
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d(TAG, "ConnectBT: Enabling Bluetooth");
                enableBT();
            }

            // wait till Bluetooth is enabled
            while (!mBluetoothAdapter.isEnabled()) {
            }

            Log.d(TAG, "ConnectBT: Looking for LEDController");
            Toast.makeText(this, "Connecting ...", Toast.LENGTH_SHORT).show();

            // cancel discovery and restart it
            mBluetoothAdapter.cancelDiscovery();

            // check permissions (this needs to be done even though it is in manifest)
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }

            // start discovery and register receiver to get found devices (the receiver will pair to the esp32)
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(btDeviceReceiver, discoverDevicesIntent);

            // if timer finishes and LEDController not found, discover is disabled
            CountDownTimer btWaitTimer = new CountDownTimer(btWaitTime, btWaitTime) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    if (mBTDevice == null) {
                        mBluetoothAdapter.cancelDiscovery();
                        //unregisterReceiver(btDeviceReceiver);
                        Log.d(TAG, "ConnectBT: LEDController not found, disabling discover");
                        EventBus.getDefault().post(new MainMessage(MainMessage.CONNECTION_FAILURE));
                    }
                }
            }.start();
        }
    }

    // checks if the esp32 is still responding by sending a connection message
    public void checkBTConnection()
    {
        isConnected = false;
        mBluetoothConnection.write("Connection".getBytes(Charset.defaultCharset()));
    }

    // EventBus subscribers
    @Subscribe
    public void onFragMessageReceive(FragMessage fragMessage)
    {
        // get message
        final String message = fragMessage.getMessage();
        Log.d(TAG, "Message Received From Fragment: " + message);
    }

    // this runs on the MainActivity thread so ui changes can be made (like displaying toasts)
    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onMainMessageReceive(MainMessage mainMessage)
    {
        switch (mainMessage.getMessage())
        {
            case MainMessage.BT_CONNECT:
                ConnectBT();
                break;
            case MainMessage.CONNECTION_SUCCESS:
                Toast.makeText(this, "Connection Successful", Toast.LENGTH_SHORT).show();
                isConnected = true;
                break;
            case MainMessage.CONNECTION_FAILURE:
                Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
                isConnected = false;
                break;
        }
    }

    @Subscribe
    public void onBTMessageReceive(BTMessage btMessage)
    {
        char message = btMessage.getMessage();
        Log.d(TAG, "Message Received From BT: " + message);

        // if 'y' is received, then the esp32 is still connected
        switch (message)
        {
            case 'y':
                isConnected = true;
                Log.d(TAG, "onBTMessageReceive: Device still connected");
                break;
            case 'd':
                Log.d(TAG, "onBTMessageReceive: Send Successful");
                break;
        }
    }

    // Broadcast Receivers
    // receives changes to bluetooth state (on / off, ect)
    private BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get the action
            final String action = intent.getAction();

            // check if it wants to change the state of bluetooth
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
            {
                // get the current state
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                // log current state
                switch (state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    // receives BT devices found during discovery and connects to it if it is the LEDController
    private BroadcastReceiver btDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get action
            final String action = intent.getAction();

            // check if the action found a device
            if (action.equals(BluetoothDevice.ACTION_FOUND))
            {
                // get the found device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // check if device has a name
                if (device.getName() != null)
                {
                    // get the name
                    String deviceName = device.getName();
                    Log.d(TAG, "onReceive: Device found with name: " + deviceName);

                    // check if the device is the LEDController, if it is connect to it
                    if (deviceName.equals("LEDController")) {
                        // cancel discovery
                        mBluetoothAdapter.cancelDiscovery();
                        Log.d(TAG, "onReceive: LEDController Found");

                        // add device and pair it
                        mBTDevice = device;
                        mBTDevice.createBond();

                        // start new BTConnectionService (this will open a BT socket and wait for a connection)
                        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

                        // connect to the esp32
                        mBluetoothConnection.startClient(mBTDevice, MY_UUID_INSECURE);
                    }
                }
            }
        }
    };

    // receives bonding state changes to other bluetooth devices
    private BroadcastReceiver btBondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get action
            final String action = intent.getAction();

            // check if action changed bond state
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                // get device and name
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();

                // log bond state
                switch (device.getBondState())
                {
                    case BluetoothDevice.BOND_BONDED:
                        Log.d(TAG, "onReceive: BOND_BONDED to " + deviceName);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d(TAG, "onReceive: BOND_BONDING to " + deviceName);
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d(TAG, "onReceive: BOND_NONE to " + deviceName);
                        break;
                }
            }
        }
    };
}






















