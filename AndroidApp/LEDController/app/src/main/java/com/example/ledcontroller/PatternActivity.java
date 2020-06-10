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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
        getSupportFragmentManager().beginTransaction().replace(fragmentContainer.getId(), new SimpleFragment()).commit();

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
                selected = new SimpleFragment();
                break;
            case 1:
                selected = new FadeFragment();
                break;
            case 2:
                selected = new BannerFragment();
                break;
            case 3:
                selected = new FlowFragment();
                break;
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
    // receives the pattern code from the respective pattern thread
    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onPatternReceive(PatternMessage patternMessage)
    {
        // checks for errors first
        if (patternMessage.getError() == PatternMessage.ERROR_COLOR_MISSING)
        {
            Toast.makeText(this, "Pick A Color", Toast.LENGTH_SHORT).show();
        }
        else if (patternMessage.getError() == PatternMessage.ERROR_NUM_INVALID)
        {
            Toast.makeText(this, "Number Input Missing Or Invalid", Toast.LENGTH_SHORT).show();
        }
        else if (patternMessage.getError() != PatternMessage.ERROR_CANCEL && nameText.getText().toString().equals(""))
        {
            Toast.makeText(this, "Input A Name", Toast.LENGTH_SHORT).show();
        }
        // if no errors or cancel, finish the activity
        else {
            // create intent and get pattern code and name
            Intent resultIntent = new Intent();
            patternName = nameText.getText().toString();
            patternCode = patternMessage.getCode();

            // don't send any extras if it is cancelled
            if (patternMessage.getError() == PatternMessage.ERROR_CANCEL) {
                Log.d(TAG, "onPatternReceive: Cancelling New Pattern");
                setResult(RESULT_CANCELED, resultIntent);
            }
            // send code and name if done is pressed
            else
            {
                Log.d(TAG, "onPatternReceive: Saving Pattern " + patternName);
                resultIntent.putExtra(EXTRA_NAME, patternName);
                resultIntent.putExtra(EXTRA_CODE, patternCode);
                setResult(RESULT_OK, resultIntent);
            }

            finish();
        }
    }
}
