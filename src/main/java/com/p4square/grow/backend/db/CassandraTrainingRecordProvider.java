/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.backend.db;

import java.io.IOException;

import com.p4square.grow.model.Playlist;
import com.p4square.grow.model.TrainingRecord;

import com.p4square.grow.provider.JsonEncodedProvider;
import com.p4square.grow.provider.Provider;

/**
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class CassandraTrainingRecordProvider implements Provider<String, TrainingRecord> {
    private static final CassandraKey DEFAULT_PLAYLIST_KEY = new CassandraKey("strings", "defaultPlaylist", "value");

    private static final String COLUMN_FAMILY = "training";
    private static final String PLAYLIST_KEY = "playlist";
    private static final String LAST_VIDEO_KEY = "lastVideo";

    private final CassandraDatabase mDb;
    private final Provider<CassandraKey, Playlist> mPlaylistProvider;

    public CassandraTrainingRecordProvider(CassandraDatabase db) {
        mDb = db;
        mPlaylistProvider = new CassandraProviderImpl<>(db, Playlist.class);
    }

    @Override
    public TrainingRecord get(String userid) throws IOException {
        Playlist playlist = mPlaylistProvider.get(new CassandraKey(COLUMN_FAMILY, userid, PLAYLIST_KEY));

        if (playlist == null) {
            // We consider no playlist to mean no record whatsoever.
            return null;
        }

        TrainingRecord r = new TrainingRecord();
        r.setPlaylist(playlist);
        r.setLastVideo(mDb.getKey(COLUMN_FAMILY, userid, LAST_VIDEO_KEY));

        return r;
    }

    @Override
    public void put(String userid, TrainingRecord record) throws IOException {
        String lastVideo = record.getLastVideo();
        Playlist playlist = record.getPlaylist();

        mDb.putKey(COLUMN_FAMILY, userid, LAST_VIDEO_KEY, lastVideo);
        mPlaylistProvider.put(new CassandraKey(COLUMN_FAMILY, userid, PLAYLIST_KEY), playlist);
    }

    /**
     * @return the default playlist stored in the database.
     */
    public Playlist getDefaultPlaylist() throws IOException {
        Playlist playlist = mPlaylistProvider.get(DEFAULT_PLAYLIST_KEY);

        if (playlist == null) {
            playlist = new Playlist();
        }

        return playlist;
    }
}
