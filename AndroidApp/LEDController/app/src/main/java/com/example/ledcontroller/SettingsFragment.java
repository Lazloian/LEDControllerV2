package com.example.ledcontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        /* Unused brightness settings, having the ability to change brightness deemed unnecessary and too dangerous, for now
        // get editTexts
        EditText brightnessText = view.findViewById(R.id.editText_brightness);

        // get textViews
        TextView errorText = view.findViewById(R.id.textView_error);

        // get button
        Button applyButton = view.findViewById(R.id.button_apply);

        // set onClickListener
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int brightness;
                try {
                    brightness = Integer.parseInt(brightnessText.getText().toString());
                } catch (NumberFormatException e)
                {
                    brightness = -1;
                }

                // check if brightness if valid
                if (brightness < 0 || brightness > 255)
                {
                    errorText.setText("Brightness Invalid");
                }
                else
                {
                    EventBus.getDefault().post(new MainMessage(MainMessage.BT_BRIGHTNESS, brightness));
                }
            }
        });
        */
        return view;
    }
}
