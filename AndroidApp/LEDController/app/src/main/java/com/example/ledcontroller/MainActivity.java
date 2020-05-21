package com.example.ledcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // tag for console logging
    private FrameLayout fragmentContainer; // holds fragments


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

        // create an IntentFilter to catch intents from fragments, intents will be named by their destination
        IntentFilter fragFilter = new IntentFilter("LEDController.MainActivity");

        // register a broadcastReceiver to manage the incoming messages from the fragments
        registerReceiver(fragmentReceiver, fragFilter);
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
                            selectedFragment = new SendFragment();
                            break;
                        case R.id.nav_colors:
                            selectedFragment = new ColorsFragment();
                            break;
                        case R.id.nav_settings:
                            selectedFragment = new SettingsFragment();
                            break;
                    }

                    // replace current fragment in view to the new fragment
                    getSupportFragmentManager().beginTransaction().replace(fragmentContainer.getId(), selectedFragment).commit();

                    return true;
                }
            };

    // BroadcastReceiver for receiving messages from fragments
    private BroadcastReceiver fragmentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // get message from intent
            String message = intent.getStringExtra("message");

            Log.d(TAG, "fragmentReceiver Incoming Message: " + message);
        }
    };
}
