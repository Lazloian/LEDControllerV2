package com.example.ledcontroller;

// class to store a message that is sent from fragments to mainActivity and other services
public class FragMessage {

    private String mMessage;

    // constructor
    FragMessage(String message)
    {
        mMessage = message;
    }

    // returns the message sent
    public String getMessage()
    {
        return mMessage;
    }
}
