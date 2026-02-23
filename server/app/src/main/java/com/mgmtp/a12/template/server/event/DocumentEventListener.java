package com.mgmtp.a12.template.server.event;

import com.mgmtp.a12.dataservices.common.events.CommonDataServicesEventListener;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeCreateEvent;
import com.mgmtp.a12.dataservices.document.events.DocumentBeforeUpdateEvent;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.template.server.changelog.ChangelogService;
import com.mgmtp.a12.template.server.utils.GroupIdSetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mgmtp.a12.template.server.utils.Constants.PERSON_DM_NAME;

@Component
@RequiredArgsConstructor
public class DocumentEventListener {

    private final GroupIdSetter groupIdSetter;
    private final ChangelogService changelogService;

    @CommonDataServicesEventListener
    public void beforeCreateListener(DocumentBeforeCreateEvent event) {
        DocumentV2 document = event.getCreatedDocument();
        if (PERSON_DM_NAME.equals(document.getDocumentModelId())) {
            DocumentV2 documentWithIds = groupIdSetter.addIdToGroups(document);
            DocumentV2 documentWithChangelog = changelogService.addChangelogInfo(
                    documentWithIds,
                    DocumentV2.empty(PERSON_DM_NAME)
            );
            event.setCreatedDocument(documentWithChangelog);
        }
    }

    @CommonDataServicesEventListener
    public void beforeUpdateListener(DocumentBeforeUpdateEvent event) {
        DocumentV2 document = event.getUpdatedDocument();
        if (PERSON_DM_NAME.equals(document.getDocumentModelId())) {
            DocumentV2 documentWithIds = groupIdSetter.addIdToGroups(document);
            DocumentV2 documentWithChangelog = changelogService.addChangelogInfo(
                    documentWithIds,
                    event.getPersistedDocument()
            );
            event.setUpdatedDocument(documentWithChangelog);
        }
    }

}
