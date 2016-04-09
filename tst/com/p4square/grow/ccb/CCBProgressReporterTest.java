package com.p4square.grow.ccb;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.*;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests for the CCBProgressReporter.
 */
public class CCBProgressReporterTest {

    private static final String GROW_LEVEL = "GrowLevelTrain";
    private static final String ASSESSMENT_LEVEL = "GrowLevelAsmnt";

    private CCBProgressReporter reporter;

    private CCBAPI api;
    private CustomFieldCache cache;

    private CCBUser user;
    private Date date;

    @Before
    public void setUp() {
        // Setup some data for testing.
        IndividualProfile profile = new IndividualProfile();
        profile.setId(123);
        profile.setFirstName("Larry");
        profile.setLastName("Cucumber");
        profile.setEmail("larry.cucumber@example.com");

        user = new CCBUser(profile);
        date = new Date(1427889600000L); // 2015-04-01

        // Setup the mocks.
        api = EasyMock.mock(CCBAPI.class);
        cache = EasyMock.mock(CustomFieldCache.class);
        reporter = new CCBProgressReporter(api, cache);
    }

    private void setupCacheMocks() {
        // Setup the Grow Level field.
        CustomField growLevelDate = new CustomField();
        growLevelDate.setName("udf_ind_date_1");
        growLevelDate.setLabel(GROW_LEVEL);

        CustomField growLevelPulldown = new CustomField();
        growLevelPulldown.setName("udf_ind_pulldown_1");
        growLevelPulldown.setLabel(GROW_LEVEL);

        LookupTableItem believer = new LookupTableItem();
        believer.setId(1);
        believer.setOrder(2);
        believer.setName("Believer");

        EasyMock.expect(cache.getDateFieldByLabel(GROW_LEVEL))
                .andReturn(growLevelDate).anyTimes();
        EasyMock.expect(cache.getIndividualPulldownByLabel(GROW_LEVEL))
                .andReturn(growLevelPulldown).anyTimes();
        EasyMock.expect(cache.getPulldownItemByName(LookupTableType.UDF_IND_PULLDOWN_1, "Believer"))
                .andReturn(believer).anyTimes();

        // Setup the Grow Assessment field.
        CustomField growAssessmentDate = new CustomField();
        growAssessmentDate.setName("udf_ind_date_2");
        growAssessmentDate.setLabel(ASSESSMENT_LEVEL);

        CustomField growAssessmentPulldown = new CustomField();
        growAssessmentPulldown.setName("udf_ind_pulldown_2");
        growAssessmentPulldown.setLabel(ASSESSMENT_LEVEL);

        EasyMock.expect(cache.getDateFieldByLabel(ASSESSMENT_LEVEL))
                .andReturn(growAssessmentDate).anyTimes();
        EasyMock.expect(cache.getIndividualPulldownByLabel(ASSESSMENT_LEVEL))
                .andReturn(growAssessmentPulldown).anyTimes();
        EasyMock.expect(cache.getPulldownItemByName(LookupTableType.UDF_IND_PULLDOWN_2, "Believer"))
                .andReturn(believer).anyTimes();
    }

    @Test
    public void reportAssessmentComplete() throws Exception {
        // Setup mocks
        setupCacheMocks();
        Capture<UpdateIndividualProfileRequest> reqCapture = EasyMock.newCapture();
        EasyMock.expect(api.updateIndividualProfile(EasyMock.capture(reqCapture)))
                .andReturn(EasyMock.mock(UpdateIndividualProfileResponse.class));
        replay();

        // Test reporter
        reporter.reportAssessmentComplete(user, "Believer", date, "Data");

        // Assert that the profile was updated.
        verify();
        assertTrue(reqCapture.hasCaptured());
        UpdateIndividualProfileRequest req = reqCapture.getValue();
        assertEquals(1, req.getCustomPulldownFields().get("udf_pulldown_2").intValue());
        assertEquals("2015-04-01", req.getCustomDateFields().get("udf_date_2").toString());
    }

    @Test
    public void testReportChapterCompleteNoPreviousChapter() throws Exception {
        // Setup mocks
        setupCacheMocks();
        Capture<UpdateIndividualProfileRequest> reqCapture = EasyMock.newCapture();
        EasyMock.expect(api.updateIndividualProfile(EasyMock.capture(reqCapture)))
                .andReturn(EasyMock.mock(UpdateIndividualProfileResponse.class));
        replay();

        // Test reporter
        reporter.reportChapterComplete(user, "Believer", date);

        // Assert that the profile was updated.
        verify();
        assertTrue(reqCapture.hasCaptured());
        UpdateIndividualProfileRequest req = reqCapture.getValue();
        assertEquals(1, req.getCustomPulldownFields().get("udf_pulldown_1").intValue());
        assertEquals("2015-04-01", req.getCustomDateFields().get("udf_date_1").toString());
    }

