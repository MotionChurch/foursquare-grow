/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.p4square.grow.backend.db.CassandraDatabase;

/**
 * 
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
class Playlist {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Load a Playlist from the database.
     */
    public static Playlist load(CassandraDatabase db, String userId) throws IOException {
        String playlistString = db.getKey("training", userId, "playlist");

        if (playlistString == null) {
            return null;
        }

        Map<String, Map<String, VideoRecord>> playlist =
            MAPPER.readValue(playlistString, new TypeReference<Map<String, Map<String, VideoRecord>>>() { });

        return new Playlist(playlist);

    }

    /**
     * Persist the Playlist for the given user.
     * @return The String serialization of the playlist.
     */
    public static String save(CassandraDatabase db, String userId, Playlist playlist) throws IOException {
        String playlistString = MAPPER.writeValueAsString(playlist.mPlaylist);
        db.putKey("training", userId, "playlist", playlistString);
        return playlistString;
    }


    private Map<String, Map<String, VideoRecord>> mPlaylist;

    /**
     * Construct an empty playlist.
     */
    public Playlist() {
        mPlaylist = new HashMap<String, Map<String, VideoRecord>>();
    }

    /**
     * Constructor for database initialization.
     */
    private Playlist(Map<String, Map<String, VideoRecord>> playlist) {
        mPlaylist = playlist;
    }

    public VideoRecord find(String videoId) {
        for (Map<String, VideoRecord> chapter : mPlaylist.values()) {
            VideoRecord r = chapter.get(videoId);

            if (r != null) {
                return r;
            }
        }

        return null;
    }

    /**
     * Add a video to the playlist.
     */
    public VideoRecord add(String chapter, String videoId) {
        Map<String, VideoRecord> chapterMap = mPlaylist.get(chapter);

        if (chapterMap == null) {
            chapterMap = new HashMap<String, VideoRecord>();
            mPlaylist.put(chapter, chapterMap);
        }

        VideoRecord r = new VideoRecord();
        chapterMap.put(videoId, r);
        return r;
    }

    /**
     * @return The last chapter to be completed.
     */
    public Map<String, Boolean> getChapterStatuses() {
        Map<String, Boolean> completed = new HashMap<String, Boolean>();

        for (String chapter : mPlaylist.keySet()) {
            completed.put(chapter, isChapterComplete(chapter));
        }

        return completed;
    }

    public boolean isChapterComplete(String chapterId) {
        boolean complete = true;

        Map<String, VideoRecord> chapter = mPlaylist.get(chapterId);
        if (chapter != null) {
            for (VideoRecord r : chapter.values()) {
                if (r.getRequired() && !r.getComplete()) {
                    return false;
                }
            }
        }

        return complete;
    }

    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(mPlaylist);

        } catch (IOException e) {
            return super.toString();
        }
    }

}
