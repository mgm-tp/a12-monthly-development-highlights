package com.mgmtp.a12.template.server.utils;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.UpdateAction;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.GroupInstanceV2;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.utils.IDocumentV2Visitor;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

class DocumentVisitorForGroupIdSetter implements IDocumentV2Visitor {

    private final Set<String> groupPathsWithId;

    @Getter
    private final Set<UpdateAction> updateActions = new HashSet<>();

    DocumentVisitorForGroupIdSetter(Set<String> groupPathsWithId) {
        this.groupPathsWithId = groupPathsWithId;
    }

    @Override
    public DescendType visitGroup(DocumentPointer pointerRelativeToBase, GroupInstanceV2 group) {

        // identify if the group is present in the given paths
        // mention descendant and ancestor of (see DocumentPointer#isDescendantOf and #isAncestorOf)

        // implement id if not present

        // create update action

        return IDocumentV2Visitor.super.visitGroup(pointerRelativeToBase, group);
    }
}
