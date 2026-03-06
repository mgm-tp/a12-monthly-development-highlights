package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentMultiPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.utils.DocumentV2Utils;
import com.mgmtp.a12.template.server.typings.views._changemetadata_dm.ChangeMetadata;
import com.mgmtp.a12.template.server.typings.views._repetitionchange_dm.RepetitionChange;
import com.mgmtp.a12.template.server.typings.views._repetitionchange_dm._repetitionchange.ChangeType;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mgmtp.a12.template.server.utils.Constants.CHANGELOG_GROUP_SUFFIX;
import static com.mgmtp.a12.template.server.utils.Constants.ID_FIELD_NAME;
import static com.mgmtp.a12.template.server.utils.Constants.POINTER_WILDCARD;

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
                DocumentPointer groupPointer = groupInstanceEntry.getKey();
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
                    result = handleRepetitionChange(result, groupPointer, groupInstance, ChangeType.ADDED);
                } else {
                    // changed -> we don't care, let's remove the map entry
                    groupInstancesRef.remove(entryRef.get());
                }
            }
            for (Map.Entry<DocumentPointer, GroupInstanceV2> removed : groupInstancesRef) {
                DocumentPointer groupPointer = removed.getKey();
                GroupInstanceV2 groupInstance = removed.getValue();
                result = handleRepetitionChange(result, groupPointer, groupInstance, ChangeType.DELETED);
            }
        }

        return result;
    }

    private DocumentV2 handleRepetitionChange(
            DocumentV2 result,
            DocumentPointer groupPointer,
            GroupInstanceV2 groupInstance,
            ChangeType changeType
    ) {
        var rcp = RepetitionChange._pointer();
        String repetitionDetails = createRepetitionDetails(groupInstance);
        var changelogEntry = RepetitionChange._empty()
                ._with(rcp.changeType(), changeType)
                ._with(rcp.repetitionDetails(), repetitionDetails);
        var pcm = ChangeMetadata._pointer();
        ChangeMetadata changeMeta = ChangeMetadata._empty()
                ._with(pcm.changedBy(), userName)
                ._with(pcm.changedAt(), now);

        changelogEntry = changelogEntry._with(rcp.changeMetadata(), changeMeta);

        // Note: the changelogGroupName is derived dynamically,
        // so here the compile-time checks against the DM via
        // typed accessors cannot be used / would not make sense
        // -> we use plain DocumentPointer and the _unwrap()'ed changelogEntry
        String entityName = groupPointer.getPathParts().getLast().name();
        String changelogGroupName = entityName + CHANGELOG_GROUP_SUFFIX + POINTER_WILDCARD;
        DocumentPointer changelogGroupPointer =
                groupPointer.parent().withConcatenated(DocumentPointer.of(changelogGroupName));
        return result.withGroupRepetitionAppended(changelogGroupPointer, changelogEntry._unwrap());
    }

    private String createRepetitionDetails(GroupInstanceV2 groupInstance) {
        return groupInstance.directFields().stream()
                .filter(e -> !ID_FIELD_NAME.equals(e.getKey()))
                .map(e -> "%s: %s".formatted(e.getKey(), e.getValue().value()))
                .collect(Collectors.joining("\n"));
    }

}
