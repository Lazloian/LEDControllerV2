package com.example.ledcontroller;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ledcontroller.PatternActivity;
import com.example.ledcontroller.PatternMessage;
import com.example.ledcontroller.R;

import org.greenrobot.eventbus.EventBus;

public class SimpleFragment extends Fragment {
    private static final String TAG = "simpleFragment";

    private int hueBarMax = 360;
    private int hueBarStep = 12;

    // keeps track of which color is being selected
    private int colorSet = 1;

    // colors selected
    private int[] colors = new int[3];

    private SeekBar hueBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple, container, false);

        // get seekBar and set its max
        hueBar = view.findViewById(R.id.seekBar_hue);
        hueBar.setMax(hueBarMax / hueBarStep);

        // box to preview color and set colors
        TextView[] colorViews = new TextView[4];
        colorViews[0] = view.findViewById(R.id.textView_colorPreview);
        colorViews[1] = view.findViewById(R.id.textView_colorSet1);
        colorViews[2] = view.findViewById(R.id.textView_colorSet2);
        colorViews[3] = view.findViewById(R.id.textView_colorSet3);

        // set default to each of them to none. enhanced for loops are kind of epic
        for (TextView preview: colorViews)
        {
            preview.setBackgroundColor(Color.GRAY);
            preview.setText("None");
        }

        // buttons
        Button doneButton = view.findViewById(R.id.button_done);
        Button cancelButton = view.findViewById(R.id.button_cancel);
        Button setButton = view.findViewById(R.id.button_set);

        // editTexts for length and speed
        EditText lengthText = view.findViewById(R.id.editText_length);
        EditText speedText = view.findViewById(R.id.editText_speed);

        int edit = this.getArguments().getInt("edit"); // get edit number

        if (edit == 1) // check if editing
        {
            // get color pattern and code
            ColorPattern pattern = this.getArguments().getParcelable("pattern");
            byte[] code = pattern.getCode();

            // set editTexts
            lengthText.setText(Integer.toString(fromByte(code[1])));
            speedText.setText(Integer.toString(fromByte(code[2])));

            // set colors and color previews
            for (int i = 0; i < fromByte(code[3]); i++)
            {
                colors[i] = Math.round(((float) fromByte(code[4 + i]) / 255) * ((float) hueBarMax / hueBarStep));
                setPreview(colorViews[1 + i], Math.round(((float) fromByte(code[4 + i]) / 255) * ((float) hueBarMax / hueBarStep)));
            }
        }

        // seekBar listener
        hueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                // set the color of the preview box
                setPreview(colorViews[0], progress);
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
                int num_colors = 0;
                int length = -1;
                int speed = -1;

                try {
                    length = Integer.parseInt(lengthText.getText().toString());
                    speed = Integer.parseInt(speedText.getText().toString());
                }
                catch (NumberFormatException e)
                {
                    // in case anything fucky happens when it tries (it probably wont)
                    length = -1;
                    speed = -1;
                }

                // check if inputs are within valid range
                if (length < 0 || speed < 0 || length > 255 || speed > 255)
                {
                    Log.d(TAG, "onDoneClick: Input invalid");
                    EventBus.getDefault().post(new PatternMessage(null, PatternMessage.ERROR_NUM_INVALID));
                }
                else {
                    // check if at least one color is picked
                    for (int color : colors) {
                        if (color != 0)
                            num_colors++;
                    }

                    if (num_colors == 0) {
                        EventBus.getDefault().post(new PatternMessage(null, PatternMessage.ERROR_COLOR_MISSING));
                    }
                    else {
                        // finally get to creating the pattern code and sending it

                        byte[] code = new byte[4 + num_colors]; // VLA REEEEEEEEEEEEEEEEE
                        // 's' is the symbol for simple pattern
                        code[0] = 's';
                        // next is the length of each color
                        code[1] = toByte(length);
                        // next is the speed of the flashing
                        code[2] = toByte(speed);
                        // next byte is the number of colors
                        code[3] = toByte(num_colors);
                        // place non zero colors into code
                        int color_counter = 1;
                        for (int color : colors) {
                            if (color != 0) {
                                // set the color in code. Adjust to a number in the range of -128 - 127
                                code[3 + color_counter] = toByte(Math.round(((float) (color * hueBarStep) / hueBarMax) * 255));
                                color_counter++;
                            }
                        }

                        // send the color code to the patternActivity
                        EventBus.getDefault().post(new PatternMessage(code, PatternMessage.ERROR_NONE));
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // just pass the error message for cancel
                EventBus.getDefault().post(new PatternMessage(null, PatternMessage.ERROR_CANCEL));
            }
        });

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int progress = hueBar.getProgress();
                float[] hsv = {progress * hueBarStep, 1, 1};

                colors[colorSet - 1] = progress;
                setPreview(colorViews[colorSet], progress);

                if (colorSet >= 3)
                {
                    colorSet = 1;
                }
                else
                {
                    colorSet++;
                }
            }
        });


        return view;
    }

    private void setPreview(TextView preview, int progress)
    {
        Log.d(TAG, "setPreview: progress: " + progress);
        if (progress == 0)
        {
            preview.setBackgroundColor(Color.GRAY);
            preview.setText("None");
        }
        else if (progress == hueBarMax / hueBarStep)
        {
            preview.setBackgroundColor(Color.LTGRAY);
            preview.setText("White");
        }
        else {
            float[] hsv = {progress * hueBarStep, 1, 1};
            // set background of color preview and hue code
            preview.setBackgroundColor(Color.HSVToColor(hsv));
            preview.setText(Integer.toString(progress * hueBarStep));
        }
    }

    private byte toByte (int number)
    {
        if (number > 255 || number < 0)
        {
            Log.d(TAG, "toByte: WARNING number input is not in the range of 0 - 255");
        }
        return (byte) (number - 128);
    }

    private int fromByte (byte byteNum)
    {
        int number = byteNum;
        Log.d(TAG, "fromByte: " + (number + 128));
        return  number + 128;
    }
}
