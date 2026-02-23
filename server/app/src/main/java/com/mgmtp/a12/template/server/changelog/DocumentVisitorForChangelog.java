package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class DocumentVisitorForChangelog implements IDocumentV2Visitor {
    private final Set<String> fieldPathsWithChangelog;
    private final Set<String> groupPathsWithChangelog;

    @Getter
    private final Map<DocumentPointer, FieldInstanceV2> fieldInstancesWithChangelog = new HashMap<>();
    @Getter
    // outer key = group id; inner key = non-repeatable field pointer
    private final Map<String, Map<DocumentPointer, FieldInstanceV2>> repeatableFieldInstancesWithChangelog =
            new HashMap<>();

    private String currentGroupId;

    DocumentVisitorForChangelog(DocumentModelVisitorForChangelog dmInfo) {
        this.fieldPathsWithChangelog = dmInfo.getFieldPathsWithChangelog();
        this.groupPathsWithChangelog = dmInfo.getGroupPathsWithChangelog();
    }

    // implement visit group - cache current group id

    // clean up the current group id

    // cache the field instances using the id to identify the non-repeatable fields

}
