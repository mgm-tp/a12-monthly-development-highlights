package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.template.server.typings.views._repetitionchange_dm.RepetitionChange;
import com.mgmtp.a12.template.server.typings.views._repetitionchange_dm._repetitionchange.ChangeType;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mgmtp.a12.template.server.utils.Constants.ID_FIELD_NAME;

class RepetitionsChangelogService extends CommonChangelogService {

    RepetitionsChangelogService(String userName, Instant now) {
        super(userName, now);
    }

    DocumentV2 addChangelogInfo(
            DocumentV2 document,
            DocumentV2 reference,
            Set<String> groupPathsWithChangelog
    ) {
        DocumentV2 result = document;

        for (String groupPath : groupPathsWithChangelog) {

            DocumentMultiPointer pointer = DocumentMultiPointer.ofPathWithAllWildcards(groupPath);
            // use document and not result to look up into the original state
            var groupInstancesDoc = DocumentV2Utils.getGroupInstances(document, pointer);
            var groupInstancesRef = DocumentV2Utils.getGroupInstances(reference, pointer);

            for (Map.Entry<DocumentPointer, GroupInstanceV2> groupInstanceEntry : groupInstancesDoc) {
                GroupInstanceV2 groupInstance = groupInstanceEntry.getValue();
                // method fieldValue would also work
                String groupInstanceId = (String) groupInstance.directFieldValue(ID_FIELD_NAME);
                var entryRef = groupInstancesRef.stream()
                        .filter(e -> {
                            String groupInstanceIdRef = (String) e.getValue().directFieldValue(ID_FIELD_NAME);
                            return groupInstanceId.equals(groupInstanceIdRef);
                        })
                        .findFirst();
                if (entryRef.isEmpty()) {
                    // new repetition
                    result = handleRepetitionChange(result, groupPath, groupInstance, ChangeType.ADDED);
                } else {
                    // changed -> we don't care, let's remove the map entry
                    groupInstancesRef.remove(entryRef.get());
                }
            }
            for (Map.Entry<DocumentPointer, GroupInstanceV2> removed : groupInstancesRef) {
                GroupInstanceV2 groupInstance = removed.getValue();
                result = handleRepetitionChange(result, groupPath, groupInstance, ChangeType.DELETED);
            }
        }

        return result;
    }

    private DocumentV2 handleRepetitionChange(
            DocumentV2 result,
            String groupPath,
            GroupInstanceV2 groupInstance,
            ChangeType changeType
    ) {
        var rcp = RepetitionChange._pointer();
        String repetitionDetails = createRepetitionDetails(groupInstance);
        var changelogEntry = RepetitionChange._empty()
                ._with(rcp.changeType(), changeType)
                ._with(rcp.repetitionDetails(), repetitionDetails);
        return addChangelogEntryToDocument(
                result,
                changelogEntry,
                rcp.changeMetadata(),
                DocumentPointer.of(groupPath)
        );
    }

    private String createRepetitionDetails(GroupInstanceV2 groupInstance) {
        return groupInstance.directFields().stream()
                .filter(e -> !ID_FIELD_NAME.equals(e.getKey()))
                .map(e -> "%s: %s".formatted(e.getKey(), e.getValue().value()))
                .collect(Collectors.joining("\n"));
    }

}
