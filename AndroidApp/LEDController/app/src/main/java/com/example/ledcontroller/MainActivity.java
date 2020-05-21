package com.example.ledcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // reference to bottom navigation view
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // set method to run when an item is selected on the bottom navigation view
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        // show the send fragment on app startup
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SendFragment()).commit();
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
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                    return true;
                }
            };
}
