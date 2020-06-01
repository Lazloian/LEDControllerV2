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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class SendFragment extends Fragment implements PatternListAdapter.OnPatternListener  {

    private static final String TAG = "SendFragment";

    @Nullable
    @Override
    // create button and other graphical references here (this method is run after all the graphical elements have been loaded)
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);

        // reference graphical elements
        Button connectButton = view.findViewById(R.id.button_connect);

        // connectButton listener, sends a request to connect to the esp32 ledController
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(MainMessage.BT_CONNECT, 0);
            }
        });

        return view;
    }

    // called after OnCreateView, recycler view goes here because of the adapter and viewModel
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // get recyclerview
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);

        // create list adapter with fragment's context
        final PatternListAdapter adapter = new PatternListAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // get PatternViewModel, this is the same one created in the main activity
        PatternViewModel mPatternViewModel = new ViewModelProvider(requireActivity()).get(PatternViewModel.class);

        // set observer to update the adapter
        mPatternViewModel.getAllColorPatterns().observe(requireActivity(), new Observer<List<ColorPattern>>() {
            @Override
            public void onChanged(List<ColorPattern> colorPatterns) {
                adapter.setPatterns(colorPatterns);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // sends a message using EventBus
    private void sendMessage(int message, int position)
    {
        Log.d(TAG, "Sending Message from SendFragment: " + message);
        EventBus.getDefault().post(new MainMessage(message, position));
    }

    // runs when the pattern is clicked in the recyclerView
    @Override
    public void onPatternClick(int position) {
        sendMessage(MainMessage.BT_SEND, position);

    }
}
