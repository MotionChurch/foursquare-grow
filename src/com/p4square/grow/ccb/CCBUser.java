package com.p4square.grow.ccb;

import com.p4square.ccbapi.model.IndividualProfile;
import org.restlet.security.User;

/**
 * CCBUser is an adapter between a CCB IndividualProfile and a Restlet User.
 *
 * Note: CCBUser prefixes the user's identifier with "CCB-". This is done to
 *       ensure the identifier does not collide with identifiers from other
 *       systems.
 */
public class CCBUser extends User {

    private final IndividualProfile mProfile;

    /**
     * Wrap an IndividualProfile inside a User object.
     *
     * @param profile The CCB IndividualProfile for the user.
     */
    public CCBUser(final IndividualProfile profile) {
        mProfile = profile;

        setIdentifier("CCB-" + mProfile.getId());
        setFirstName(mProfile.getFirstName());
        setLastName(mProfile.getLastName());
        setEmail(mProfile.getEmail());
    }

    /**
     * @return The IndividualProfile of the user.
     */
    public IndividualProfile getProfile() {
        return mProfile;
    }
}
