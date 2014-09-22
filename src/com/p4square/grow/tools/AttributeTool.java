/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.tools;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;

import com.p4square.grow.config.Config;
import com.p4square.f1oauth.Attribute;
import com.p4square.f1oauth.F1Access;
import com.p4square.f1oauth.F1API;
import com.p4square.f1oauth.F1Exception;
import com.p4square.restlet.oauth.OAuthUser;

/**
 * Tool for manipulating F1 Attributes.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AttributeTool {

    private static Config mConfig;
    private static F1API mF1API;

    public static void usage() {
        System.out.println("java com.p4square.grow.tools.AttributeTool <command>...\n");
        System.out.println("Commands:");
        System.out.println("\t--domain <domain>                           Set config domain");
        System.out.println("\t--dev                                       Set config domain to dev");
        System.out.println("\t--config <file>                             Merge in config file");
        System.out.println("\t--list                                      List all attributes");
        System.out.println("\t--assign <user> <attribute> <comment>       Assign an attribute");
    }

    public static void main(String... args) {
        if (args.length == 0) {
            usage();
            System.exit(1);
        }

        mConfig = new Config();

        try {
            mConfig.updateConfig(AttributeTool.class.getResourceAsStream("/grow.properties"));

            int offset = 0;
            while (offset < args.length) {
                if ("--domain".equals(args[offset])) {
                    mConfig.setDomain(args[offset + 1]);
                    mF1API = null;
                    offset += 2;

                } else if ("--dev".equals(args[offset])) {
                    mConfig.setDomain("dev");
                    mF1API = null;
                    offset += 1;

                } else if ("--config".equals(args[offset])) {
                    mConfig.updateConfig(args[offset + 1]);
                    mF1API = null;
                    offset += 2;

                } else if ("--list".equals(args[offset])) {
                    offset = list(args, ++offset);

                } else if ("--assign".equals(args[offset])) {
                    offset = assign(args, ++offset);

                } else {
                    throw new IllegalArgumentException("Unknown command " + args[offset]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static F1API getF1API() throws Exception {
        if (mF1API == null) {
            Context context = new Context();
            Client client = new Client(context, Arrays.asList(Protocol.HTTP, Protocol.HTTPS));
            context.setClientDispatcher(client);

            F1Access f1Access = new F1Access(context,
                    mConfig.getString("f1ConsumerKey"),
                    mConfig.getString("f1ConsumerSecret"),
                    mConfig.getString("f1BaseUrl"),
                    mConfig.getString("f1ChurchCode"),
                    F1Access.UserType.WEBLINK);

            // Gather Username and Password
            String username = System.console().readLine("F1 Username: ");
            char[] password = System.console().readPassword("F1 Password: ");

            OAuthUser user = f1Access.getAccessToken(username, new String(password));
            Arrays.fill(password, ' '); // Lost cause, but I'll still try.

            mF1API = f1Access.getAuthenticatedApi(user);
        }

        return mF1API;
    }

    private static int list(String[] args, int offset) throws Exception {
        final F1API f1 = getF1API();

        final Map<String, String> attributes = f1.getAttributeList();
        System.out.printf("%7s %s\n", "ID", "Name");
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            System.out.printf("%7s %s\n", entry.getValue(), entry.getKey());
        }

        return offset;
    }

    private static int assign(String[] args, int offset) throws Exception {
        final String userId = args[offset++];
        final String attributeName = args[offset++];
        final String comment = args[offset++];

        final F1API f1 = getF1API();

        Attribute attribute = new Attribute();
        attribute.setStartDate(new Date());
        attribute.setComment(comment);

        if (f1.addAttribute(userId, attributeName, attribute)) {
            System.out.println("Added attribute " + attributeName + " for " + userId);
        } else {
            System.out.println("Failed to add attribute " + attributeName + " for " + userId);
        }

        return offset;
    }
}
