package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@AllArgsConstructor
public class ChangelogService {

    static String getUserName() {
        return ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
    }

    private final IModelLoader<IDocumentModel> documentModelLoader;
    private final IDocumentModelService documentModelService;

    // Note on related (but different) library functionality: if we were interested in changes between documents
    // that _do not include_ shifted group repetitions, we could use DocumentV2Utils.compare()
    // Here we had to implement the comparison ourselves, because DocumentV2Utils.compare() does not currently
    // offer a way to detect group repetitions that have moved to a different repetition index (due to inserts or
    // deletions of preceding repetitions)
    public DocumentV2 addChangelogInfo(DocumentV2 document, DocumentV2 reference) {
        Instant now = Instant.now();
        String userName = getUserName();

        String documentModelId = document.getDocumentModelId();
        DocumentModelVisitorForChangelog dmInfo = new DocumentModelVisitorForChangelog(documentModelService);
        IDocumentModel documentModel = documentModelLoader.loadModel(documentModelId);
        new DocumentModelWalker().acceptDocumentModel(documentModel, dmInfo);

        DocumentVisitorForChangelog entitiesWithChangelog = new DocumentVisitorForChangelog(dmInfo);
        document.traverse(entitiesWithChangelog);
        DocumentVisitorForChangelog referenceEntities = new DocumentVisitorForChangelog(dmInfo);
        reference.traverse(referenceEntities);

        DocumentV2 result = document;

        FieldChangelogService fieldService = new FieldChangelogService(userName, now);
        result = fieldService.addChangelogInfo(result, entitiesWithChangelog, referenceEntities);

        RepetitionsChangelogService repetitionsService = new RepetitionsChangelogService(userName, now);
        result = repetitionsService.addChangelogInfo(result, reference, dmInfo.getGroupPathsWithChangelog());

        AttachmentChangelogService attachmentService = new AttachmentChangelogService(userName, now);
        result = attachmentService.addChangelogInfo(result, reference);

        return result;
    }

}
