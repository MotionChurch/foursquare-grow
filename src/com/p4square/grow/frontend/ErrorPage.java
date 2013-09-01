/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.frontend;

import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.Writer;

import freemarker.template.Template;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;

/**
 * ErrorPage wraps a String or Template Representation and displays the given
 * error message.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class ErrorPage extends WriterRepresentation {
    public static final ErrorPage TEMPLATE_NOT_FOUND =
        new ErrorPage("Could not find the requested page template.");

    public static final ErrorPage RENDER_ERROR =
        new ErrorPage("Error rendering page.");

    private static Template cTemplate = null;

    private final String mMessage;

    public ErrorPage(String msg) {
        this(msg, MediaType.TEXT_HTML);
    }

    public ErrorPage(String msg, MediaType mediaType) {
        super(mediaType);

        mMessage = msg;
    }

    public static synchronized void setTemplate(Template template) {
        cTemplate = template;
    }

    protected Representation getRepresentation() {
        if (cTemplate == null) {
            return new StringRepresentation(mMessage);

        } else {
            Map<String, Object> root = new HashMap<String, Object>();
            root.put("errorMessage", mMessage);
            return new TemplateRepresentation(cTemplate, root, MediaType.TEXT_HTML);
        }
    }

    @Override
    public void write(Writer writer) throws IOException {
        getRepresentation().write(writer);
    }
}
