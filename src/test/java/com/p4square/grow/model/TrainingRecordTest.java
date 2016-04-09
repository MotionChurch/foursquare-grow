/*
 * Copyright 2013 Jesse Morgan
 */

package com.p4square.grow.model;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test TrainingRecord, Playlist, and Chapter.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingRecordTest {
    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(TrainingRecordTest.class.getName());
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Test deserialization of a JSON Training record.
     */
    @Test
    public void testDeserialization() throws Exception {
        InputStream in = getClass().getResourceAsStream("trainingrecord.json");
        TrainingRecord record = MAPPER.readValue(in, TrainingRecord.class);

        // Last Video
        assertEquals("teacher-1", record.getLastVideo());

        // Playlist
        Playlist playlist = record.getPlaylist();

        // Find video successfully
        VideoRecord r = playlist.find("teacher-1");
        assertEquals(true, r.getRequired());
        assertEquals(true, r.getComplete());
        assertEquals(new Date(1379288806266L), r.getCompletionDate());

        // Find non-existent video
        r = playlist.find("not-a-video");
        assertEquals(null, r);

        // isChapterComplete
        assertTrue(playlist.isChapterComplete("seeker")); // Complete because not required.
        assertTrue(playlist.isChapterComplete("disciple")); // Required and completed.
        assertFalse(playlist.isChapterComplete("teacher")); // Not complete.

        // getChapterStatuses
        Map<String, Boolean> statuses = playlist.getChapterStatuses();
        assertTrue(statuses.get("seeker")); // Complete because not required.
        assertTrue(statuses.get("disciple")); // Required and completed.
        assertFalse(statuses.get("teacher")); // Not complete.
    }

    /**
     * Tests for VideoRecord.
     */
    @Test
    public void testVideoRecord() {
        VideoRecord record = new VideoRecord();

        // Verify defaults
        assertTrue(record.getRequired());
        assertFalse(record.getComplete());
        assertEquals(null, record.getCompletionDate());

        // Verify completion
        long now = System.currentTimeMillis();
        record.complete();
        assertTrue(record.getRequired());
        assertTrue(record.getComplete());
        assertTrue(now <= record.getCompletionDate().getTime());
    }
}
