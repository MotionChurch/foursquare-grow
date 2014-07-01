/*
 * Copyright 2014 Jesse Morgan
 */

package com.p4square.grow.model;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for Playlist.
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class PlaylistTest {
    public static void main(String... args) {
        org.junit.runner.JUnitCore.main(PlaylistTest.class.getName());
    }

    /**
     * Tests for Playlist and Chapter methods not covered in the deserialization test.
     */
    @Test
    public void testPlaylistAndChapter() {
        // Create a playlist for the test
        Playlist playlist = new Playlist();
        playlist.add("chapter1", "video1");
        playlist.add("chapter1", "video2");

        // Chapter should not be complete
        assertFalse(playlist.isChapterComplete("chapter1"));

        // We should find the chapter in the map
        Map<String, Chapter> chapterMap = playlist.getChaptersMap();
        Chapter chapter1 = chapterMap.get("chapter1");
        assertTrue(null != chapter1);

        // We should find the videos in the map.
        Map<String, VideoRecord> videoMap = chapter1.getVideos();
        assertTrue(null != videoMap.get("video1"));
        assertTrue(null != videoMap.get("video2"));
        assertTrue(null == videoMap.get("video3"));

        // Mark the videos as complete
        VideoRecord video1 = videoMap.get("video1");
        VideoRecord video2 = videoMap.get("video2");
        video1.complete();
        video2.complete();

        // Chapter should be complete now.
        assertTrue(playlist.isChapterComplete("chapter1"));
        assertFalse(playlist.isChapterComplete("bogusChapter"));
    }

    /**
     * Tests for Playlist default values.
     */
    @Test
    public void testPlaylistDefaults() {
        Date before = new Date();
        Playlist p = new Playlist();

        // Verify that a playlist without an explicit lastUpdated date is older than now.
        assertTrue(p.getLastUpdated().before(before));
    }

    /**
     * Tests for the Playlist merge method.
     */
    @Test
    public void testMergePlaylist() {
        Playlist oldList = new Playlist();
        oldList.add("chapter1", "video1").setRequired(true);
        oldList.add("chapter2", "video2").setRequired(false);
        oldList.add("chapter2", "video3").complete();
        oldList.setLastUpdated(new Date(100));

        Playlist newList = new Playlist();
        newList.add("chapter1", "video4").setRequired(true);
        newList.add("chapter2", "video5").setRequired(false);
        newList.add("chapter3", "video6").setRequired(false);
        newList.setLastUpdated(new Date(500));

        // Verify that you can't merge the old into the new
        newList.merge(oldList);
        assertTrue(null == newList.find("video2"));

        // Merge the new list into the old and verify results
        oldList.merge(newList);

        // All Videos Present
        assertTrue(oldList.find("video1").getRequired());
        assertFalse(oldList.find("video2").getRequired());
        assertTrue(oldList.find("video3").getComplete());
        assertTrue(oldList.find("video4").getRequired());
        assertFalse(oldList.find("video5").getRequired());
        assertFalse(oldList.find("video6").getRequired());

        // New Chapter added
        Map<String, Chapter> chapters = oldList.getChaptersMap();
        assertEquals(3, chapters.size());
        assertTrue(null != chapters.get("chapter3"));

        // Date updated
        assertEquals(newList.getLastUpdated(), oldList.getLastUpdated());

        // Video objects are actually independent
        VideoRecord oldVideo4 = oldList.find("video4");
        VideoRecord newVideo4 = newList.find("video4");
        assertTrue(oldVideo4 != newVideo4);
    }

    /**
     * Tests for merges that move videos.
     */
    @Test
    public void testMergeMoveVideoRecord() {
        Playlist oldList = new Playlist();
        oldList.add("chapter1", "video1").setRequired(true);
        VideoRecord toMove = oldList.add("chapter1", "video2");
        toMove.setRequired(true);
        toMove.complete();
        oldList.add("chapter2", "video3").complete();
        oldList.setLastUpdated(new Date(100));

        Playlist newList = new Playlist();
        newList.add("chapter1", "video1").setRequired(true);
        newList.add("chapter2", "video2").setRequired(true);
        newList.add("chapter3", "video3").complete();
        newList.setLastUpdated(new Date(500));

        // Merge the new list into the old and verify results
        oldList.merge(newList);

        // All Videos Present
        assertTrue(oldList.find("video1").getRequired());
        assertTrue(oldList.find("video2").getRequired());
        assertTrue(oldList.find("video3").getComplete());

        // toMove is in the correct chapter.
        assertNull(oldList.getChaptersMap().get("chapter1").getVideoRecord("video2"));
        VideoRecord afterMove = oldList.getChaptersMap().get("chapter2").getVideoRecord("video2");
        assertSame(toMove, afterMove);

        // video3 got moved to the new chapter3
        assertNull(oldList.getChaptersMap().get("chapter2").getVideoRecord("video3"));
        assertTrue(oldList.getChaptersMap().get("chapter3").getVideoRecord("video3").getComplete());
    }
}
