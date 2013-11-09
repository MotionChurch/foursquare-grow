/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Representation of a user's playlist.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Playlist {
    /**
     * Map of Chapter ID to map of Video ID to VideoRecord.
     */
    private Map<String, Chapter> mPlaylist;

    private Date mLastUpdated;

    /**
     * Construct an empty playlist.
     */
    public Playlist() {
        mPlaylist = new HashMap<String, Chapter>();
        mLastUpdated = new Date(0); // Default to a prehistoric date if we don't have one.
    }

    /**
     * Find the VideoRecord for a video id.
     */
    public VideoRecord find(String videoId) {
        for (Chapter chapter : mPlaylist.values()) {
            VideoRecord r = chapter.getVideoRecord(videoId);

            if (r != null) {
                return r;
            }
        }

        return null;
    }

    /**
     * @return The last modified date of the source playlist.
     */
    public Date getLastUpdated() {
        return mLastUpdated;
    }

    /**
     * Set the last updated date.
     * @param date the new last updated date.
     */
    public void setLastUpdated(Date date) {
        mLastUpdated = date;
    }

    /**
     * Add a video to the playlist.
     */
    public VideoRecord add(String chapterId, String videoId) {
        Chapter chapter = mPlaylist.get(chapterId);

        if (chapter == null) {
            chapter = new Chapter();
            mPlaylist.put(chapterId, chapter);
        }

        VideoRecord r = new VideoRecord();
        chapter.setVideoRecord(videoId, r);
        return r;
    }

    /**
     * Add a Chapter to the Playlist.
     * @param chapterId The name of the chapter.
     * @param chapter The Chapter object to add.
     */
    @JsonAnySetter
    public void addChapter(String chapterId, Chapter chapter) {
        mPlaylist.put(chapterId, chapter);
    }

    /**
     * @return a map of chapter id to chapter.
     */
    @JsonAnyGetter
    public Map<String, Chapter> getChaptersMap() {
        return mPlaylist;
    }

    /**
     * @return The last chapter to be completed.
     */
    @JsonIgnore
    public Map<String, Boolean> getChapterStatuses() {
        Map<String, Boolean> completed = new HashMap<String, Boolean>();

        for (Map.Entry<String, Chapter> entry : mPlaylist.entrySet()) {
            completed.put(entry.getKey(), entry.getValue().isComplete());
        }

        return completed;
    }

    /**
     * @return true if all required videos in the chapter have been watched.
     */
    public boolean isChapterComplete(String chapterId) {
        Chapter chapter = mPlaylist.get(chapterId);
        if (chapter != null) {
            return chapter.isComplete();
        }

        return false;
    }

    /**
     * Merge a playlist into this playlist.
     *
     * Merge is accomplished by adding all missing Chapters and VideoRecords to
     * this playlist.
     */
    public void merge(Playlist source) {
        if (source.getLastUpdated().before(mLastUpdated)) {
            // Already up to date.
            return;
        }

        for (Map.Entry<String, Chapter> entry : source.getChaptersMap().entrySet()) {
            String chapterName = entry.getKey();
            Chapter theirChapter = entry.getValue();
            Chapter myChapter = mPlaylist.get(entry.getKey());

            if (myChapter == null) {
                // Add entire chapter
                try {
                    mPlaylist.put(chapterName, theirChapter.clone());
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e); // Unexpected...
                }

            } else {
                // Check chapter for missing videos
                for (Map.Entry<String, VideoRecord> videoEntry : theirChapter.getVideos().entrySet()) {
                    String videoId = videoEntry.getKey();
                    VideoRecord myVideo = myChapter.getVideoRecord(videoId);

                    if (myVideo == null) {
                        try {
                            myVideo = videoEntry.getValue().clone();
                            myChapter.setVideoRecord(videoId, myVideo);
                        } catch (CloneNotSupportedException e) {
                            throw new RuntimeException(e); // Unexpected...
                        }
                    }
                }
            }
        }

        mLastUpdated = source.getLastUpdated();
    }
}
