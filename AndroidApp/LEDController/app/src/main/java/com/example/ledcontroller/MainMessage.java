package com.example.ledcontroller;

// class stores a message to either start a process from main activity or update user on state of a process
public class MainMessage {

    private int mMessage;

    public static final int BT_CONNECT = 3;
    public static final int CONNECTION_SUCCESS = 1;
    public static final int CONNECTION_FAILURE = 0;

    // constructor
    MainMessage(int message)
    {
        mMessage = message;
    }

    // returns the message sent
    public int getMessage()
    {
        return mMessage;
    }
}
