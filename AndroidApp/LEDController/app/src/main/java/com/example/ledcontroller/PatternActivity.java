package com.example.ledcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PatternActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "PatternActivity";

    // key for name of pattern
    public static final String EXTRA_NAME = "PATTERN_NAME";

    // key for code of pattern
    public static final String EXTRA_CODE = "PATTERN_CODE";

    private String patternName;
    private byte[] patternCode;

    private FrameLayout fragmentContainer;
    private EditText nameText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        // spinner
        Spinner typeSpinner = findViewById(R.id.spinner_type);

        // set up simple spinner adapter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.types_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // set adapter to spinner and set itemSelectedListener
        typeSpinner.setAdapter(spinnerAdapter);
        typeSpinner.setOnItemSelectedListener(this);

        // fragmentContainer
        fragmentContainer = findViewById(R.id.frameLayout);
        getSupportFragmentManager().beginTransaction().replace(fragmentContainer.getId(), new simpleFragment()).commit();

        // EditText for the name
        nameText = findViewById(R.id.editText_name);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // fragment that will be shown
        Fragment selected = null;

        switch (position)
        {
            case 0: // Simple
                selected = new simpleFragment();
        }
        // show new fragment
        getSupportFragmentManager().beginTransaction().replace(fragmentContainer.getId(), selected).commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
    // Subscribers
    @Subscribe
    public void onPatternReceive(PatternMessage patternMessage)
    {
        byte[] code = patternMessage.getCode();
        Intent resultIntent = new Intent();

        if (code[0] == 0)
        {
            Log.d(TAG, "onPatternReceive: Cancelling Pattern");
            setResult(RESULT_CANCELED, resultIntent);
        }
        else
        {
            Log.d(TAG, "onPatternReceive: Saving Pattern: " + nameText.getText().toString());
            resultIntent.putExtra(EXTRA_NAME, nameText.getText().toString());
            resultIntent.putExtra(EXTRA_CODE, code);
            setResult(RESULT_OK, resultIntent);
        }

        finish();
    }
}
