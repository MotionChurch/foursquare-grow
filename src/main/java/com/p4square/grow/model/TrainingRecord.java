/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

/**
 * Representation of a user's training record.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingRecord {
    private String mLastVideo;
    private Playlist mPlaylist;

    public TrainingRecord() {
        mPlaylist = new Playlist();
    }

    /**
     * @return Video id of the last video watched.
     */
    public String getLastVideo() {
        return mLastVideo;
    }

    /**
     * Set the video id for the last video watched.
     * @param video The new video id.
     */
    public void setLastVideo(String video) {
        mLastVideo = video;
    }

    /**
     * @return the user's Playlist.
     */
    public Playlist getPlaylist() {
        return mPlaylist;
    }

    /**
     * Set the user's playlist.
     * @param playlist The new playlist.
     */
    public void setPlaylist(Playlist playlist) {
        mPlaylist = playlist;
    }
}
