package com.thoughtworks.rslist.exception;

public class CommonError {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public CommonError(){
    }
    public CommonError(String errorMessage){
        this.errorMessage = errorMessage;
    }

}
