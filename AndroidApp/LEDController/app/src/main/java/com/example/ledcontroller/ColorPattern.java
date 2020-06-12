package com.example.ledcontroller;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// class for entities that are stored in the database using Room
@Entity(tableName = "pattern_table") // name of the data table
public class ColorPattern implements Parcelable {

    // name of the pattern
    @PrimaryKey // the key for each entity in the table will be its name
    @NonNull // the name will never be null
    @ColumnInfo(name = "name") // the name of the column of the table
    private String mName;

    // code to send to esp32
    @NonNull
    @ColumnInfo(name = "code")
    private byte[] mCode;

    // constructor
    public ColorPattern(@NonNull String name, @NonNull byte[] code)
    {
        this.mName = name;
        this.mCode = code;
    }

    protected ColorPattern(Parcel in) { // order of these two
        mName = in.readString();
        mCode = in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) { // must be the same as these two
        dest.writeString(mName);
        dest.writeByteArray(mCode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ColorPattern> CREATOR = new Creator<ColorPattern>() {
        @Override
        public ColorPattern createFromParcel(Parcel in) {
            return new ColorPattern(in);
        }

        @Override
        public ColorPattern[] newArray(int size) {
            return new ColorPattern[size];
        }
    };

    // get name
    public String getName()
    {
        return this.mName;
    }

    // get code
    public byte[] getCode()
    {
        return this.mCode;
    }
}
