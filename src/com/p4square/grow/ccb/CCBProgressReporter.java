package com.p4square.grow.ccb;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.*;
import com.p4square.grow.frontend.ProgressReporter;
import com.p4square.grow.model.Score;
import org.apache.log4j.Logger;
import org.restlet.security.User;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * A ProgressReporter which records progress in CCB.
 *
 * Except not really, because it's not implemented yet.
 * This is just a placeholder until ccb-api-client-java has support for updating an individual.
 */
public class CCBProgressReporter implements ProgressReporter {

    private static final Logger LOG = Logger.getLogger(CCBProgressReporter.class);

    private static final String GROW_LEVEL = "GrowLevelTrain";
    private static final String GROW_ASSESSMENT = "GrowLevelAsmnt";

    private final CCBAPI mAPI;
    private final CustomFieldCache mCache;

    public CCBProgressReporter(final CCBAPI api, final CustomFieldCache cache) {
        mAPI = api;
        mCache = cache;
    }

    @Override
    public void reportAssessmentComplete(final User user, final String level, final Date date, final String results) {
        if (!(user instanceof CCBUser)) {
            throw new IllegalArgumentException("Expected CCBUser but got " + user.getClass().getCanonicalName());
        }
        final CCBUser ccbuser = (CCBUser) user;

        updateLevelAndDate(ccbuser, GROW_ASSESSMENT, level, date);
    }

    @Override
    public void reportChapterComplete(final User user, final String chapter, final Date date) {
        if (!(user instanceof CCBUser)) {
            throw new IllegalArgumentException("Expected CCBUser but got " + user.getClass().getCanonicalName());
        }
        final CCBUser ccbuser = (CCBUser) user;

        // Only update the level if it is increasing.
        final CustomPulldownFieldValue currentLevel = ccbuser.getProfile()
                .getCustomPulldownFields().getByLabel(GROW_LEVEL);

        if (currentLevel != null) {
            if (Score.numericScore(chapter) <= Score.numericScore(currentLevel.getSelection().getLabel())) {
                LOG.info("Not updating level for " + user.getIdentifier()
                        + " because current level (" + currentLevel.getSelection().getLabel()
                        + ") is greater than new level (" + chapter + ")");
                return;
            }
        }

        updateLevelAndDate(ccbuser, GROW_LEVEL, chapter, date);
    }

    private void updateLevelAndDate(final CCBUser user, final String field, final String level, final Date date) {
        boolean modified = false;

        final UpdateIndividualProfileRequest req = new UpdateIndividualProfileRequest()
                .withIndividualId(user.getProfile().getId());

        final CustomField pulldownField = mCache.getIndividualPulldownByLabel(field);
        if (pulldownField != null) {
            final LookupTableType type = LookupTableType.valueOf(pulldownField.getName().toUpperCase());
            final LookupTableItem item = mCache.getPulldownItemByName(type, level);
            if (item != null) {
                req.withCustomPulldownField(pulldownField.getName(), item.getId());
                modified = true;
            }
        }

        final CustomField dateField = mCache.getDateFieldByLabel(field);
        if (dateField != null) {
            req.withCustomDateField(dateField.getName(), date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            modified = true;
        }

        try {
            // Only update if a field exists.
            if (modified) {
                mAPI.updateIndividualProfile(req);
            }

        } catch (IOException e) {
            LOG.error("updateIndividual failed for " + user.getIdentifier()
                    + ", field " + field
                    + ", level " + level
                    + ", date " + date.toString());
        }
    }
}
