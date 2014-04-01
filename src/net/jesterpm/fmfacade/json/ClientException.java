/*
 * Copyright 2013 Jesse Morgan
 */

package net.jesterpm.fmfacade.json;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ClientException extends Exception {

    public ClientException(final String msg) {
        super(msg);
    }

    public ClientException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
