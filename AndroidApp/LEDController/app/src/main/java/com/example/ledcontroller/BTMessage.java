package com.example.ledcontroller;

// class to store messages received from BTConnection service that are sent to MainActivity
public class BTMessage {

    private char mMessage;

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
