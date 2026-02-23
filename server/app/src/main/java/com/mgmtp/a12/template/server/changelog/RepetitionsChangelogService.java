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

            // extract the group instances from document and reference (be sure to use wildcards - ofPathWithAllWildcards)
            DocumentMultiPointer pointer = DocumentMultiPointer.ofPathWithAllWildcards(groupPath);
            var groupInstancesDoc = DocumentV2Utils.getGroupInstances(document, pointer);
            var groupInstancesRef = DocumentV2Utils.getGroupInstances(reference, pointer);

            for (Map.Entry<DocumentPointer, GroupInstanceV2> groupInstanceEntry : groupInstancesDoc) {
                String id = (String) groupInstanceEntry.getValue().directFieldValue(ID_FIELD_NAME);
                var entryRef = groupInstancesRef.stream()
                        .filter(e -> {
                            String refId = (String) e.getValue().directFieldValue(ID_FIELD_NAME);
                            return id.equals(refId);
                        })
                        .findFirst();
                if (entryRef.isEmpty()) {
                    // added
                    var changelogEntry = RepetitionChange._empty()
                            ._with(RepetitionChange._pointer().changeType(), ChangeType.ADDED)
                            ._with(RepetitionChange._pointer().repetitionDetails(), "");
                    ChangeMetadata._empty()
                            ._with(ChangeMetadata._pointer().changedAt(), now)
                            ._with(ChangeMetadata._pointer().changedBy(), userName);
                }
            }

            // iterate over the group instances from document

            // get group id and also from reference

            // compare existence (consider deleted ones)

            // build RepetitionChange using TAC
            // implement repetition details
        }

        return result;
    }

}
