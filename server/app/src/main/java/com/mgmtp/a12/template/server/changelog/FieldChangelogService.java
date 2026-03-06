package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.template.server.typings.views._fieldchange_dm.FieldChange;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class FieldChangelogService extends CommonChangelogService {

    FieldChangelogService(String userName, Instant now) {
        super(userName, now);
    }

    DocumentV2 addChangelogInfo(
            DocumentV2 document,
            DocumentVisitorForChangelog entitiesWithChangelog,
            DocumentVisitorForChangelog referenceEntities
    ) {
        DocumentV2 result = document;

        Map<DocumentPointer, FieldInstanceV2> fieldInstances = entitiesWithChangelog.getFieldInstancesWithChangelog();
        Map<DocumentPointer, FieldInstanceV2> referenceFieldInstances =
                referenceEntities.getFieldInstancesWithChangelog();

        for (Map.Entry<DocumentPointer, FieldInstanceV2> fieldInstanceEntry : fieldInstances.entrySet()) {
            DocumentPointer fieldPointer = fieldInstanceEntry.getKey();
            FieldInstanceV2 referenceField = referenceFieldInstances.remove(fieldPointer);

            Optional<GroupInstanceV2> changelogEntry = createFieldChangelogEntry(
                    fieldInstanceEntry.getValue(),
                    referenceField
            );

            if (changelogEntry.isPresent()) {
                result = addChangelogEntryToDocument(
                        result,
                        FieldChange._viewOf(changelogEntry.get()),
                        FieldChange._pointer().changeMetadata(),
                        fieldPointer
                );
            }

        }
        for (Map.Entry<DocumentPointer, FieldInstanceV2> entry : referenceFieldInstances.entrySet()) {
            GroupInstanceV2 changelogEntry = GroupInstanceV2.empty();
            changelogEntry = changelogEntry.withField(
                    FieldChange._pointer().oldValue()._unwrap(),
                    entry.getValue()
            );
            result = addChangelogEntryToDocument(
                    result,
                    FieldChange._viewOf(changelogEntry),
                    FieldChange._pointer().changeMetadata(),
                    entry.getKey()
            );
        }

        result = processRepeatableFields(
                result,
                entitiesWithChangelog.getRepeatableFieldInstancesWithChangelog(),
                referenceEntities.getRepeatableFieldInstancesWithChangelog()
        );

        return result;
    }

    private DocumentV2 processRepeatableFields(
            DocumentV2 document,
            Map<String, Map<DocumentPointer, FieldInstanceV2>> repeatableFieldInstances,
            Map<String, Map<DocumentPointer, FieldInstanceV2>> referenceRepeatableFieldInstances
    ) {
        DocumentV2 result = document;
        for (Map.Entry<String, Map<DocumentPointer, FieldInstanceV2>> repeatableFieldInstanceEntry
                : repeatableFieldInstances.entrySet()) {

            String groupId = repeatableFieldInstanceEntry.getKey();
            Map<DocumentPointer, FieldInstanceV2> fieldInstances = repeatableFieldInstanceEntry.getValue();

            Map<DocumentPointer, FieldInstanceV2> referenceGroupInstance =
                    referenceRepeatableFieldInstances.get(groupId);

            if (referenceGroupInstance == null) {
                result = addNewRepetitionFieldChangelog(result, fieldInstances);
            } else {

                for (Map.Entry<DocumentPointer, FieldInstanceV2> fieldInstanceEntry : fieldInstances.entrySet()) {
                    DocumentPointer fieldPointer = fieldInstanceEntry.getKey();
                    // since we already have the match of the repetition, we need to look-up with the path
                    FieldInstanceV2 referenceField = referenceGroupInstance.entrySet().stream()
                            .filter(e -> fieldPointer.fullName().equals(e.getKey().fullName()))
                            .findFirst()
                            .map(Map.Entry::getValue)
                            .orElse(null);

                    Optional<GroupInstanceV2> changelogEntry = createFieldChangelogEntry(
                            fieldInstanceEntry.getValue(),
                            referenceField
                    );

                    if (changelogEntry.isPresent()) {
                        result = addChangelogEntryToDocument(
                                result,
                                FieldChange._viewOf(changelogEntry.get()),
                                FieldChange._pointer().changeMetadata(),
                                fieldPointer
                        );
                    }
                }
            }
        }
        return result;
    }

    private DocumentV2 addNewRepetitionFieldChangelog(
            DocumentV2 document,
            Map<DocumentPointer, FieldInstanceV2> fieldInstances
    ) {
        DocumentV2 result = document;
        for (Map.Entry<DocumentPointer, FieldInstanceV2> fieldInstanceEntry : fieldInstances.entrySet()) {
            GroupInstanceV2 changelogEntry = GroupInstanceV2.empty();
            changelogEntry = changelogEntry.withField(
                    FieldChange._pointer().newValue()._unwrap(),
                    fieldInstanceEntry.getValue()
            );
            result = addChangelogEntryToDocument(
                    result,
                    FieldChange._viewOf(changelogEntry),
                    FieldChange._pointer().changeMetadata(),
                    fieldInstanceEntry.getKey()
            );
        }
        return result;
    }

    private Optional<GroupInstanceV2> createFieldChangelogEntry(
            FieldInstanceV2 currentField,
            FieldInstanceV2 referenceField
    ) {
        if (referenceField == null) {
            // new field
            GroupInstanceV2 changelogEntry = GroupInstanceV2.empty();
            changelogEntry = changelogEntry.withField(
                    FieldChange._pointer().newValue()._unwrap(),
                    currentField
            );
            return Optional.of(changelogEntry);
        } else if (!Objects.equals(currentField, referenceField)) {
            // field changed
            GroupInstanceV2 changelogEntry = GroupInstanceV2.empty()
                    .withField(FieldChange._pointer().oldValue()._unwrap(), referenceField)
                    .withField(FieldChange._pointer().newValue()._unwrap(), currentField);
            return Optional.of(changelogEntry);
        }
        // no change
        return Optional.empty();
    }

}
