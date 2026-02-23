package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import java.time.Instant;

class AttachmentChangelogService extends CommonChangelogService {

    AttachmentChangelogService(String userName, Instant now) {
        super(userName, now);
    }

    DocumentV2 addChangelogInfo(DocumentV2 document, DocumentV2 reference) {
        DocumentV2 result = document;

        // use type accessor classes

        // get attachment_id (be careful with NPE)

        // compare ids: new, updated, deleted

        // add changelog entry to document

        return result;
    }

}
