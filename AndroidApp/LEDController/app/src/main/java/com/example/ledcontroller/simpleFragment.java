package com.example.ledcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ledcontroller.PatternActivity;
import com.example.ledcontroller.PatternMessage;
import com.example.ledcontroller.R;

import org.greenrobot.eventbus.EventBus;

public class simpleFragment extends Fragment {

    private int hueBarMax = 360;
    private int hueBarStep = 12;

    private SeekBar hueBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple, container, false);

        // get seekBar and set its max
        hueBar = view.findViewById(R.id.seekBar_hue);
        hueBar.setMax(hueBarMax / hueBarStep);

        // box to preview color
        TextView color = view.findViewById(R.id.textView_color);

        // buttons
        Button doneButton = view.findViewById(R.id.button_done);
        Button cancelButton = view.findViewById(R.id.button_cancel);

        // seekBar listener
        hueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float[] hsv = {progress * hueBarStep, 1, 1};
                color.setBackgroundColor(Color.HSVToColor(hsv));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] code = new byte[2];
                code[0] = 'h';
                code[1] = (byte) (Math.round(((float) (hueBar.getProgress() * hueBarStep) / 360) * 225) - 128);

                EventBus.getDefault().post(new PatternMessage(code));
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create new code that it filled with zero by default
                byte[] code = new byte[1];
                EventBus.getDefault().post(new PatternMessage(code));
            }
        });


        return view;
    }
}
