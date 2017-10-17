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
 * Chapter is a list of VideoRecords in a Playlist.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class Chapter implements Cloneable {
    private Chapters mName;
    private Map<String, VideoRecord> mVideos;

    public Chapter(Chapters name) {
        mName = name;
        mVideos = new HashMap<>();
    }

    /**
     * Private constructor for JSON decoding.
     */
    private Chapter() {
        this(null);
    }

    /**
     * @return The Chapter name.
     */
    public Chapters getName() {
        return mName;
    }

    /**
     * Set the chapter name.
     *
     * @param name The name of the chapter.
     */
    public void setName(final Chapters name) {
        mName = name;
    }

    /**
     * @return The VideoRecord for videoid or null if videoid is not in the chapter.
     */
    public VideoRecord getVideoRecord(String videoid) {
        return mVideos.get(videoid);
    }

    /**
     * @return A map of video ids to VideoRecords.
     */
    @JsonAnyGetter
    public Map<String, VideoRecord> getVideos() {
        return mVideos;
    }

    /**
     * Set the VideoRecord for a video id.
     * @param videoId the video id.
     * @param video the VideoRecord.
     */
    @JsonAnySetter
    public void setVideoRecord(String videoId, VideoRecord video) {
        mVideos.put(videoId, video);
    }

    /**
     * Remove the VideoRecord for a video id.
     * @param videoId The id to remove.
     */
    public void removeVideoRecord(String videoId) {
        mVideos.remove(videoId);
    }

    /**
     * @return true if every required video has been completed.
     */
    @JsonIgnore
    public boolean isComplete() {
        for (VideoRecord r : mVideos.values()) {
            if (r.getRequired() && !r.getComplete()) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return true if the chapter is required.
     */
    @JsonIgnore
    public boolean isRequired() {
        for (VideoRecord r : mVideos.values()) {
            if (r.getRequired()) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return the completion date for the chapter, or null if it has not been completed.
     */
    @JsonIgnore
    public Date getCompletionDate() {
        Date latest = new Date(0);
        for (VideoRecord video : mVideos.values()) {
            if (video.getRequired() && !video.getComplete()) {
                // Hey, this chapter isn't complete!
                return null;
            }

            Date completionDate = video.getCompletionDate();
            if (completionDate != null) {
                if (completionDate.after(latest)) {
                    latest = completionDate;
                }
            }
        }
        return latest;
    }

    /**
     * Deeply clone a chapter.
     *
     * @return a new Chapter object identical but independent of this one.
     */
    public Chapter clone() throws CloneNotSupportedException {
        Chapter c = new Chapter(mName);
        for (Map.Entry<String, VideoRecord> videoEntry : mVideos.entrySet()) {
            c.setVideoRecord(videoEntry.getKey(), videoEntry.getValue().clone());
        }
        return c;
    }
}
