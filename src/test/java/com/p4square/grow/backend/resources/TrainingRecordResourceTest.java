/*
 * Copyright 2015 Jesse Morgan
 */

package com.p4square.grow.backend.resources;

import java.util.Map;
import java.util.HashMap;

import com.p4square.grow.model.Chapters;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.jackson.JacksonRepresentation;

import com.p4square.grow.model.Playlist;
import com.p4square.grow.model.TrainingRecord;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the TrainingRecordResource
 *
 * @author Jesse Morgan <jesse@jesterpm.net>
 */
public class TrainingRecordResourceTest extends ResourceTestBase {

    private final String USER_ID = "1234";

    private TrainingRecordResource mResource;
    private Request mRequest;
    private Response mResponse;

    @Before
    public void setup() throws Exception {
        super.setup();

        mResource = new TrainingRecordResource();

        mRequest = new Request(Method.GET, "/");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", USER_ID);
        mRequest.setAttributes(attributes);
        mResponse = new Response(mRequest);

        Playlist playlist = new Playlist();
        playlist.add(Chapters.INTRODUCTION, "intro-1");
        playlist.add(Chapters.SEEKER, "seeker-1");
        playlist.add(Chapters.BELIEVER, "believer-1");
        playlist.add(Chapters.BELIEVER, "believer-2");
        playlist.add(Chapters.DISCIPLE, "disciple-1");
        playlist.add(Chapters.TEACHER, "teacher-1");
        playlist.add(Chapters.LEADER, "leader-1");
        mApplication.setDefaultPlaylist(playlist);
    }

    private <T> T run(Class<T> type) throws Exception {
        mResource.init(mApplication.getContext(), mRequest, mResponse);
        mResource.handle();
        mResource.release();

        return new JacksonRepresentation<T>(mResponse.getEntity(), type).getObject();
    }

    @Test
    public void testSkipAssessedChaptersLow() throws Exception {
        // Set the User's score.
        mApplication.getAnswerProvider().put(USER_ID, "summary", "{\"sum\": 0.0, \"count\": 1}");

        // Run the test
        TrainingRecord record = run(TrainingRecord.class);

        // Assert correct videos required.
        Playlist actualPlaylist = record.getPlaylist();
        assertTrue(actualPlaylist.find("intro-1").getRequired()); // Always required.
        assertTrue(actualPlaylist.find("seeker-1").getRequired()); // Required by assessment.
        assertTrue(actualPlaylist.find("believer-1").getRequired());
        assertTrue(actualPlaylist.find("believer-1").getRequired());
        assertTrue(actualPlaylist.find("disciple-1").getRequired());
        assertTrue(actualPlaylist.find("teacher-1").getRequired());
        assertTrue(actualPlaylist.find("leader-1").getRequired());
    }

    @Test
    public void testSkipAssessedChaptersSeeker() throws Exception {
        // Set the User's score.
        mApplication.getAnswerProvider().put(USER_ID, "summary", "{\"sum\": 1.0, \"count\": 1}");

        // Run the test
        TrainingRecord record = run(TrainingRecord.class);

        // Assert correct videos required.
        Playlist actualPlaylist = record.getPlaylist();
        assertTrue(actualPlaylist.find("intro-1").getRequired()); // Always required.
        assertTrue(actualPlaylist.find("seeker-1").getRequired()); // Required by assessment.
        assertTrue(actualPlaylist.find("believer-1").getRequired());
        assertTrue(actualPlaylist.find("believer-1").getRequired());
        assertTrue(actualPlaylist.find("disciple-1").getRequired());
        assertTrue(actualPlaylist.find("teacher-1").getRequired());
        assertTrue(actualPlaylist.find("leader-1").getRequired());
    }

    @Test
    public void testSkipAssessedChaptersBeliever() throws Exception {
        // Set the User's score.
        mApplication.getAnswerProvider().put(USER_ID, "summary", "{\"sum\": 2.0, \"count\": 1}");

        // Run the test
        TrainingRecord record = run(TrainingRecord.class);

        // Assert correct videos required.
        Playlist actualPlaylist = record.getPlaylist();
        assertTrue(actualPlaylist.find("intro-1").getRequired()); // Always required.
        assertFalse(actualPlaylist.find("seeker-1").getRequired()); // Not required by assessment.
        assertTrue(actualPlaylist.find("believer-1").getRequired()); // Required by assessment.
        assertTrue(actualPlaylist.find("believer-1").getRequired());
        assertTrue(actualPlaylist.find("disciple-1").getRequired());
        assertTrue(actualPlaylist.find("teacher-1").getRequired());
        assertTrue(actualPlaylist.find("leader-1").getRequired());
    }

    @Test
    public void testSkipAssessedChaptersHigh() throws Exception {
        // Set the User's score.
        mApplication.getAnswerProvider().put(USER_ID, "summary", "{\"sum\": 4.0, \"count\": 1}");

        // Run the test
        TrainingRecord record = run(TrainingRecord.class);

        // Assert correct videos required.
        Playlist actualPlaylist = record.getPlaylist();
        assertTrue(actualPlaylist.find("intro-1").getRequired()); // Always required.
        assertFalse(actualPlaylist.find("seeker-1").getRequired()); // Not required by assessment.
        assertFalse(actualPlaylist.find("believer-1").getRequired());
        assertFalse(actualPlaylist.find("believer-1").getRequired());
        assertFalse(actualPlaylist.find("disciple-1").getRequired());
        assertTrue(actualPlaylist.find("teacher-1").getRequired()); // Required by assessment.
        assertTrue(actualPlaylist.find("leader-1").getRequired());
    }
}
