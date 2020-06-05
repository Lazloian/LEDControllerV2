package com.example.ledcontroller;

// stores the result of the user creating a pattern
public class PatternMessage {

    public static final int ERROR_NONE = 0;
    public static final int ERROR_CANCEL = 1;
    public static final int ERROR_COLOR_MISSING = 2;
    public static final int ERROR_NUM_INVALID = 3;

    private byte[] mCode;
    private int mError;

    PatternMessage(byte[] code, int error)
    {
        mCode = code;
        mError = error;
    }

    public byte[] getCode()
    {
        return mCode;
    }

    public int getError()
    {
        return mError;
    }
}
