package com.example.ledcontroller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PatternsFragment extends Fragment implements PatternListAdapter.OnPatternListener{

    private static final String TAG = "PatternsFragment";

    public static final int REQUEST_CODE = 1;

    // color pattern that is currently selected
    private ColorPattern selected = null;

    // viewModel and List of patterns
    private PatternViewModel mPatternViewModel;
    private List<ColorPattern> mColorPatterns;

    // textView
    private TextView selectedText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_patterns, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // buttons
        Button editButton = view.findViewById(R.id.button_edit);
        Button newButton = view.findViewById(R.id.button_new);
        Button deleteButton = view.findViewById(R.id.button_delete);

        // textView
        selectedText = view.findViewById(R.id.textView_selected);

        // recyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);

        // create list adapter with fragment's context
        final PatternListAdapter adapter = new PatternListAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // get PatternViewModel, this is the same one created in the main activity
        mPatternViewModel = new ViewModelProvider(requireActivity()).get(PatternViewModel.class);

        // set observer to update the adapter
        mPatternViewModel.getAllColorPatterns().observe(requireActivity(), new Observer<List<ColorPattern>>() {
            @Override
            public void onChanged(List<ColorPattern> colorPatterns) {
                adapter.setPatterns(colorPatterns);
                mColorPatterns = colorPatterns;
            }
        });

        // onClickListeners
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected != null)
                {
                    Log.d(TAG, "onClick: Deleting " + selected.getName());
                    mPatternViewModel.delete(selected);
                    selectedText.setText("Pattern Deleted");
                    selected = null;
                }
                else
                {
                    selectedText.setText("No Pattern Selected");
                }
            }
        });

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start newPattern Activity
                Intent newPatternIntent = new Intent(getContext(), PatternActivity.class);
                startActivityForResult(newPatternIntent, REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            ColorPattern pattern = new ColorPattern(data.getStringExtra(PatternActivity.EXTRA_NAME), data.getByteArrayExtra(PatternActivity.EXTRA_CODE));
            mPatternViewModel.insert(pattern);
        }
    }

    @Override
    public void onPatternClick(int position) {
        // get pattern that was clicked
        selected = mColorPatterns.get(position);

        // show the name of the selected pattern
        selectedText.setText(selected.getName());
    }
}
