package com.example.ledcontroller;

// class to store messages received from BTConnection service that are sent to MainActivity
public class BTMessage {

    private char mMessage;
    public static final int UPDATE_ERROR = 0;
    public static final int UPDATE_SUCCESS = 1;

    // constructor
    BTMessage(char message)
    {
        mMessage = message;
    }

    // message getter
    public char getMessage()
    {
        return mMessage;
    }
}
