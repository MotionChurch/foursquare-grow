package com.p4square.grow.ccb;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.*;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Tests for the CustomFieldCache.
 */
public class CustomFieldCacheTest {

    private CustomFieldCache cache;

    private CCBAPI api;
    private GetCustomFieldLabelsResponse customFieldsResponse;
    private GetLookupTableResponse lookupTableResponse;

    @Before
    public void setUp() {
        api = EasyMock.mock(CCBAPI.class);
        cache = new CustomFieldCache(api);

        // Prepare some custom fields for the test.
        CustomField textField = new CustomField();
        textField.setName("udf_ind_text_6");
        textField.setLabel("Grow Level");

        CustomField dateField = new CustomField();
        dateField.setName("udf_ind_date_6");
        dateField.setLabel("Grow Level");

        CustomField pullDown = new CustomField();
        pullDown.setName("udf_ind_pulldown_6");
        pullDown.setLabel("Grow Level");

        customFieldsResponse = new GetCustomFieldLabelsResponse();
        customFieldsResponse.setCustomFields(Arrays.asList(textField, dateField, pullDown));

        // Prepare some pulldown items for the tests.
        LookupTableItem seeker = new LookupTableItem();
        seeker.setId(1);
        seeker.setOrder(1);
        seeker.setName("Seeker");

        LookupTableItem believer = new LookupTableItem();
        believer.setId(2);
        believer.setOrder(2);
        believer.setName("Believer");

        lookupTableResponse = new GetLookupTableResponse();
        lookupTableResponse.setItems(Arrays.asList(seeker, believer));
    }

    @Test
    public void testGetTextFieldByLabel() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getCustomFieldLabels()).andReturn(customFieldsResponse);
        EasyMock.replay(api);

        // Test the cache
        CustomField field = cache.getTextFieldByLabel("Grow Level");

        // Verify result.
        EasyMock.verify(api);
        assertEquals("udf_ind_text_6", field.getName());
        assertEquals("Grow Level", field.getLabel());
    }

    @Test
    public void testGetDateFieldByLabel() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getCustomFieldLabels()).andReturn(customFieldsResponse);
        EasyMock.replay(api);

        // Test the cache
        CustomField field = cache.getDateFieldByLabel("Grow Level");

        // Verify result.
        EasyMock.verify(api);
        assertEquals("udf_ind_date_6", field.getName());
        assertEquals("Grow Level", field.getLabel());
    }

    @Test
    public void testGetPullDownFieldByLabel() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getCustomFieldLabels()).andReturn(customFieldsResponse);
        EasyMock.replay(api);

        // Test the cache
        CustomField field = cache.getIndividualPulldownByLabel("Grow Level");

        // Verify result.
        EasyMock.verify(api);
        assertEquals("udf_ind_pulldown_6", field.getName());
        assertEquals("Grow Level", field.getLabel());
    }

    @Test
    public void testGetPullDownFieldByLabelMissing() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getCustomFieldLabels()).andReturn(customFieldsResponse);
        EasyMock.replay(api);

        // Test the cache
        CustomField field = cache.getIndividualPulldownByLabel("Missing Label");

        // Verify result.
        EasyMock.verify(api);
        assertNull(field);
    }

    @Test
    public void testGetPullDownFieldByLabelException() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getCustomFieldLabels()).andThrow(new IOException());
        EasyMock.expect(api.getCustomFieldLabels()).andReturn(customFieldsResponse);
        EasyMock.replay(api);

        // Test the cache
        CustomField field1 = cache.getIndividualPulldownByLabel("Grow Level");
        CustomField field2 = cache.getIndividualPulldownByLabel("Grow Level");

        // Verify result.
        EasyMock.verify(api);
        assertNull(field1);
        assertNotNull(field2);
    }

    @Test
    public void testGetMultipleFields() throws Exception {
        // Setup mocks
        // Note: only one API call.
        EasyMock.expect(api.getCustomFieldLabels()).andReturn(customFieldsResponse);
        EasyMock.replay(api);

        // Test the cache
        CustomField field1 = cache.getTextFieldByLabel("Grow Level");
        CustomField field2 = cache.getIndividualPulldownByLabel("Grow Level");

        // Verify result.
        EasyMock.verify(api);
        assertEquals("udf_ind_text_6", field1.getName());
        assertEquals("Grow Level", field1.getLabel());
        assertEquals("udf_ind_pulldown_6", field2.getName());
        assertEquals("Grow Level", field2.getLabel());
    }

    @Test
    public void testGetPullDownOptions() throws Exception {
        // Setup mocks
        Capture<GetLookupTableRequest> requestCapture = EasyMock.newCapture();
        EasyMock.expect(api.getLookupTable(EasyMock.capture(requestCapture))).andReturn(lookupTableResponse);
        EasyMock.replay(api);

        // Test the cache
        LookupTableItem item = cache.getPulldownItemByName(
                LookupTableType.valueOf("udf_ind_pulldown_6".toUpperCase()),
                "Believer");

        // Verify result.
        EasyMock.verify(api);
        assertEquals(LookupTableType.UDF_IND_PULLDOWN_6, requestCapture.getValue().getType());
        assertEquals(2, item.getId());
        assertEquals(2, item.getOrder());
        assertEquals("Believer", item.getName());
    }

    @Test
    public void testGetPullDownOptionsMixedCase() throws Exception {
        // Setup mocks
        Capture<GetLookupTableRequest> requestCapture = EasyMock.newCapture();
        EasyMock.expect(api.getLookupTable(EasyMock.capture(requestCapture))).andReturn(lookupTableResponse);
        EasyMock.replay(api);

        // Test the cache
        LookupTableItem item = cache.getPulldownItemByName(
                LookupTableType.valueOf("udf_ind_pulldown_6".toUpperCase()),
                "BeLiEvEr");

        // Verify result.
        EasyMock.verify(api);
        assertEquals(LookupTableType.UDF_IND_PULLDOWN_6, requestCapture.getValue().getType());
        assertEquals(2, item.getId());
        assertEquals(2, item.getOrder());
        assertEquals("Believer", item.getName());
    }

    @Test
    public void testGetPullDownOptionMissing() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getLookupTable(EasyMock.anyObject())).andReturn(lookupTableResponse);
        EasyMock.replay(api);

        // Test the cache
        LookupTableItem item = cache.getPulldownItemByName(LookupTableType.UDF_IND_PULLDOWN_6, "Something else");

        // Verify result.
        EasyMock.verify(api);
        assertNull(item);
    }

    @Test
    public void testGetPullDownMissing() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getLookupTable(EasyMock.anyObject())).andReturn(new GetLookupTableResponse());
        EasyMock.replay(api);

        // Test the cache
        LookupTableItem item = cache.getPulldownItemByName(LookupTableType.UDF_IND_PULLDOWN_6, "Believer");

        // Verify result.
        EasyMock.verify(api);
        assertNull(item);
    }

    @Test
    public void testGetPullDownException() throws Exception {
        // Setup mocks
        EasyMock.expect(api.getLookupTable(EasyMock.anyObject())).andThrow(new IOException());
        EasyMock.expect(api.getLookupTable(EasyMock.anyObject())).andReturn(lookupTableResponse);
        EasyMock.replay(api);

        // Test the cache
        LookupTableItem item1 = cache.getPulldownItemByName(LookupTableType.UDF_IND_PULLDOWN_6, "Believer");
        LookupTableItem item2 = cache.getPulldownItemByName(LookupTableType.UDF_IND_PULLDOWN_6, "Believer");

        // Verify result.
        EasyMock.verify(api);
        assertNull(item1);
        assertNotNull(item2);
    }
}