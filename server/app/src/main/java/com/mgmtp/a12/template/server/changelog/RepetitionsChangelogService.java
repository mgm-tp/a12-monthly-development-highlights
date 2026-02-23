package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;

import java.time.Instant;
import java.util.Set;

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

            // iterate over the group instances from document

            // get group id and also from reference

            // compare existence (consider deleted ones)

            // build RepetitionChange using TAC
            // implement repetition details
        }

        return result;
    }

}
