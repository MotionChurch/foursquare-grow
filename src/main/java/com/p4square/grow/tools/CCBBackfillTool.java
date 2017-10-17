/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.tools;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.CustomPulldownFieldValue;
import com.p4square.ccbapi.model.GetIndividualProfilesRequest;
import com.p4square.ccbapi.model.GetIndividualProfilesResponse;
import com.p4square.ccbapi.model.IndividualProfile;
import com.p4square.grow.ccb.CCBProgressReporter;
import com.p4square.grow.ccb.CCBUser;
import com.p4square.grow.ccb.ChurchCommunityBuilderIntegrationDriver;
import com.p4square.grow.frontend.IntegrationDriver;
import com.p4square.grow.frontend.ProgressReporter;
import com.p4square.grow.model.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
 * This utility is used to backfill attributes from the GROW database into CCB.
 *
 * This tool currently reads from Dynamo directly. It should probably access the
 * backend or use the {@link com.p4square.grow.backend.GrowData} abstraction instead.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CCBBackfillTool {

    private static final Chapters[] REVERSED_CHAPTERS = { Chapters.LEADER, Chapters.TEACHER, Chapters.DISCIPLE,
            Chapters.BELIEVER, Chapters.SEEKER, Chapters.INTRODUCTION};

    private static final String GROW_LEVEL = "GrowLevelTrain";

    private static Config mConfig;
    private static ChurchCommunityBuilderIntegrationDriver mIntegrationDriver;
    private static DynamoDatabase mDatabase;

    public static void usage() {
        System.out.println("java com.p4square.grow.tools.CCBBackfillTool <command>...\n");
        System.out.println("Commands:");
        System.out.println("\t--domain <domain>             Set config domain");
        System.out.println("\t--dev                         Set config domain to dev");
        System.out.println("\t--config <file>               Merge in config file");
        System.out.println("\t--training [page] [limit]     Backfill All Training Records");
    }

    public static void main(String... args) {
        Logger.getRootLogger().setLevel(Level.WARN);

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
                    mIntegrationDriver = null;
                    mDatabase = null;
                    offset += 2;

                } else if ("--dev".equals(args[offset])) {
                    mConfig.setDomain("dev");
                    mIntegrationDriver = null;
                    mDatabase = null;
                    offset += 1;

                } else if ("--config".equals(args[offset])) {
                    mConfig.updateConfig(args[offset + 1]);
                    mIntegrationDriver = null;
                    mDatabase = null;
                    offset += 2;

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

    private static ChurchCommunityBuilderIntegrationDriver getIntegrationDriver() throws Exception {
        if (mIntegrationDriver == null) {
            Context context = new Context();
            Client client = new Client(context, Arrays.asList(Protocol.HTTP, Protocol.HTTPS));
            context.setClientDispatcher(client);

            Map<String, Object> attributes = new HashMap<>();
            attributes.put("com.p4square.grow.config", mConfig);
            context.setAttributes(attributes);

            mIntegrationDriver = new ChurchCommunityBuilderIntegrationDriver(context);
        }
        return mIntegrationDriver;
    }

    private static DynamoDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = new DynamoDatabase(mConfig);
        }
        return mDatabase;
    }

    private static int training(String[] args, int offset) throws Exception {

        final CCBAPI ccb = getIntegrationDriver().getAPI();
        final ProgressReporter reporter = mIntegrationDriver.getProgressReporter();

        // Iterate over every person in CCB
        long limit = Long.MAX_VALUE;
        GetIndividualProfilesRequest req = new GetIndividualProfilesRequest().withPage(1);
        if (args.length > offset) {
            req.withPage(Integer.parseInt(args[offset++]));
        }
        if (args.length > offset) {
            limit = Integer.parseInt(args[offset++]);
        }

        GetIndividualProfilesResponse resp;
        do {
            System.err.println("Fetching page " + req.getPage());
            resp = ccb.getIndividualProfiles(req);

            for (IndividualProfile profile : resp.getIndividuals()) {
                // Look for the person in Grow
                CCBUser user = new CCBUser(profile);
                TrainingRecord record = getTrainingRecord(user);
                if (record == null) {
                    continue;
                }

                // Find the highest completed chapter
                Map<Chapters, Chapter> chaptersMap = record.getPlaylist().getChaptersMap();
                Optional<Chapter> highestChapterCompleted = Arrays.stream(REVERSED_CHAPTERS)
                        .map(chaptersMap::get)
                        .filter(Objects::nonNull)
                        .filter(Chapter::isRequired)
                        .filter(Chapter::isComplete)
                        .findFirst();

                if (highestChapterCompleted.isPresent()) {
                    final Chapters lastChapter = highestChapterCompleted.get().getName();
                    final Date completionDate = highestChapterCompleted.get().getCompletionDate();

                    // Check if the level needs to be updated
                    final CustomPulldownFieldValue currentLevel = user.getProfile()
                            .getCustomPulldownFields().getByLabel(GROW_LEVEL);
                    String currentChapterString = currentLevel == null ? null : currentLevel.getSelection().getLabel();
                    
                    // Should we update the level?
                    if (currentChapterString != null && lastChapter.compareTo(Chapters.fromString(currentChapterString)) <= 0) {
                        // Print out a note that we didn't modify this user.
                        System.out.printf("** UNMODIFIED %s %s %s %s\n",
                                user.getIdentifier(),  // User
                                currentChapterString,  // Level in CCB
                                lastChapter,           // Level in Grow
                                currentChapterString); // Level to persist in CCB
                    } else {
                        // Print out a summary of the changes
                        System.out.printf("** UPDATE %s %s %s %s %s\n",
                                user.getIdentifier(), // User
                                currentChapterString, // Level in CCB
                                lastChapter,          // Level in Grow
                                lastChapter,          // Level to persist in CCB
                                completionDate);      // New completion date
                        reporter.reportChapterComplete(user, lastChapter, completionDate);
                    }

                } else {
                    System.out.println("** INCOMPLETE " + user.getIdentifier());
                }
            }
            req.withPage(req.getPage() + 1);
            limit--;
        } while (resp.getIndividuals().size() > 0 && limit > 0);

        return offset;
    }

    private static TrainingRecord getTrainingRecord(CCBUser user) throws IOException {
        DynamoKey key = DynamoKey.newKey("training", user.getIdentifier());

        String valueString = getDatabase().getKey(key).get("value");
        if (valueString == null || valueString.length() == 0) {
            // User doesn't exist.
            System.out.println("** MISSING " + user.getIdentifier());
            return null;
        }

        return JsonEncodedProvider.MAPPER.readValue(valueString, TrainingRecord.class);
    }
}
