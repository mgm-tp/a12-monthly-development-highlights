package com.mgmtp.a12.template.server.utils;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.FieldInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.mgmtp.a12.template.server.utils.Constants.ID_POINTER;

class DocumentVisitorForGroupIdSetter implements IDocumentV2Visitor {

    private final Set<String> groupPathsWithId;

    @Getter
    private final Set<UpdateAction> updateActions = new HashSet<>();

    DocumentVisitorForGroupIdSetter(Set<String> groupPathsWithId) {
        this.groupPathsWithId = groupPathsWithId;
    }

    @Override
    public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {

        if (groupPathsWithId.contains(pointerRelativeToBase.fullName())) {
            String id = (String) group.fieldValue(ID_POINTER);
            if (StringUtils.isBlank(id)) {
                String uuid = UUID.randomUUID().toString();
                DocumentPointer idPointer = pointerRelativeToBase.withConcatenated(ID_POINTER);
                updateActions.add(UpdateAction.putField(idPointer, FieldInstanceV2.ofValue(uuid)));
            }
        }

        return IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);
    }
}
