package com.online.banking.Back_End_Banking_System.exception;


public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message); // Passes the message to the parent RuntimeException class
    }
}
