package com.example.ledcontroller;

// class stores a message to either start a process from main activity or update user on state of a process
public class MainMessage {

    private int mMessage;
    private int mPosition;

    public static final int BT_OFF = 6;
    public static final int BT_BRIGHTNESS = 5;
    public static final int BT_SEND = 4;
    public static final int BT_CONNECT = 3;
    public static final int CONNECTION_SUCCESS = 1;
    public static final int CONNECTION_FAILURE = 0;

    // constructor
    MainMessage(int message, int position)
    {
        mMessage = message;
        mPosition = position;
    }

    // returns the message sent
    public int getMessage()
    {
        return mMessage;
    }

    public int getPosition()
    {
        return mPosition;
    }
}
