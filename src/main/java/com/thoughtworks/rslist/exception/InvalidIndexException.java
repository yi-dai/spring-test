package com.thoughtworks.rslist.exception;

public class InvalidIndexException extends RuntimeException {
    private String errorMessage;
    public InvalidIndexException(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    @Override
    public String getMessage(){
        return errorMessage;
    }

}
