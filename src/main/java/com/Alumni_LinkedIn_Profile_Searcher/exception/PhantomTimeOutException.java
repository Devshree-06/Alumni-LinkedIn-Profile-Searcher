package com.Alumni_LinkedIn_Profile_Searcher.exception;

public class PhantomTimeOutException extends RuntimeException{

    public PhantomTimeOutException(String message){
        super(message);
    }

    public PhantomTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }
}
