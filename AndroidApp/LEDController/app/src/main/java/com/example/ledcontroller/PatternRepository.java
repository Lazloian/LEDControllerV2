package com.example.ledcontroller;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

// this class facilitates interactions between the app and database
class PatternRepository {

    private PatternDao mPatternDao; // DAO
    private LiveData<List<ColorPattern>> mAllColorPatterns; // Patterns in database

    PatternRepository (Application application)
    {
        // get database
        PatternDatabase db = PatternDatabase.getDatabase(application);

        // get DAO from database
        mPatternDao = db.patternDao();

        // get patterns from database
        mAllColorPatterns = mPatternDao.getPatterns();
    }

    // returns all the patterns
    LiveData<List<ColorPattern>> getAllColorPatterns()
    {
        return mAllColorPatterns;
    }

    // inserts a pattern into the database
    void insert(ColorPattern colorPattern)
    {
        PatternDatabase.databaseWriterExecutor.execute(() ->
        {
            mPatternDao.insert(colorPattern);
        });
    }

    // updates an already existing pattern
    void update(ColorPattern colorPattern)
    {
        PatternDatabase.databaseWriterExecutor.execute(() ->
        {
            mPatternDao.update(colorPattern);
        });
    }

    // deletes a pattern from the database
    void delete(ColorPattern colorPattern)
    {
        PatternDatabase.databaseWriterExecutor.execute(() ->
        {
            mPatternDao.delete(colorPattern);
        });
    }
}
