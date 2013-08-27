/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import org.restlet.representation.StringRepresentation;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ErrorPage extends StringRepresentation {
    public static final ErrorPage TEMPLATE_NOT_FOUND = new ErrorPage();
    public static final ErrorPage RENDER_ERROR = new ErrorPage();


    public ErrorPage() {
        super("TODO");
    }

    public ErrorPage(String s) {
        super(s);
    }
}
