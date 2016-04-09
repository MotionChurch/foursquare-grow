package com.p4square.grow.ccb;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.GetIndividualProfilesRequest;
import com.p4square.ccbapi.model.GetIndividualProfilesResponse;
import com.p4square.ccbapi.model.IndividualProfile;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.ClientInfo;
import org.restlet.security.Verifier;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests for CCBUserVerifier.
 */
public class CCBUserVerifierTest {

    private IndividualProfile mProfile = new IndividualProfile();

    private CCBAPI mAPI;
    private CCBUserVerifier verifier;

    private ClientInfo mClientInfo;
    private Request mMockRequest;
    private Response mMockResponse;

    @Before
    public void setUp() {
        mAPI = EasyMock.mock(CCBAPI.class);
        verifier = new CCBUserVerifier(mAPI);

        mClientInfo = new ClientInfo();
        mMockRequest = EasyMock.mock(Request.class);
        EasyMock.expect(mMockRequest.getClientInfo()).andReturn(mClientInfo).anyTimes();

        mMockResponse = EasyMock.mock(Response.class);

        mProfile.setId(48);
        mProfile.setFirstName("Larry");
        mProfile.setLastName("Bob");
        mProfile.setEmail("larry.bob@example.com");
    }

    private void replay() {
        EasyMock.replay(mAPI, mMockRequest, mMockResponse);
    }

    private void verify() {
        EasyMock.verify(mAPI, mMockRequest, mMockResponse);
    }


    @Test
    public void testVerifyNoCredentials() throws Exception {
        // Prepare mocks
        EasyMock.expect(mMockRequest.getChallengeResponse()).andReturn(null).anyTimes();
        replay();

        // Test
        int result = verifier.verify(mMockRequest, mMockResponse);

        // Verify
        verify();
        assertEquals(Verifier.RESULT_MISSING, result);
        assertNull(mClientInfo.getUser());
    }

    @Test
    public void testVerifyAuthFailure() throws Exception {
        // Prepare mocks
        ChallengeResponse challenge = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        EasyMock.expect(mMockRequest.getChallengeResponse()).andReturn(challenge).anyTimes();
        GetIndividualProfilesResponse response = new GetIndividualProfilesResponse();
        response.setIndividuals(Collections.<IndividualProfile>emptyList());
        EasyMock.expect(mAPI.getIndividualProfiles(new GetIndividualProfilesRequest()
                    .withLoginPassword("user", "pass".toCharArray()))).andReturn(response);
        replay();

        // Test
        int result = verifier.verify(mMockRequest, mMockResponse);

        // Verify
        verify();
        assertEquals(Verifier.RESULT_INVALID, result);
        assertNull(mClientInfo.getUser());
    }

    @Test
    public void testVerifyAuthException() throws Exception {
        // Prepare mocks
        ChallengeResponse challenge = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        EasyMock.expect(mMockRequest.getChallengeResponse()).andReturn(challenge).anyTimes();
        EasyMock.expect(mAPI.getIndividualProfiles(EasyMock.anyObject(GetIndividualProfilesRequest.class)))
                .andThrow(new IOException());
        replay();

        // Test
        int result = verifier.verify(mMockRequest, mMockResponse);

        // Verify
        verify();
        assertEquals(Verifier.RESULT_INVALID, result);
        assertNull(mClientInfo.getUser());
    }

    @Test
    public void testVerifyAuthSuccess() throws Exception {
        // Prepare mocks
        ChallengeResponse challenge = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "user", "pass");
        EasyMock.expect(mMockRequest.getChallengeResponse()).andReturn(challenge).anyTimes();
        GetIndividualProfilesResponse response = new GetIndividualProfilesResponse();
        response.setIndividuals(Collections.singletonList(mProfile));
        EasyMock.expect(mAPI.getIndividualProfiles(new GetIndividualProfilesRequest()
                .withLoginPassword("user", "pass".toCharArray()))).andReturn(response);

        replay();

        // Test
        int result = verifier.verify(mMockRequest, mMockResponse);

        // Verify
        verify();
        assertEquals(Verifier.RESULT_VALID, result);
        assertNotNull(mClientInfo.getUser());
        assertEquals("CCB-48", mClientInfo.getUser().getIdentifier());
        assertEquals("Larry", mClientInfo.getUser().getFirstName());
        assertEquals("Bob", mClientInfo.getUser().getLastName());
        assertEquals("larry.bob@example.com", mClientInfo.getUser().getEmail());
    }
}