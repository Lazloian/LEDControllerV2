package com.example.ledcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

import java.util.UUID;

/* TODO:
    Make one button connection
    Toast confirmations for connected and debug
    Make new eventbus for BT state changes (like connection successful because only BTConnectionService will know)
    Might change FragMessage to something else that can do above
*/
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // tag for console logging
    private FrameLayout fragmentContainer; // holds fragments

    private final int btWaitTime = 10000; // the amount of time to search for esp32

    // bluetooth objects
    BluetoothAdapter mBluetoothAdapter;
    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;

    // UUID for communication with esp32, uuid in android example will not work
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


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
                            Log.d(TAG, "onNavigationItemSelected: Colors Fragment");
                            selectedFragment = new ColorsFragment();
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

    public void ConnectBT()
    {
        Log.d(TAG, "ConnectBT: Looking for LEDController");

        // cancel discovery and restart it
        mBluetoothAdapter.cancelDiscovery();

        // check permissions (this needs to be done even though it is in manifest)
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0)
        {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }

        // start discovery and register receiver to get found devices (the receiver will pair to the esp32)
        mBluetoothAdapter.startDiscovery();
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btDeviceReceiver, discoverDevicesIntent);

        // if timer finishes and LEDController not found, discover is disabled
        CountDownTimer btWaitTimer = new CountDownTimer(btWaitTime, btWaitTime)
        {
            public void onTick(long millisUntilFinished)
            {
            }

            public void onFinish()
            {
                if (mBTDevice == null)
                {
                    mBluetoothAdapter.cancelDiscovery();
                    unregisterReceiver(btDeviceReceiver);
                    Log.d(TAG, "ConnectBT: LEDController not found, disabling discover");
                }
            }
        }.start();
    }

    // EventBus subscribers
    @Subscribe
    public void onFragMessageReceive(FragMessage fragMessage)
    {
        // get message
        final String message = fragMessage.getMessage();
        Log.d(TAG, "Message Received From Fragment: " + message);

        // do something depending on the message
        switch (message)
        {
            case "ConnectBT":
                ConnectBT();
        }
    }

    @Subscribe
    public void onBTMessageReceive(BTMessage btMessage)
    {
        Log.d(TAG, "Message Received From BT: " + btMessage.getMessage());
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






















