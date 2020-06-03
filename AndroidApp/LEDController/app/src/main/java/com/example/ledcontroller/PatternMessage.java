package com.example.ledcontroller;

// stores the result of the user creating a pattern
public class PatternMessage {

    private byte[] mCode;

    PatternMessage(byte[] code)
    {
        mCode = code;
    }

    public byte[] getCode()
    {
        return mCode;
    }
}
