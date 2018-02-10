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
        playlist.add(Chapters.SEEKER, "video1");
        playlist.add(Chapters.SEEKER, "video2");

        // Chapter should not be complete
        assertFalse(playlist.isChapterComplete(Chapters.SEEKER));

        // We should find the chapter in the map
        Map<Chapters, Chapter> chapterMap = playlist.getChaptersMap();
        Chapter chapter1 = chapterMap.get(Chapters.SEEKER);
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
        assertTrue(playlist.isChapterComplete(Chapters.SEEKER));
        
        // But other chapters should not be complete.
        assertFalse(playlist.isChapterComplete(Chapters.BELIEVER));
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
        oldList.add(Chapters.SEEKER, "video1").setRequired(true);
        oldList.add(Chapters.BELIEVER, "video2").setRequired(false);
        oldList.add(Chapters.BELIEVER, "video3").complete();
        oldList.setLastUpdated(new Date(100));

        Playlist newList = new Playlist();
        newList.add(Chapters.SEEKER, "video4").setRequired(true);
        newList.add(Chapters.BELIEVER, "video5").setRequired(false);
        newList.add(Chapters.DISCIPLE, "video6").setRequired(false);
        newList.setLastUpdated(new Date(500));

        // Verify that you can't merge the old into the new
        newList.merge(oldList);
        assertTrue(null == newList.find("video2"));

        // Merge the new list into the old and verify results
        oldList.merge(newList);

        // All Videos Present
        assertFalse(oldList.find("video1").getRequired());  // N.B. not required because video
                                                            // was removed from newer playlist.
        assertFalse(oldList.find("video2").getRequired());
        assertTrue(oldList.find("video3").getComplete());
        assertTrue(oldList.find("video4").getRequired());
        assertFalse(oldList.find("video5").getRequired());
        assertFalse(oldList.find("video6").getRequired());

        // New Chapter added
        Map<Chapters, Chapter> chapters = oldList.getChaptersMap();
        assertEquals(3, chapters.size());
        assertTrue(null != chapters.get(Chapters.DISCIPLE));

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
        oldList.add(Chapters.SEEKER, "video1").setRequired(true);
        VideoRecord toMove = oldList.add(Chapters.SEEKER, "video2");
        toMove.setRequired(true);
        toMove.complete();
        oldList.add(Chapters.BELIEVER, "video3").complete();
        oldList.setLastUpdated(new Date(100));

        Playlist newList = new Playlist();
        newList.add(Chapters.SEEKER, "video1").setRequired(true);
        newList.add(Chapters.BELIEVER, "video2").setRequired(true);
        newList.add(Chapters.DISCIPLE, "video3").complete();
        newList.setLastUpdated(new Date(500));

        // Merge the new list into the old and verify results
        oldList.merge(newList);

        // All Videos Present
        assertTrue(oldList.find("video1").getRequired());
        assertTrue(oldList.find("video2").getRequired());
        assertTrue(oldList.find("video3").getComplete());

        // toMove is in the correct chapter.
        assertNull(oldList.getChaptersMap().get(Chapters.SEEKER).getVideoRecord("video2"));
        VideoRecord afterMove = oldList.getChaptersMap().get(Chapters.BELIEVER).getVideoRecord("video2");
        assertSame(toMove, afterMove);

        // video3 got moved to the new chapter3
        assertNull(oldList.getChaptersMap().get(Chapters.BELIEVER).getVideoRecord("video3"));
        assertTrue(oldList.getChaptersMap().get(Chapters.DISCIPLE).getVideoRecord("video3").getComplete());
    }

    /**
     * If a required video has been removed in the newer playlist, we don't
     * consider it to be required anymore since the user will not be able to
     * view the removed video.
     */
    @Test
    public void testMergeRemovedVideosAreNotRequired() {
        Playlist oldList = new Playlist();
        oldList.add(Chapters.SEEKER, "video1").setRequired(true);
        oldList.add(Chapters.SEEKER, "video2").setRequired(true);
        oldList.setLastUpdated(new Date(100));

        Playlist newList = new Playlist();
        newList.add(Chapters.SEEKER, "video1").setRequired(true);
        newList.add(Chapters.SEEKER, "video3").setRequired(true);
        newList.setLastUpdated(new Date(500));

        // Merge the new list into the old and verify results
        oldList.merge(newList);

        // video2 should exist, but no longer be required.
        assertTrue(oldList.find("video1").getRequired());
        assertFalse(oldList.find("video2").getRequired());
        assertTrue(oldList.find("video3").getRequired());
    }

    /**
     * (1) If a new required video is added to a chapter that the user has
     * already completed, we don't want to force the user to go back to the
     * previous chapter, so the video should be marked as not required.
     *
     * (2) Likewise, if a previously optional video is marked as required in a
     * chapter that the user has already completed, we don't want to force the
     * user to go back to the previous chapter, so the video should be marked
     * as not required.
     */
    @Test
    public void testMergeVideosAreNotRequiredInCompletedChapters() {
        Playlist oldList = new Playlist();
        // video1 is complete and required.
        VideoRecord video1 = oldList.add(Chapters.SEEKER, "video1");
        video1.setRequired(true);
        video1.setComplete(true);
        // video2 is not complete, but qualifies as complete because it's not required.
        VideoRecord video2 = oldList.add(Chapters.SEEKER, "video2");
        video2.setRequired(false);
        video2.setComplete(false);
        oldList.setLastUpdated(new Date(100));

        Playlist newList = new Playlist();
        newList.add(Chapters.SEEKER, "video1").setRequired(true);
        newList.add(Chapters.SEEKER, "video2").setRequired(true);
        newList.add(Chapters.SEEKER, "video3").setRequired(true);
        newList.setLastUpdated(new Date(500));

        // Merge the new list into the old and verify results
        oldList.merge(newList);

        // video1 is unchanged.
        assertTrue(oldList.find("video1").getComplete());
        assertTrue(oldList.find("video1").getRequired());

        // Case 2: video2 is not required because the chapter was completed.
        assertFalse(oldList.find("video2").getComplete());
        assertFalse(oldList.find("video2").getRequired());

        // Case 1: new video3 is not required because the chapter was completed.
        assertFalse(oldList.find("video3").getComplete());
        assertFalse(oldList.find("video3").getRequired());
    }

    /**
     * This is the opposite of the previous test case:
     * new required videos are required if the chapter is not complete.
     */
    @Test
    public void testMergeVideosAreRequiredInIncompletedChapters() {
        Playlist oldList = new Playlist();
        // video1 is complete and required.
        VideoRecord video1 = oldList.add(Chapters.SEEKER, "video1");
        video1.setRequired(true);
        video1.setComplete(true);
        // video2 is not complete, but qualifies as complete because it's not required.
        VideoRecord video2 = oldList.add(Chapters.SEEKER, "video2");
        video2.setRequired(false);
        video2.setComplete(false);
        // video3 is not complete and required, therefore the chapter is not complete.
        VideoRecord video3 = oldList.add(Chapters.SEEKER, "video3");
        video3.setRequired(true);
        video3.setComplete(false);
        oldList.setLastUpdated(new Date(100));

        Playlist newList = new Playlist();
        newList.add(Chapters.SEEKER, "video1").setRequired(true);
        newList.add(Chapters.SEEKER, "video2").setRequired(true);
        newList.add(Chapters.SEEKER, "video3").setRequired(true);
        newList.add(Chapters.SEEKER, "video4").setRequired(true);
        newList.setLastUpdated(new Date(500));

        // Merge the new list into the old and verify results
        oldList.merge(newList);

        // videos 1 and 3 are unchanged.
        assertTrue(oldList.find("video1").getComplete());
        assertTrue(oldList.find("video1").getRequired());
        assertFalse(oldList.find("video3").getComplete());
        assertTrue(oldList.find("video3").getRequired());

        // Case 2: video2 is now required
        assertFalse(oldList.find("video2").getComplete());
        assertTrue(oldList.find("video2").getRequired());

        // Case 1: new video4 is required because the chapter was not completed.
        assertFalse(oldList.find("video4").getComplete());
        assertTrue(oldList.find("video4").getRequired());
    }
}
