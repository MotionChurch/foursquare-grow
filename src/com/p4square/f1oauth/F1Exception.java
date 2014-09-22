/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.f1oauth;

public class F1Exception extends Exception {
    public F1Exception(String message) {
        super(message);
    }

    public F1Exception(String message, Exception cause) {
        super(message, cause);
    }
}
