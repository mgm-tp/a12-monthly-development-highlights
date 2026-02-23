package com.mgmtp.a12.template.server.utils;

import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.mgmtp.a12.template.server.utils.Constants.WITH_ID_ANNOTATION;

@Component
@AllArgsConstructor
public class GroupIdSetter {

    private final IModelLoader<IDocumentModel> documentModelLoader;
    private final IDocumentModelService documentModelService;

    public DocumentV2 addIdToGroups(DocumentV2 document) {
        Set<String> groupPathsWithId = getGroupPathsWithId(document.getDocumentModelId());

        DocumentVisitorForGroupIdSetter visitor = new DocumentVisitorForGroupIdSetter(groupPathsWithId);
        document.traverse(visitor);

        return document.withBatchUpdates(visitor.getUpdateActions());
    }

    private Set<String> getGroupPathsWithId(String documentModelId) {
        IDocumentModel documentModel = documentModelLoader.loadModel(documentModelId);
        Map<String, String> annotatedElements = documentModelService.getAllAnnotatedElements(
                documentModel,
                WITH_ID_ANNOTATION
        );
        return annotatedElements.keySet();
    }

}
