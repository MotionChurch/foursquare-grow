/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.fmfacade.ftl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import org.apache.log4j.Logger;

import org.restlet.data.Status;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

import org.restlet.ext.jackson.JacksonRepresentation;

/**
 * This method allows templates to make GET requests.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class GetMethod implements TemplateMethodModel {
    private static final Logger cLog = Logger.getLogger(GetMethod.class);

    private final Restlet mDispatcher;

    public GetMethod(Restlet dispatcher) {
        mDispatcher = dispatcher;
    }

    /**
     * @param args List with exactly two arguments:
     *              * The variable in which to put the result.
     *              * The URI to GET.
     */
    public TemplateModel exec(List args) throws TemplateModelException {
        final Environment env = Environment.getCurrentEnvironment();

        if (args.size() != 2) {
            throw new TemplateModelException(
                    "Expecting exactly one argument containing the URI");
        }

        Request request = new Request(Method.GET, (String) args.get(1));
        Response response = mDispatcher.handle(request);
        Status status = response.getStatus();
        Representation representation = response.getEntity();

        try {
            if (response.getStatus().isSuccess()) {
                JacksonRepresentation<Map> mapRepresentation;
                if (representation instanceof JacksonRepresentation) {
                    mapRepresentation = (JacksonRepresentation<Map>) representation;
                } else {
                    mapRepresentation = new JacksonRepresentation<Map>(
                            representation, Map.class);
                }
                try {
                    TemplateModel mapModel = env.getObjectWrapper().wrap(mapRepresentation.getObject());

                    env.setVariable((String) args.get(0), mapModel);

                } catch (IOException e) {
                    cLog.warn("Exception occurred when calling getObject(): " 
                            + e.getMessage(), e);
                    status = Status.SERVER_ERROR_INTERNAL;
                }
            }

            Map statusMap = new HashMap();
            statusMap.put("code", status.getCode());
            statusMap.put("reason", status.getReasonPhrase());
            statusMap.put("succeeded", status.isSuccess());
            return env.getObjectWrapper().wrap(statusMap);
        } finally {
            if (representation != null) {
                representation.release();
            }
        }
    }
}
