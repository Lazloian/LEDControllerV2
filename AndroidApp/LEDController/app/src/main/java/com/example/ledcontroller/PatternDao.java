package com.example.ledcontroller;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// this class is the DAO, it handles interactions with the database
@Dao
public interface PatternDao { // public in the following methods is redundant since this is an interface

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Inserts a pattern into the database, if there is a name conflict it is replaced
    void insert(ColorPattern colorPattern);

    @Update // updates the pattern with the same primary key
    void update(ColorPattern colorPattern);

    @Delete // deletes the pattern with the same primary key
    void delete(ColorPattern colorPattern);

    @Query("DELETE FROM pattern_table") // deletes all entities in the pattern table
    void deleteAll();

    @Query("SELECT * from pattern_table ORDER BY name ASC") // returns an alphabetized list of all the patterns
    LiveData<List<ColorPattern>> getPatterns();
}
