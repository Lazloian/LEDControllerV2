package com.example.ledcontroller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;

public class SendFragment extends Fragment {

    private static final String TAG = "SendFragment";
    private Button connectButton;

    @Nullable
    @Override
    // create button and other graphical references here (this method is run after all the graphical elements have been loaded)
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        // reference graphical elements
        connectButton = view.findViewById(R.id.button_connect);

        // connectButton listener, sends a request to connect to the esp32 ledController
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "ConnectBL";
                sendMessage(message);
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // sends a message using EventBus
    private void sendMessage(String message)
    {
        Log.d(TAG, "Sending Message from SendFragment: " + message);
        EventBus.getDefault().post(new FragMessage(message));
    }


}
