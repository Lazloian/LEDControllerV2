package com.example.ledcontroller;

// class to store messages received from BTConnection service that are sent to MainActivity
public class BTMessage {

    private String mMessage;

    // constructor
    BTMessage(String message)
    {
        mMessage = message;
    }

    // message getter
    public String getMessage()
    {
        return mMessage;
    }
}
