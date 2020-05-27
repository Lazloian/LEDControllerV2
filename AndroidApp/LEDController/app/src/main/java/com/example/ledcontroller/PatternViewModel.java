package com.example.ledcontroller;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

// this class gets data from database for the ui and sends data to the repository
public class PatternViewModel extends AndroidViewModel {

    private PatternRepository mRepository;
    private LiveData<List<ColorPattern>> mColorPatterns;

    public PatternViewModel (Application application)
    {
        super(application);

        // create new repository and load cached patterns
        mRepository = new PatternRepository(application);
        mColorPatterns = mRepository.getAllColorPatterns();
    }

    // returns all color patterns
    LiveData<List<ColorPattern>> getAllColorPatterns()
    {
        return mColorPatterns;
    }

    // adds a color pattern into the database
    public void insert(ColorPattern colorPattern)
    {
        mRepository.insert(colorPattern);
    }

    // updates a color pattern already in teh database
    public void update(ColorPattern colorPattern)
    {
        mRepository.update(colorPattern);
    }

    // deletes a color pattern from the database
    public void delete(ColorPattern colorPattern)
    {
        mRepository.delete(colorPattern);
    }
}
