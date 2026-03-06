package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.mgmtp.a12.template.server.utils.Constants.ID_POINTER;

// another use case for the document visitor
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

    @Override
    public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
        String path = pointerRelativeToBase.fullName();
        if (groupPathsWithChangelog.contains(path)) {
            currentGroupId = (String) group.fieldValue(ID_POINTER);
        }
        return DescendType.VISIT_CHILDREN;
    }

    @Override
    public void endVisitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {
        if (groupPathsWithChangelog.contains(pointerRelativeToBase.fullName())) {
            currentGroupId = null;
        }
    }

    @Override
    public void visitField(DocumentPointer pointerRelativeToBase, FieldInstanceV2 field) {
        if (fieldPathsWithChangelog.contains(pointerRelativeToBase.fullName())) {
            if (currentGroupId != null) {
                repeatableFieldInstancesWithChangelog.computeIfAbsent(currentGroupId, k -> new HashMap<>())
                        .put(pointerRelativeToBase, field);
            } else {
                fieldInstancesWithChangelog.put(pointerRelativeToBase, field);
            }
        }
    }

}
