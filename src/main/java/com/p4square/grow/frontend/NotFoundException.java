/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.io.IOException;

public class NotFoundException extends IOException {
    public NotFoundException(final String message) {
        super(message);
    }
}