    @Test
    public void testReportChapterCompleteLowerPreviousChapter() throws Exception {
        // Setup mocks
        setupCacheMocks();
        Capture<UpdateIndividualProfileRequest> reqCapture = EasyMock.newCapture();
        EasyMock.expect(api.updateIndividualProfile(EasyMock.capture(reqCapture)))
                .andReturn(EasyMock.mock(UpdateIndividualProfileResponse.class));

        setUserPulldownSelection(GROW_LEVEL, "Seeker");

        replay();

        // Test reporter
        reporter.reportChapterComplete(user, "Believer", date);

        // Assert that the profile was updated.
        verify();
        assertTrue(reqCapture.hasCaptured());
        UpdateIndividualProfileRequest req = reqCapture.getValue();
        assertEquals(1, req.getCustomPulldownFields().get("udf_pulldown_1").intValue());
        assertEquals("2015-04-01", req.getCustomDateFields().get("udf_date_1").toString());
    }

    @Test
    public void testReportChapterCompleteHigherPreviousChapter() throws Exception {
        // Setup mocks
        setupCacheMocks();
        setUserPulldownSelection(GROW_LEVEL, "Disciple");

        replay();

        // Test reporter
        reporter.reportChapterComplete(user, "Believer", date);

        // Assert that the profile was updated.
        verify();
    }

    @Test
    public void testReportChapterCompleteNoCustomField() throws Exception {
        // Setup mocks
        EasyMock.expect(cache.getDateFieldByLabel(EasyMock.anyString())).andReturn(null).anyTimes();
        EasyMock.expect(cache.getIndividualPulldownByLabel(EasyMock.anyString())).andReturn(null).anyTimes();
        EasyMock.expect(cache.getPulldownItemByName(EasyMock.anyObject(), EasyMock.anyString()))
                .andReturn(null).anyTimes();
        replay();

        // Test reporter
        reporter.reportChapterComplete(user, "Believer", date);

        // Assert that the profile was updated.
        verify();
    }

    @Test
    public void testReportChapterCompleteNoSuchValue() throws Exception {
        // Setup mocks
        setupCacheMocks();
        EasyMock.expect(cache.getPulldownItemByName(LookupTableType.UDF_IND_PULLDOWN_1, "Foo"))
                .andReturn(null).anyTimes();
        Capture<UpdateIndividualProfileRequest> reqCapture = EasyMock.newCapture();
        EasyMock.expect(api.updateIndividualProfile(EasyMock.capture(reqCapture)))
                .andReturn(EasyMock.mock(UpdateIndividualProfileResponse.class));
        replay();

        // Test reporter
        reporter.reportChapterComplete(user, "Foo", date);

        // Assert that the profile was updated.
        verify();
        assertTrue(reqCapture.hasCaptured());
        UpdateIndividualProfileRequest req = reqCapture.getValue();
        assertNull(req.getCustomPulldownFields().get("udf_pulldown_1"));
        assertEquals("2015-04-01", req.getCustomDateFields().get("udf_date_1").toString());
    }

    private void setUserPulldownSelection(final String field, final String value) {
        // Get the pulldown field collection for the user.
        CustomFieldCollection<CustomPulldownFieldValue> pulldowns = user.getProfile().getCustomPulldownFields();
        if (pulldowns == null) {
            pulldowns = new CustomFieldCollection<>();
            user.getProfile().setCustomPulldownFields(pulldowns);
        }

        // Create the selection for the value.
        PulldownSelection selection = new PulldownSelection();
        selection.setLabel(value);

        // Create the field/value pair and add it to the collection.
        CustomPulldownFieldValue fieldValue = new CustomPulldownFieldValue();
        fieldValue.setName(field); // This is unused by the test, but it should be a udf_ identifier.
        fieldValue.setLabel(field);
        fieldValue.setSelection(selection);
        pulldowns.add(fieldValue);
    }

    private void replay() {
        EasyMock.replay(api, cache);
    }

    private void verify() {
        EasyMock.verify(api, cache);
    }
}