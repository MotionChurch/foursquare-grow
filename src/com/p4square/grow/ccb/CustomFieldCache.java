package com.p4square.grow.ccb;

import com.p4square.ccbapi.CCBAPI;
import com.p4square.ccbapi.model.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CustomFieldCache maintains an index from custom field labels to names.
 */
public class CustomFieldCache {

    private static final Logger LOG = Logger.getLogger(CustomFieldCache.class);

    private final CCBAPI mAPI;

    private CustomFieldCollection<CustomField> mTextFields;
    private CustomFieldCollection<CustomField> mDateFields;
    private CustomFieldCollection<CustomField> mIndividualPulldownFields;
    private CustomFieldCollection<CustomField> mGroupPulldownFields;

    private final Map<LookupTableType, Map<String, LookupTableItem>> mItemByNameTable;

    public CustomFieldCache(final CCBAPI api) {
        mAPI = api;
        mTextFields = new CustomFieldCollection<>();
        mDateFields = new CustomFieldCollection<>();
        mIndividualPulldownFields = new CustomFieldCollection<>();
        mGroupPulldownFields = new CustomFieldCollection<>();
        mItemByNameTable = new HashMap<>();
    }

    public CustomField getTextFieldByLabel(final String label) {
        if (mTextFields.size() == 0) {
            refresh();
        }
        return mTextFields.getByLabel(label);
    }

    public CustomField getDateFieldByLabel(final String label) {
        if (mDateFields.size() == 0) {
            refresh();
        }
        return mDateFields.getByLabel(label);
    }

    public CustomField getIndividualPulldownByLabel(final String label) {
        if (mIndividualPulldownFields.size() == 0) {
            refresh();
        }
        return mIndividualPulldownFields.getByLabel(label);
    }

    public CustomField getGroupPulldownByLabel(final String label) {
        if (mGroupPulldownFields.size() == 0) {
            refresh();
        }
        return mGroupPulldownFields.getByLabel(label);
    }

    public LookupTableItem getPulldownItemByName(final LookupTableType type, final String name) {
        Map<String, LookupTableItem> items = mItemByNameTable.get(type);
        if (items == null) {
            if (!cacheLookupTable(type)) {
                return null;
            }
            items = mItemByNameTable.get(type);
        }

        return items.get(name);
    }

    private synchronized void refresh() {
        try {
            // Get all of the custom fields.
            final GetCustomFieldLabelsResponse resp = mAPI.getCustomFieldLabels();

            final CustomFieldCollection<CustomField> newTextFields = new CustomFieldCollection<>();
            final CustomFieldCollection<CustomField> newDateFields = new CustomFieldCollection<>();
            final CustomFieldCollection<CustomField> newIndPulldownFields = new CustomFieldCollection<>();
            final CustomFieldCollection<CustomField> newGrpPulldownFields = new CustomFieldCollection<>();

            for (final CustomField field : resp.getCustomFields()) {
                if (field.getName().startsWith("udf_ind_text_")) {
                    newTextFields.add(field);
                } else if (field.getName().startsWith("udf_ind_date_")) {
                    newDateFields.add(field);
                } else if (field.getName().startsWith("udf_ind_pulldown_")) {
                    newIndPulldownFields.add(field);
                } else if (field.getName().startsWith("udf_grp_pulldown_")) {
                    newGrpPulldownFields.add(field);
                } else {
                    LOG.warn("Unknown custom field type " + field.getName());
                }
            }

            this.mTextFields = newTextFields;
            this.mDateFields = newDateFields;
            this.mIndividualPulldownFields = newIndPulldownFields;
            this.mGroupPulldownFields = newGrpPulldownFields;

        } catch (IOException e) {
            // Error fetching labels.
            LOG.error("Error fetching custom fields: " + e.getMessage(), e);
        }
    }

    private synchronized boolean cacheLookupTable(final LookupTableType type) {
        try {
            final GetLookupTableResponse resp = mAPI.getLookupTable(new GetLookupTableRequest().withType(type));
            mItemByNameTable.put(type,
                    resp.getItems().stream().collect(Collectors.toMap(LookupTableItem::getName, Function.identity())));
            return true;

        } catch (IOException e) {
            LOG.error("Exception caching lookup table of type " + type, e);
        }

        return false;
    }
}
