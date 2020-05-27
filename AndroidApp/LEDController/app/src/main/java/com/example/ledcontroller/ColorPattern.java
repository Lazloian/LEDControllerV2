package com.example.ledcontroller;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// class for entities that are stored in the database using Room
@Entity(tableName = "pattern_table") // name of the data table
public class ColorPattern {

    // name of the pattern
    @PrimaryKey // the key for each entity in the table will be its name
    @NonNull // the name will never be null
    @ColumnInfo(name = "name") // the name of the column of the table
    private String mName;

    // code to send to esp32
    @NonNull
    @ColumnInfo(name = "code")
    private String mCode;

    // constructor
    public ColorPattern(@NonNull String name, @NonNull String code)
    {
        this.mName = name;
        this.mCode = code;
    }

    // get name
    public String getName()
    {
        return this.mName;
    }

    // get code
    public String getCode()
    {
        return this.mCode;
    }
}
