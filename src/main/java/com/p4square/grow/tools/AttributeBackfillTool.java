/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.tools;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.p4square.grow.model.*;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;

import com.p4square.f1oauth.Attribute;
import com.p4square.f1oauth.F1API;
import com.p4square.f1oauth.F1Access;
import com.p4square.f1oauth.F1Exception;
import com.p4square.restlet.oauth.OAuthUser;

import com.p4square.grow.backend.dynamo.DynamoDatabase;
import com.p4square.grow.backend.dynamo.DynamoKey;

import com.p4square.grow.config.Config;

import com.p4square.grow.provider.JsonEncodedProvider;

/**
 * This utility is used to backfill F1 Attributes from the GROW database into F1.
 *
 * This tool currently reads from Dynamo directly. It should probably access the
 * backend or use the {@link com.p4square.grow.backend.GrowData} abstraction instead.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class AttributeBackfillTool {

    private static Config mConfig;
    private static F1API mF1API;
    private static DynamoDatabase mDatabase;

    public static void usage() {
        System.out.println("java com.p4square.grow.tools.AttributeBackfillTool <command>...\n");
        System.out.println("Commands:");
        System.out.println("\t--domain <domain>             Set config domain");
        System.out.println("\t--dev                         Set config domain to dev");
        System.out.println("\t--config <file>               Merge in config file");
        System.out.println("\t--assessments                 Backfill All Assessments");
        System.out.println("\t--training                    Backfill All Training Records");
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
                    mDatabase = null;
                    offset += 2;

                } else if ("--dev".equals(args[offset])) {
                    mConfig.setDomain("dev");
                    mF1API = null;
                    mDatabase = null;
                    offset += 1;

                } else if ("--config".equals(args[offset])) {
                    mConfig.updateConfig(args[offset + 1]);
                    mF1API = null;
                    mDatabase = null;
                    offset += 2;

                } else if ("--assessments".equals(args[offset])) {
                    offset = assessments(args, ++offset);

                } else if ("--training".equals(args[offset])) {
                    offset = training(args, ++offset);

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

    private static DynamoDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = new DynamoDatabase(mConfig);
        }

        return mDatabase;
    }

    private static int assessments(String[] args, int offset) throws Exception {
        final F1API f1 = getF1API();
        final DynamoDatabase db = getDatabase();

        DynamoKey key = DynamoKey.newKey("assessments", null);

        while (key != null) {
            Map<DynamoKey, Map<String, String>> rows = db.getAll(key);

            key = null;

            for (Map.Entry<DynamoKey, Map<String, String>> row : rows.entrySet()) {
                key = row.getKey();

                String userId = key.getHashKey();

                String summaryString = row.getValue().get("summary");
                if (summaryString == null || summaryString.length() == 0) {
                    System.out.printf("%s assessment incomplete\n", userId);
                    continue;
                }

                try {
                    Map summary = JsonEncodedProvider.MAPPER.readValue(summaryString, Map.class);

                    String result = (String) summary.get("result");
                    if (result == null) {
                        System.out.printf("%s assessment incomplete\n", userId);
                        continue;
                    }

                    String attributeName = "Assessment Complete - " + result;

                    // Check if the user already has the attribute.
                    List<Attribute> attributes = f1.getAttribute(userId, attributeName);

                    if (attributes.size() == 0) {
                        Attribute attribute = new Attribute(attributeName);
                        attribute.setStartDate(new Date());
                        attribute.setComment(summaryString);

                        if (f1.addAttribute(userId, attribute)) {
                            System.out.printf("%s attribute added\n", userId);
                        } else {
                            System.out.printf("%s failed to add attribute\n", userId);
                        }
                    } else {
                        System.out.printf("%s already has attribute\n", userId);
                    }
                } catch (Exception e) {
                    System.out.printf("%s exception: %s\n", userId, e.getMessage());
                }
            }
        }

        return offset;
    }

    private static int training(String[] args, int offset) throws Exception {
        final F1API f1 = getF1API();
        final DynamoDatabase db = getDatabase();

        DynamoKey key = DynamoKey.newKey("training", null);

        while (key != null) {
            Map<DynamoKey, Map<String, String>> rows = db.getAll(key);

            key = null;

            for (Map.Entry<DynamoKey, Map<String, String>> row : rows.entrySet()) {
                key = row.getKey();

                String userId = key.getHashKey();

                String valueString = row.getValue().get("value");
                if (valueString == null || valueString.length() == 0) {
                    System.out.printf("%s empty training record\n", userId);
                    continue;
                }

                try {
                    TrainingRecord record =
                        JsonEncodedProvider.MAPPER.readValue(valueString, TrainingRecord.class);
                    Playlist playlist = record.getPlaylist();

chapters:
                    for (Map.Entry<Chapters, Chapter> entry : playlist.getChaptersMap().entrySet()) {
                        Chapter chapter = entry.getValue();

                        // Find completion date
                        Date complete = new Date(0);
                        for (VideoRecord vr : chapter.getVideos().values()) {
                            if (!vr.getComplete()) {
                                continue chapters;
                            }

                            Date recordCompletion = vr.getCompletionDate();
                            if (recordCompletion != null && complete.before(recordCompletion)) {
                                complete = vr.getCompletionDate();
                            }
                        }

                        String attributeName = "Training Complete - " + entry.getKey().toString().toLowerCase();

                        // Check if the user already has the attribute.
                        List<Attribute> attributes = f1.getAttribute(userId, attributeName);

                        if (attributes.size() == 0) {
                            Attribute attribute = new Attribute(attributeName);
                            attribute.setStartDate(complete);

                            if (f1.addAttribute(userId, attribute)) {
                                System.out.printf("%s added %s\n", userId, attributeName);
                            } else {
                                System.out.printf("%s failed to add %s\n", userId, attributeName);
                            }
                        } else {
                            System.out.printf("%s already has %s\n", userId, attributeName);
                        }
                    }

                } catch (Exception e) {
                    System.out.printf("%s exception: %s\n", userId, e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return offset;
    }
}
