package com.p4square.grow.ccb;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.GetIndividualProfilesRequest;
import com.p4square.ccbapi.model.GetIndividualProfilesResponse;
import org.apache.log4j.Logger;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.Verifier;

/**
 * CCBUserVerifier authenticates a user through the CCB individual_profile_from_login_password API.
 */
public class CCBUserVerifier implements Verifier {
    private static final Logger LOG = Logger.getLogger(CCBUserVerifier.class);

    private final CCBAPI mAPI;

    public CCBUserVerifier(final CCBAPI api) {
        mAPI = api;
    }

    @Override
    public int verify(Request request, Response response) {
        if (request.getChallengeResponse() == null) {
            return RESULT_MISSING; // no credentials
        }

        final String username = request.getChallengeResponse().getIdentifier();
        final char[] password = request.getChallengeResponse().getSecret();

        try {
            GetIndividualProfilesResponse resp = mAPI.getIndividualProfiles(
                    new GetIndividualProfilesRequest().withLoginPassword(username, password));

            if (resp.getIndividuals().size() == 1) {
                // Wrap the IndividualProfile up in an User and update the user on the request.
                final CCBUser user = new CCBUser(resp.getIndividuals().get(0));
                LOG.info("Successfully authenticated " + user.getIdentifier());
                request.getClientInfo().setUser(user);
                return RESULT_VALID;
            }

        } catch (Exception e) {
            LOG.error("CCB API Exception: " + e, e);
        }

        return RESULT_INVALID; // Invalid credentials
    }
}
