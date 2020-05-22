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
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    @Subscribe
    public void onMessageReceive(FragMessage fragMessage)
    {
        Log.d(TAG, "Message Received From Fragment: " + fragMessage.getMessage());
        Toast.makeText(this, fragMessage.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
