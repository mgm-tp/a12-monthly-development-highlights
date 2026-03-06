package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.template.server.typings.views.Person_DM;
import com.mgmtp.a12.template.server.typings.views._attachmentchange_dm.AttachmentChange;
import com.mgmtp.a12.template.server.typings.views._attachmentchange_dm._attachmentchange.NewAttachment;
import com.mgmtp.a12.template.server.typings.views._attachmentchange_dm._attachmentchange.OldAttachment;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;

class AttachmentChangelogService extends CommonChangelogService {

    AttachmentChangelogService(String userName, Instant now) {
        super(userName, now);
    }

    DocumentV2 addChangelogInfo(DocumentV2 document, DocumentV2 reference) {
        DocumentV2 result = document;

        Person_DM personDoc = Person_DM._viewOf(document);
        Person_DM personRef = Person_DM._viewOf(reference);

        // trying to access via personDoc.person().personalData().photo().attachment_id() might lead to an NPE
        // use _at instead
        var attachmentIdPointer = Person_DM._pointer().person().personalData().photo().attachment_id();
        String attachmentId = personDoc._at(attachmentIdPointer);
        String attachmentIdRef = personRef._at(attachmentIdPointer);

        AttachmentChange changelogEntry = AttachmentChange._empty();
        if (StringUtils.isNotBlank(attachmentId)) {
            NewAttachment photo = NewAttachment._viewOf(personDoc.person().personalData().photo()._unwrap());
            if (StringUtils.isBlank(attachmentIdRef)) {
                // new attachment
                changelogEntry = changelogEntry._with(AttachmentChange._pointer().newAttachment(), photo);
            } else if (!attachmentId.equals(attachmentIdRef)) {
                OldAttachment photoRef = OldAttachment._viewOf(personRef.person().personalData().photo()._unwrap());
                changelogEntry = changelogEntry
                        ._with(AttachmentChange._pointer().oldAttachment(), photoRef)
                        ._with(AttachmentChange._pointer().newAttachment(), photo);
            }
            // no change
        } else if (StringUtils.isNotBlank(attachmentIdRef)) {
            // deleted attachment
            OldAttachment photoRef = OldAttachment._viewOf(personRef.person().personalData().photo()._unwrap());
            changelogEntry = changelogEntry._with(AttachmentChange._pointer().oldAttachment(), photoRef);
        }
        if (!changelogEntry.equals(AttachmentChange._empty())) {
            result = addChangelogEntryToDocument(
                    result,
                    changelogEntry,
                    AttachmentChange._pointer().changeMetadata(),
                    Person_DM._pointer().person().personalData().photo()._unwrap()
            );
        }
        return result;
    }

}
