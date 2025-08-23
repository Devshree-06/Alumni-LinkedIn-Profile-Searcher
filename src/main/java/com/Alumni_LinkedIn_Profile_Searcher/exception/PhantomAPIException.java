package com.Alumni_LinkedIn_Profile_Searcher.exception;


public class PhantomAPIException extends RuntimeException{

    public PhantomAPIException(String message){
        super(message);
    }

    public PhantomAPIException(String message, Throwable cause) {
        super(message, cause);
    }
}
