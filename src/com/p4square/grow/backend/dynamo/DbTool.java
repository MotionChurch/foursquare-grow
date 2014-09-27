/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.backend.dynamo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.p4square.grow.backend.dynamo.DynamoDatabase;
import com.p4square.grow.backend.dynamo.DynamoKey;
import com.p4square.grow.config.Config;
import com.p4square.grow.model.UserRecord;
import com.p4square.grow.provider.Provider;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class DbTool {
    private static final FilenameFilter JSON_FILTER = new JsonFilter();

    private static Config mConfig;
    private static DynamoDatabase mDatabase;

    public static void usage() {
        System.out.println("java com.p4square.grow.backend.dynamo.DbTool <command>...\n");
        System.out.println("Commands:");
        System.out.println("\t--domain <domain>                           Set config domain");
        System.out.println("\t--dev                                       Set config domain to dev");
        System.out.println("\t--config <file>                             Merge in config file");
        System.out.println("\t--list                                      List all tables");
        System.out.println("\t--create <table> <reads> <writes>           Create a table");
        System.out.println("\t--update <table> <reads> <writes>           Update table throughput");
        System.out.println("\t--drop   <table>                            Delete a table");
        System.out.println("\t--get    <table> <key> <attribute>          Get a value");
        System.out.println("\t--put    <table> <key> <attribute> <value>  Put a value");
        System.out.println("\t--delete <table> <key> <attribute>          Delete a value");
        System.out.println("\t--scan   <table>                            List all rows");
        System.out.println("\t--scanf  <table> <attribute>                List all rows");
        System.out.println();
        System.out.println("Bootstrap Commands:");
        System.out.println("\t--bootstrap <data>          Create all tables and import all data");
        System.out.println("\t--loadStrings <data>        Load all videos and questions");
        System.out.println("\t--destroy                   Drop all tables");
        System.out.println("\t--addadmin <user> <pass>    Add a backend account");
        System.out.println("\t--import   <table> <file>   Backfill a table");
    }

    public static void main(String... args) {
        if (args.length == 0) {
            usage();
            System.exit(1);
        }

        mConfig = new Config();

        try {
            mConfig.updateConfig(DbTool.class.getResourceAsStream("/grow.properties"));

            int offset = 0;
            while (offset < args.length) {
                if ("--domain".equals(args[offset])) {
                    mConfig.setDomain(args[offset + 1]);
                    mDatabase = null;
                    offset += 2;

                } else if ("--dev".equals(args[offset])) {
                    mConfig.setDomain("dev");
                    mDatabase = null;
                    offset += 1;

                } else if ("--config".equals(args[offset])) {
                    mConfig.updateConfig(args[offset + 1]);
                    mDatabase = null;
                    offset += 2;

                } else if ("--list".equals(args[offset])) {
                    //offset = list(args, ++offset);

                } else if ("--create".equals(args[offset])) {
                    offset = create(args, ++offset);

                } else if ("--update".equals(args[offset])) {
                    offset = update(args, ++offset);

                } else if ("--drop".equals(args[offset])) {
                    offset = drop(args, ++offset);

                } else if ("--get".equals(args[offset])) {
                    offset = get(args, ++offset);

                } else if ("--put".equals(args[offset])) {
                    offset = put(args, ++offset);

                } else if ("--delete".equals(args[offset])) {
                    offset = delete(args, ++offset);

                } else if ("--scan".equals(args[offset])) {
                    offset = scan(args, ++offset);

                } else if ("--scanf".equals(args[offset])) {
                    offset = scanf(args, ++offset);

                /* Bootstrap Commands */
                } else if ("--bootstrap".equals(args[offset])) {
                    if ("dev".equals(mConfig.getDomain())) {
                        offset = bootstrapDevTables(args, ++offset);
                    } else {
                        offset = bootstrapTables(args, ++offset);
                    }
                    offset = loadStrings(args, offset);

                } else if ("--loadStrings".equals(args[offset])) {
                    offset = loadStrings(args, ++offset);

                } else if ("--destroy".equals(args[offset])) {
                    offset = destroy(args, ++offset);

                } else if ("--addadmin".equals(args[offset])) {
                    offset = addAdmin(args, ++offset);

                } else if ("--import".equals(args[offset])) {
                    offset = importTable(args, ++offset);

                } else {
                    throw new IllegalArgumentException("Unknown command " + args[offset]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static DynamoDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = new DynamoDatabase(mConfig);
        }

        return mDatabase;
    }

    private static int create(String[] args, int offset) {
        String name = args[offset++];
        long reads  = Long.parseLong(args[offset++]);
        long writes = Long.parseLong(args[offset++]);

        DynamoDatabase db = getDatabase();

        db.createTable(name, reads, writes);

        return offset;
    }

    private static int update(String[] args, int offset) {
        String name = args[offset++];
        long reads  = Long.parseLong(args[offset++]);
        long writes = Long.parseLong(args[offset++]);

        DynamoDatabase db = getDatabase();

        db.updateTable(name, reads, writes);

        return offset;
    }

    private static int drop(String[] args, int offset) {
        String name = args[offset++];

        DynamoDatabase db = getDatabase();

        db.deleteTable(name);

        return offset;
    }

    private static int get(String[] args, int offset) {
        String table     = args[offset++];
        String key       = args[offset++];
        String attribute = args[offset++];

        DynamoDatabase db = getDatabase();

        String value = db.getAttribute(DynamoKey.newAttributeKey(table, key, attribute));

        if (value == null) {
            value = "<null>";
        }

        System.out.printf("%s %s:%s\n%s\n\n", table, key, attribute, value);

        return offset;
    }

    private static int put(String[] args, int offset) {
        String table     = args[offset++];
        String key       = args[offset++];
        String attribute = args[offset++];
        String value     = args[offset++];

        DynamoDatabase db = getDatabase();

        db.putAttribute(DynamoKey.newAttributeKey(table, key, attribute), value);

        return offset;
    }

    private static int delete(String[] args, int offset) {
        String table     = args[offset++];
        String key       = args[offset++];
        String attribute = args[offset++];

        DynamoDatabase db = getDatabase();

        db.deleteAttribute(DynamoKey.newAttributeKey(table, key, attribute));

        System.out.printf("Deleted %s %s:%s\n\n", table, key, attribute);

        return offset;
    }

    private static int scan(String[] args, int offset) {
        String table     = args[offset++];

        DynamoKey key = DynamoKey.newKey(table, null);

        doScan(key);

        return offset;
    }

    private static int scanf(String[] args, int offset) {
        String table     = args[offset++];
        String attribute = args[offset++];

        DynamoKey key = DynamoKey.newAttributeKey(table, null, attribute);

        doScan(key);

        return offset;
    }

    private static void doScan(DynamoKey key) {
        DynamoDatabase db = getDatabase();

        String attributeFilter = key.getAttribute();

        while (key != null) {
            Map<DynamoKey, Map<String, String>> result = db.getAll(key);

            key = null; // If there are no results, exit

            for (Map.Entry<DynamoKey, Map<String, String>> entry : result.entrySet()) {
                key = entry.getKey(); // Save the last key

                for (Map.Entry<String, String> attribute : entry.getValue().entrySet()) {
                    if (attributeFilter == null || attributeFilter.equals(attribute.getKey())) {
                        String keyString = key.getHashKey();
                        if (key.getRangeKey() != null) {
                            keyString += "(" + key.getRangeKey() + ")";
                        }
                        System.out.printf("%s %s:%s\n%s\n\n",
                                key.getTable(), keyString, attribute.getKey(),
                                attribute.getValue());
                    }
                }
            }
        }
    }


    private static int bootstrapTables(String[] args, int offset) {
        DynamoDatabase db = getDatabase();

        db.createTable("strings",      5,  1);
        db.createTable("accounts",     5,  1);
        db.createTable("assessments",  5,  5);
        db.createTable("training",     5,  5);
        db.createTable("feedthreads",  5,  1);
        db.createTable("feedmessages", 5,  1);

        return offset;
    }

    private static int bootstrapDevTables(String[] args, int offset) {
        DynamoDatabase db = getDatabase();

        db.createTable("strings",      1,  1);
        db.createTable("accounts",     1,  1);
        db.createTable("assessments",  1,  1);
        db.createTable("training",     1,  1);
        db.createTable("feedthreads",  1,  1);
        db.createTable("feedmessages", 1,  1);

        return offset;
    }

    private static int loadStrings(String[] args, int offset) throws IOException {
        String data = args[offset++];
        File baseDir = new File(data);

        DynamoDatabase db = getDatabase();

        insertQuestions(baseDir);
        insertVideos(baseDir);
        insertDefaultPlaylist(baseDir);

        return offset;
    }

    private static int destroy(String[] args, int offset) {
        DynamoDatabase db = getDatabase();

        final String[] tables = { "strings",
                                  "accounts",
                                  "assessments",
                                  "training",
                                  "feedthreads",
                                  "feedmessages"
                                };

        for (String table : tables) {
            try {
                db.deleteTable(table);
            } catch (Exception e) {
                System.err.println("Deleting " + table + ": " + e.getMessage());
            }
        }

        return offset;
    }

    private static int addAdmin(String[] args, int offset) throws IOException {
        String user = args[offset++];
        String pass = args[offset++];

        DynamoDatabase db = getDatabase();

        UserRecord record = new UserRecord();
        record.setId(user);
        record.setBackendPassword(pass);

        Provider<DynamoKey, UserRecord> provider = new DynamoProviderImpl(db, UserRecord.class);
        provider.put(DynamoKey.newAttributeKey("accounts", user, "value"), record);

        return offset;
    }

    private static int importTable(String[] args, int offset) throws IOException {
        String table = args[offset++];
        String filename = args[offset++];

        DynamoDatabase db = getDatabase();

        List<String> lines = Files.readAllLines(new File(filename).toPath(),
                StandardCharsets.UTF_8);

        int count = 0;

        String key = null;
        Map<String, String> attributes = new HashMap<>();
        for (String line : lines) {
            if (line.length() == 0) {
                if (attributes.size() > 0) {
                    db.putKey(DynamoKey.newKey(table, key), attributes);
                    count++;

                    if (count % 50 == 0) {
                        System.out.printf("Imported %d records into %s...\n", count, table);
                    }
                }
                key = null;
                attributes = new HashMap<>();
                continue;
            }

            if (key == null) {
                key = line;
                continue;
            }

            int space = line.indexOf(' ');
            String attribute = line.substring(0, space);
            String value = line.substring(space + 1);

            attributes.put(attribute, value);
        }

        // Finish up the remaining attributes.
        if (key != null && attributes.size() > 0) {
            db.putKey(DynamoKey.newKey(table, key), attributes);
            count++;
        }

        System.out.printf("Imported %d records into %s.\n", count, table);

        return offset;
    }

    private static void insertQuestions(File baseDir) throws IOException {
        DynamoDatabase db = getDatabase();
        File questions = new File(baseDir, "questions");

        File[] files = questions.listFiles(JSON_FILTER);
        Arrays.sort(files);

        for (File file : files) {
            String filename = file.getName();
            String questionId = filename.substring(0, filename.lastIndexOf('.'));

            byte[] encoded = Files.readAllBytes(file.toPath());
            String value = new String(encoded, StandardCharsets.UTF_8);
            db.putAttribute(DynamoKey.newAttributeKey("strings",
                        "/questions/" + questionId, "value"), value);
            System.out.println("Inserted /questions/" + questionId);
        }

        String filename = files[0].getName();
        String first    = filename.substring(0, filename.lastIndexOf('.'));
        int    count    = files.length;
        String summary  = "{\"first\": \"" + first + "\", \"count\": " + count + "}";
        db.putAttribute(DynamoKey.newAttributeKey("strings", "/questions", "value"), summary);
        System.out.println("Inserted /questions");
    }

    private static void insertVideos(File baseDir) throws IOException {
        DynamoDatabase db = getDatabase();
        File videos = new File(baseDir, "videos");

        for (File topic : videos.listFiles()) {
            if (!topic.isDirectory()) {
                continue;
            }

            String topicName = topic.getName();

            Map<String, String> attributes = new HashMap<>();
            File[] files = topic.listFiles(JSON_FILTER);
            for (File file : files) {
                String filename = file.getName();
                String videoId = filename.substring(0, filename.lastIndexOf('.'));

                byte[] encoded = Files.readAllBytes(file.toPath());
                String value = new String(encoded, StandardCharsets.UTF_8);

                attributes.put(videoId, value);
                System.out.println("Found /training/" + topicName + ":" + videoId);
            }

            db.putKey(DynamoKey.newKey("strings",
                        "/training/" + topicName), attributes);
            System.out.println("Inserted /training/" + topicName);
        }
    }

    private static void insertDefaultPlaylist(File baseDir) throws IOException {
        DynamoDatabase db = getDatabase();
        File file = new File(baseDir, "videos/playlist.json");

        byte[] encoded = Files.readAllBytes(file.toPath());
        String value = new String(encoded, StandardCharsets.UTF_8);
        db.putAttribute(DynamoKey.newAttributeKey("strings",
                    "/training/defaultplaylist", "value"), value);
        System.out.println("Inserted /training/defaultplaylist");
    }

    private static class JsonFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".json");
        }
    }
}
